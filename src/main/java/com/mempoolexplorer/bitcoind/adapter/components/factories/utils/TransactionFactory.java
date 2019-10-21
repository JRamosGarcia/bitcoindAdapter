package com.mempoolexplorer.bitcoind.adapter.components.factories.utils;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolVerboseData;
import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.utils.JSONUtils;

public class TransactionFactory {

	public static Transaction from(String txId, GetRawMemPoolVerboseData getRawMemPoolVerboseData) {

		Transaction tx = new Transaction();
		tx.setTxId(txId);
		tx.setAncestorCount(getRawMemPoolVerboseData.getAncestorcount());
		tx.setAncestorFees(getRawMemPoolVerboseData.getAncestorfees());
		tx.setAncestorSize(getRawMemPoolVerboseData.getAncestorsize());
		tx.setDepends(getRawMemPoolVerboseData.getDepends());
		tx.setDescendantCount(getRawMemPoolVerboseData.getDescendantcount());
		tx.setDescendantFees(getRawMemPoolVerboseData.getDescendantfees());
		tx.setDescendantSize(getRawMemPoolVerboseData.getDescendantsize());
		tx.setFee(JSONUtils.JSONtoAmount(getRawMemPoolVerboseData.getFee()));
		tx.setSize(getRawMemPoolVerboseData.getSize());
		tx.setTimeInSecs(getRawMemPoolVerboseData.getTime());
		// Estos datos se tienen que rellenar via query a getRawTransaction.
		// tx.setTxInputs(txInputs);
		// tx.setTxOutputs(txOutputs);
		// tx.setSatBytes(satBytes);
		// tx.setvSize(vSize);
		return tx;
	}

}
