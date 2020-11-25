package com.mempoolexplorer.bitcoind.adapter.components.factories;

import java.time.Clock;
import java.time.Instant;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockResult;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetMemPoolEntry;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetMemPoolInfo;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolNonVerbose;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolNonVerboseData;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionInput;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionOutput;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionResult;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.RawMemPoolEntryData;
import com.mempoolexplorer.bitcoind.adapter.components.alarms.AlarmLogger;
import com.mempoolexplorer.bitcoind.adapter.components.clients.BitcoindClient;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.TxPoolContainer;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.components.factories.utils.TransactionFactory;
import com.mempoolexplorer.bitcoind.adapter.components.factories.utils.TxAncestryChangesFactory;
import com.mempoolexplorer.bitcoind.adapter.entities.Fees;
import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.TxAncestry;
import com.mempoolexplorer.bitcoind.adapter.entities.TxInput;
import com.mempoolexplorer.bitcoind.adapter.entities.TxOutput;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolImp;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;
import com.mempoolexplorer.bitcoind.adapter.metrics.ProfileMetricNames;
import com.mempoolexplorer.bitcoind.adapter.metrics.annotations.ProfileTime;
import com.mempoolexplorer.bitcoind.adapter.threads.MempoolSeqEvent;
import com.mempoolexplorer.bitcoind.adapter.utils.JSONUtils;
import com.mempoolexplorer.bitcoind.adapter.utils.PercentLog;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TxPoolFillerImpl implements TxPoolFiller {

    @Autowired
    private BitcoindClient bitcoindClient;

    @Autowired
    private TxPoolContainer txPoolContainer;

    @Autowired
    private AlarmLogger alarmLogger;

    @Autowired
    private Clock clock;

    private AtomicInteger changeCounter = new AtomicInteger(0);

    @Override
    @ProfileTime(metricName = ProfileMetricNames.MEMPOOL_INITIAL_CREATION_TIME)
    public TxPool createMemPool() throws TxPoolException {
        try {
            GetMemPoolInfo memPoolInfo = bitcoindClient.getMemPoolInfo();
            if (null != memPoolInfo.getError()) {
                // We can't recover from this error.
                throw new TxPoolException("Error, rawMemPoolNonVerbose returned error: " + memPoolInfo.getError()
                        + ", requestId: " + memPoolInfo.getId());
            }
            log.info("Bitcoind node has {} tx in his mempoool, loading...",
                    memPoolInfo.getGetMemPoolInfoData().getSize());
            GetRawMemPoolNonVerbose rawMemPoolNonVerbose = bitcoindClient.getRawMemPoolNonVerbose();

            if (null != rawMemPoolNonVerbose.getError()) {
                // We can't recover from this error.
                throw new TxPoolException("Error, rawMemPoolNonVerbose returned error: "
                        + rawMemPoolNonVerbose.getError() + ", requestId: " + rawMemPoolNonVerbose.getId());
            }

            GetRawMemPoolNonVerboseData getRawMemPoolNonVerboseData = rawMemPoolNonVerbose
                    .getGetRawMemPoolNonVerboseData();
            int mempoolSequence = getRawMemPoolNonVerboseData.getMempoolSequence();

            log.info("bitcoindClient.getRawMemPoolNonVerbose() returned a mempool with {} Txs and mempoolSequence: {}",
                    getRawMemPoolNonVerboseData.getTrxHashList().size(), mempoolSequence);

            ConcurrentHashMap<String, Transaction> txIdToTxMap = createTxIdToTxMapFrom(
                    getRawMemPoolNonVerboseData.getTrxHashList());

            return new TxPoolImp(txIdToTxMap, mempoolSequence);
        } catch (ResourceAccessException e) {
            throw new TxPoolException("Error: Can't connect to bitcoindClient", e);
        }

    }

    private ConcurrentHashMap<String, Transaction> createTxIdToTxMapFrom(List<String> txIdList) {

        // Map to return
        ConcurrentHashMap<String, Transaction> map = new ConcurrentHashMap<>();

        // If a mempool transaction is dumped beetween bitcoind RPC calls, the
        // transaction is listed here, also it is not added to the ConcurrentHashMap
        Queue<String> txIdsWithError = new ConcurrentLinkedQueue<>();

        // Utility for logging % completed. 1% granularity.
        PercentLog pl = new PercentLog(txIdList.size(), 1);
        // int count = 0;// for PercentLog
        AtomicInteger count = new AtomicInteger(0);

        txIdList.stream().parallel().forEach(txId -> {
            Transaction tx = loadTransaction(txId);
            if (null == tx) {
                txIdsWithError.add(txId);
            } else {
                map.put(txId, tx);
            }
            pl.update(count.incrementAndGet(), percent -> log.info("Querying data for txs... {}", percent));
        });

        if (!txIdsWithError.isEmpty()) {
            log.info("Transactions not found in mempool by rpc race conditions: {}", txIdsWithError);
        }
        return map;
    }

    private Transaction loadTransaction(String txId) {
        GetMemPoolEntry mempoolEntry = bitcoindClient.getMempoolEntry(txId);
        GetVerboseRawTransactionResult rawTx = bitcoindClient.getVerboseRawTransaction(txId);
        if (mempoolEntry.getError() == null && rawTx.getError() == null) {
            Transaction tx = TransactionFactory.from(txId, mempoolEntry.getRawMemPoolEntryData());
            // addAdditionalData does more rpc, add error if any.
            if (!addAdditionalData(tx, rawTx)) {
                return null;
            } else {
                return tx;
            }
        } else {
            return null;
        }
    }

    private boolean addAdditionalData(Transaction tx, GetVerboseRawTransactionResult rawTx) {
        tx.setHex(rawTx.getGetRawTransactionResultData().getHex());

        // Add Txoutputs to transaccions
        rawTx.getGetRawTransactionResultData().getVout().stream().forEach(output -> {
            // JSON preserves order. http://www.rfc-editor.org/rfc/rfc7159.txt
            TxOutput txOutput = new TxOutput();
            txOutput.setAddressIds(output.getScriptPubKey().getAddresses());
            txOutput.setAmount(JSONUtils.jsonToAmount(output.getValue()));
            txOutput.setIndex(output.getN());
            tx.getTxOutputs().add(txOutput);
        });

        // Add Txinputs to transaccions
        for (GetVerboseRawTransactionInput input : rawTx.getGetRawTransactionResultData().getVin()) {

            // obtain transaction output which is spent in current transaction input.
            GetVerboseRawTransactionResult spentRawTx = bitcoindClient.getVerboseRawTransaction(input.getTxid());
            if (null != spentRawTx.getError()) {
                return false;
            } else {
                // JSON preserves order. http://www.rfc-editor.org/rfc/rfc7159.txt
                GetVerboseRawTransactionOutput spentTxOutput = spentRawTx.getGetRawTransactionResultData().getVout()
                        .get(input.getVout());

                TxInput txInput = new TxInput();
                txInput.setAddressIds(spentTxOutput.getScriptPubKey().getAddresses());
                txInput.setAmount(JSONUtils.jsonToAmount(spentTxOutput.getValue()));
                txInput.setTxId(input.getTxid());
                txInput.setVOutIndex(input.getVout());
                txInput.setCoinbase(input.getCoinbase());

                // No need to sort data here.
                tx.getTxInputs().add(txInput);
            }
        }
        // At this point transaction must be correct if not error, we validate it.
        validateTx(tx);
        return true;
    }

    private void validateTx(Transaction tx) {
        Validate.notNull(tx.getTxId(), "txId can't be null");
        Validate.notNull(tx.getTxInputs(), "txInputs can't be null");
        Validate.notNull(tx.getTxOutputs(), "txOutputs can't be null");
        Validate.notNull(tx.getWeight(), "weight can't be null");
        Validate.notNull(tx.getFees(), "Fees object can't be null");
        Validate.notNull(tx.getFees().getBase(), "Fees.base can't be null");
        Validate.notNull(tx.getFees().getModified(), "Fees.modified can't be null");
        Validate.notNull(tx.getFees().getAncestor(), "Fees.ancestor can't be null");
        Validate.notNull(tx.getFees().getDescendant(), "Fees.descendant can't be null");
        Validate.notNull(tx.getTimeInSecs(), "timeInSecs can't be null");
        Validate.notNull(tx.getTxAncestry(), "txAncestry can't be null");
        Validate.notNull(tx.getTxAncestry().getDescendantCount(), "descendantCount can't be null");
        Validate.notNull(tx.getTxAncestry().getDescendantSize(), "descendantSize can't be null");
        Validate.notNull(tx.getTxAncestry().getAncestorCount(), "ancestorCount can't be null");
        Validate.notNull(tx.getTxAncestry().getAncestorSize(), "ancestorSize can't be null");
        Validate.notNull(tx.getTxAncestry().getDepends(), "depends can't be null");
        Validate.notNull(tx.getBip125Replaceable(), "bip125Replaceable can't be null");
        Validate.notEmpty(tx.getHex(), "Hex can't be empty");

        tx.getTxInputs().forEach(input -> {
            if (input.getCoinbase() == null) {
                Validate.notNull(input.getTxId(), "input.txId can't be null");
                Validate.notNull(input.getVOutIndex(), "input.voutIndex can't be null");
                Validate.notNull(input.getAmount(), "input.amount can't be null");
                // Input address could be null in case of unrecognized input scripts
            }
        });

        tx.getTxOutputs().forEach(output -> {
            // addressIds can be null if script is not recognized.
            Validate.notNull(output.getAmount(), "amount can't be null in a TxOutput");
            Validate.notNull(output.getIndex(), "index can't be null in a TxOutput");
        });

    }

    @Override
    public Optional<TxPoolChanges> obtainMemPoolChanges(MempoolSeqEvent event) {
        switch (event.getEvent()) {
            case TXADD:
                return onTxAdd(event.getHash());
            case TXDEL:
                return onTxDel(event.getHash());
            case BLOCKCON:
                return onBlockConnection(event.getHash());
            case BLOCKDIS:
                return onBlockDisconnection(event.getHash());
            default:
                throw new IllegalArgumentException("MempoolSeqEvent invalid");
        }
    }

    private Optional<TxPoolChanges> onTxAdd(String txIdAdd) {
        TxPoolChanges txpc = new TxPoolChanges();
        Transaction txAdd = loadTransaction(txIdAdd);
        // If tx is not in bitcoind, then we are far behind. just ignore it
        if (null == txAdd) {
            return Optional.empty();
        }
        txpc.setChangeTime(Instant.now(clock));
        // Increment only when we don't return an empty optional
        txpc.setChangeCounter(changeCounter.addAndGet(1));
        txpc.getNewTxs().add(txAdd);
        // obtain transaction DAG (see below). This is the set of tx that we must update
        // its ancestry.
        Set<String> txDAG = getTransactionDAG(txAdd);
        addPoolChangesFromAncestry(txpc, txDAG);
        return Optional.of(txpc);
    }

    private Optional<TxPoolChanges> onTxDel(String txIdDel) {
        TxPoolChanges txpc = new TxPoolChanges();
        Transaction txDel = txPoolContainer.getTxPool().getTx(txIdDel);
        if (txDel == null) {
            return Optional.empty();// Already deleted, I think it's not possible, but I don't care
        }
        txpc.setChangeTime(Instant.now(clock));
        // Increment only when we don't return an empty optional
        txpc.setChangeCounter(changeCounter.addAndGet(1));
        txpc.getRemovedTxsId().add(txIdDel);
        // obtain transaction DAG (see below). This is the set of tx that we must update
        // its ancestry.
        Set<String> txDAG = getTransactionDAG(txDel);
        addPoolChangesFromAncestry(txpc, txDAG);
        return Optional.of(txpc);
    }

    private Optional<TxPoolChanges> onBlockConnection(String blockHash) {
        TxPoolChanges txpc = new TxPoolChanges();
        GetBlockResult blockResult = bitcoindClient.getBlock(blockHash);
        if (null != blockResult.getError()) {
            alarmLogger.addAlarm("Bitcoind Cannot find block: " + blockHash);
            log.error("Bitcoind can't find block: {}", blockHash);
            return Optional.empty();
        }
        txpc.setChangeTime(Instant.now(clock));
        // Increment only when we don't return an empty optional
        txpc.setChangeCounter(changeCounter.addAndGet(1));
        Set<String> blockDAGsSet = new HashSet<>();
        List<String> blockTxsList = blockResult.getGetBlockResultData().getTx();
        // Obtain all tx that changes its ancestry
        fillBlockDAGsSet(blockTxsList, blockDAGsSet);
        // add pool changes from tx mined (remove)
        txpc.getRemovedTxsId().addAll(blockTxsList);
        // add pool changes from ancestry changes
        addPoolChangesFromAncestry(txpc, blockDAGsSet);
        return Optional.of(txpc);
    }

    private Optional<TxPoolChanges> onBlockDisconnection(String blockHash) {
        // Fist of all this is not a very frequent case, we send an alarm to see what
        // happened
        alarmLogger.addAlarm("BLOCK DISCONNECTION WITH HASH: " + blockHash);
        TxPoolChanges txpc = new TxPoolChanges();
        GetBlockResult blockResult = bitcoindClient.getBlock(blockHash);
        if (null != blockResult.getError()) {
            alarmLogger.addAlarm("Bitcoind Cannot find block: " + blockHash);
            log.error("Bitcoind can't find block: {}", blockHash);
            return Optional.empty();
        }

        txpc.setChangeTime(Instant.now(clock));
        // Increment only when we don't return an empty optional
        txpc.setChangeCounter(changeCounter.addAndGet(1));
        Set<String> blockDAGsSet = new HashSet<>();
        List<String> blockTxsList = blockResult.getGetBlockResultData().getTx();
        // Obtain all tx that changes its ancestry
        fillBlockDAGsSet(blockTxsList, blockDAGsSet);
        // Add pool changes from tx "un-mined" (add)
        addPoolChangesFromNewTxs(txpc, blockTxsList);
        // add pool changes from ancestry changes
        addPoolChangesFromAncestry(txpc, blockDAGsSet);
        return Optional.of(txpc);
    }

    private void addPoolChangesFromNewTxs(TxPoolChanges txpc, List<String> blockTxsList) {
        for (String txIdInBlock : blockTxsList) {
            Transaction txInBlock = loadTransaction(txIdInBlock);
            if (txIdInBlock == null) {
                log.error("TxId {} in disconnected block, not found", txIdInBlock);
                continue;
            }
            txpc.getNewTxs().add(txInBlock);
        }
    }

    /**
     * Returns all the transactions IN OUR MEMPOOL connected as parents or childrens
     * to the Transaction recursively. That is, the complete DAG (Direct Acyclic
     * Graph) on which the transaction is into. If tx has no dependencies or
     * dependants the returned DAG is empty.
     */
    private Set<String> getTransactionDAG(Transaction seedTx) {

        Set<String> dagSet = new HashSet<>();// The resulting DAG
        Deque<String> txIdStack = new LinkedList<>();// Stack containing txIds to visit
        TxPool txPool = txPoolContainer.getTxPool();

        // Adds initial txs connections.
        txIdStack.addAll(seedTx.getTxAncestry().getDepends());
        txIdStack.addAll(seedTx.getTxAncestry().getSpentby());

        while (!txIdStack.isEmpty()) {
            String txId = txIdStack.pop();
            Transaction tx = txPool.getTx(txId);
            if (tx == null) {
                // This can happen when our mempool is far behind bitcoind's.
                // Continue with no problem, the missing tx will be called eventually.
                continue;
            }
            if (dagSet.add(tx.getTxId())) {
                List<String> depends = tx.getTxAncestry().getDepends();
                List<String> spentBy = tx.getTxAncestry().getSpentby();
                depends.stream().filter(parent -> !dagSet.contains(parent)).forEach(txIdStack::add);
                spentBy.stream().filter(child -> !dagSet.contains(child)).forEach(txIdStack::add);
            }
        }
        return dagSet;
    }

    /**
     * Fills blockDAGsSet with all the transactions IN OUR MEMPOOL connected as
     * parents or childrens to all transactions in a block (recursively). That is,
     * the sets of complete DAGs (Direct Acyclic Graph) on which all block
     * transactions are into. If all txs has no dependencies or dependants the
     * returned DAG is empty (maybe on empty blocks).
     * 
     * This method it's equivalent of invoking getTransactionDAG for each tx in a
     * block and then store all tx in a superset, we make a new method with other
     * signature for eficiency
     */
    private void fillBlockDAGsSet(List<String> txsList, Set<String> blockDAGsSet) {
        Deque<String> txIdStack = new LinkedList<>();// Stack containing txIds to visit
        TxPool txPool = txPoolContainer.getTxPool();

        for (String txIdInBlock : txsList) {

            Transaction txInBlock = txPool.getTx(txIdInBlock);
            if (txInBlock == null) {
                // This (mined/unmined) tx is not in our mempool
                continue;
            }

            // Adds initial txs connections.
            txIdStack.addAll(txInBlock.getTxAncestry().getDepends());
            txIdStack.addAll(txInBlock.getTxAncestry().getSpentby());

            while (!txIdStack.isEmpty()) {
                String txId = txIdStack.pop();
                Transaction tx = txPool.getTx(txId);
                if (tx == null) {
                    // This can happen when our mempool is far behind bitcoind's.
                    // Continue with no problem, the missing tx will be called eventually.
                    continue;
                }
                if (blockDAGsSet.add(tx.getTxId())) {
                    List<String> depends = tx.getTxAncestry().getDepends();
                    List<String> spentBy = tx.getTxAncestry().getSpentby();
                    depends.stream().filter(parent -> !blockDAGsSet.contains(parent)).forEach(txIdStack::add);
                    spentBy.stream().filter(child -> !blockDAGsSet.contains(child)).forEach(txIdStack::add);
                }
            }
        }
    }

    /**
     * Given a set of txIds (txDAG) query for ancestry changes in bitcoind, if
     * any,add it to txpc
     * 
     * @param txpc
     * @param txDAG
     */
    private void addPoolChangesFromAncestry(TxPoolChanges txpc, Set<String> txDAG) {
        for (String txId : txDAG) {
            Transaction tx = txPoolContainer.getTxPool().getTx(txId);
            if (null == tx) {
                continue;// I think it's not possible, but I don't care
            }
            GetMemPoolEntry mempoolEntry = bitcoindClient.getMempoolEntry(txId);
            if (mempoolEntry.getError() != null) {
                continue;// As above, ignore if not in bitcoind's mempool
            }
            RawMemPoolEntryData rawMemPoolEntryData = mempoolEntry.getRawMemPoolEntryData();
            if (ancestryHasChanged(tx, rawMemPoolEntryData)) {
                txpc.getTxAncestryChangesMap().put(txId, TxAncestryChangesFactory.from(rawMemPoolEntryData));
            }
        }
    }

    private boolean ancestryHasChanged(Transaction tx, RawMemPoolEntryData rawMemPoolEntryData) {
        TxAncestry txa = tx.getTxAncestry();
        Fees txf = tx.getFees();
        return (!txa.getDescendantCount().equals(rawMemPoolEntryData.getDescendantcount()))
                || (!txa.getDescendantSize().equals(rawMemPoolEntryData.getDescendantsize()))
                || (!txa.getAncestorCount().equals(rawMemPoolEntryData.getAncestorcount()))
                || (!txa.getAncestorSize().equals(rawMemPoolEntryData.getAncestorsize()))
                || (!txa.getDepends().equals(rawMemPoolEntryData.getDepends()))
                || (!txa.getSpentby().equals(rawMemPoolEntryData.getSpentby()))
                || (!txf.getAncestor().equals(JSONUtils.jsonToAmount(rawMemPoolEntryData.getFees().getAncestor())))
                || (!txf.getDescendant().equals(JSONUtils.jsonToAmount(rawMemPoolEntryData.getFees().getDescendant())));
    }

}
