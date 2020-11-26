package com.mempoolexplorer.bitcoind.adapter.components.factories;

import java.util.Optional;

import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.Block;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;
import com.mempoolexplorer.bitcoind.adapter.threads.MempoolSeqEvent;

import org.apache.commons.lang3.tuple.Pair;

public interface TxPoolFiller {
    /**
     * Creates a mempool by querying bitcoind for all tx in mempool.
     * 
     * @return
     * @throws TxPoolException
     */
    TxPool createMemPool() throws TxPoolException;

    /**
     * Obtain mempool changes from a {@link MempoolSeqEvent} (txadd or txdel). This
     * mempool changes includes ancestry changes due to transaction dependencies.
     * 
     * @param event
     * @return
     */
    Optional<TxPoolChanges> obtainOnTxMemPoolChanges(MempoolSeqEvent event);

    /**
     * Obtain mempool changes from a {@link MempoolSeqEvent} (blockcon or blockdis).
     * This mempool changes includes ancestry changes due to transaction
     * dependencies whithin the block.
     * 
     * @param event
     * @return
     */
    Optional<Pair<TxPoolChanges, Block>> obtainOnBlockMemPoolChanges(MempoolSeqEvent event);

    /**
     * Resets TxPoolChanges incremental counter to 0. This provokes that services
     * depending on bitcoindAdapter through kafka will reset it's mempool also.
     */
    void resetChangeCounter();
}
