package com.mempoolexplorer.bitcoind.adapter.components.factories;

import java.util.Optional;

import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;
import com.mempoolexplorer.bitcoind.adapter.threads.MempoolSeqEvent;

public interface TxPoolFiller {
    /**
     * Creates a mempool by querying bitcoind for all tx in mempool.
     * 
     * @return
     * @throws TxPoolException
     */
    TxPool createMemPool() throws TxPoolException;

    /**
     * Obtain mempool changes from a {@link MempoolSeqEvent} (txadd,txdel,blockcon o
     * blockdis). This mempool changes includes ancestry changes due to transaction
     * dependencies.
     * 
     * @param event
     * @return
     */
    Optional<TxPoolChanges> obtainMemPoolChanges(MempoolSeqEvent event);
}
