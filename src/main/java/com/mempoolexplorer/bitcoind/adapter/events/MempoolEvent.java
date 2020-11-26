package com.mempoolexplorer.bitcoind.adapter.events;

import java.util.Optional;

import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.Block;
import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplateChanges;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

import lombok.Getter;

/**
 * This class is an union of Block and TxPoolChanges since is meant to be used
 * as a kafka message for same topic conserving message order. The topic is a
 * "mempool event"
 */
@Getter
public class MempoolEvent {
	//
	public enum EventType {
		NEW_BLOCK, REFRESH_POOL
	}

	private EventType eventType;
	private Block block;
	private TxPoolChanges txPoolChanges;
	private BlockTemplateChanges blockTemplateChanges;

	private MempoolEvent() {
	}

	public static MempoolEvent createFrom(TxPoolChanges txPoolChanges,
			Optional<BlockTemplateChanges> blockTemplateChanges) {
		MempoolEvent mpe = new MempoolEvent();
		mpe.eventType = EventType.REFRESH_POOL;
		mpe.txPoolChanges = txPoolChanges;
		mpe.blockTemplateChanges = blockTemplateChanges.orElse(null);
		return mpe;
	}

	public static MempoolEvent createFrom(Block block) {
		MempoolEvent mpe = new MempoolEvent();
		mpe.eventType = EventType.NEW_BLOCK;
		mpe.block = block;
		return mpe;
	}

}
