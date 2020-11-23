package com.mempoolexplorer.bitcoind.adapter.components.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetMemPoolEntry;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetMemPoolInfo;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolNonVerbose;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolNonVerboseData;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionInput;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionOutput;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionResult;
import com.mempoolexplorer.bitcoind.adapter.components.clients.BitcoindClient;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.components.factories.utils.TransactionFactory;
import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
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
    BitcoindClient bitcoindClient;

    @Override
    @ProfileTime(metricName = ProfileMetricNames.MEMPOOL_INITIAL_CREATION_TIME)
    public TxPool createMemPool() throws TxPoolException {
        try {
            GetMemPoolInfo memPoolInfo = bitcoindClient.getMemPoolInfo();
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
        List<String> txIdsWithError = new ArrayList<>();

        // Utility for logging % completed. 1% granularity.
        PercentLog pl = new PercentLog(txIdList.size(), 1);
        int count = 0;// for PercentLog

        for (String txId : txIdList) {
            GetMemPoolEntry mempoolEntry = bitcoindClient.getMempoolEntry(txId);
            GetVerboseRawTransactionResult rawTx = bitcoindClient.getVerboseRawTransaction(txId);
            if (mempoolEntry.getError() == null && rawTx.getError() == null) {
                Transaction tx = TransactionFactory.from(txId, mempoolEntry.getRawMemPoolEntryData());
                // addAdditionalData does more rpc, add error if any.
                if (!addAdditionalData(tx, rawTx)) {
                    txIdsWithError.add(txId);
                } else {
                    map.put(txId, tx);
                }
            } else {
                txIdsWithError.add(txId);
            }
            pl.update(count++, percent -> log.info("Querying data for txs... {}", percent));
        }

        if (!txIdsWithError.isEmpty()) {
            log.info("Transactions not found in mempool by rpc race conditions: {}", txIdsWithError);
        }
        return map;
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
    public TxPoolChanges obtainMemPoolChanges(MempoolSeqEvent event) {
        // TODO: Continue here!!!!!!!!!!!
        return new TxPoolChanges();
    }

}
