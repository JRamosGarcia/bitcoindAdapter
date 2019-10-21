package com.mempoolexplorer.bitcoind.adapter.entities.mempool;

public class TxPoolDiff {

	private TxPool goneOrMinedMemPool;
	private TxPool newMemPool;

	public TxPoolDiff(TxPool goneOrMinedMemPool, TxPool newMemPool) {
		super();
		this.goneOrMinedMemPool = goneOrMinedMemPool;
		this.newMemPool = newMemPool;
	}

	public TxPool getGoneOrMinedMemPool() {
		return goneOrMinedMemPool;
	}

	public TxPool getNewMemPool() {
		return newMemPool;
	}

	public Boolean hasNoChanges() {
		if ((goneOrMinedMemPool.getSize() == 0) && (newMemPool.getSize() == 0)) {
			return true;
		}
		return false;
	}

	public Boolean hasChanges() {
		return !hasNoChanges();
	}

}
