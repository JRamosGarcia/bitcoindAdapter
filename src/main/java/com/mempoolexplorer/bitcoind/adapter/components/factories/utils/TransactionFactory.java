package com.mempoolexplorer.bitcoind.adapter.components.factories.utils;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolVerboseData;
import com.mempoolexplorer.bitcoind.adapter.entities.Fees;
import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.utils.JSONUtils;

public class TransactionFactory {

	public static Transaction from(String txId, GetRawMemPoolVerboseData getRawMemPoolVerboseData) {

		Transaction tx = new Transaction();
		tx.setTxId(txId);
		tx.setAncestorCount(getRawMemPoolVerboseData.getAncestorcount());
		tx.setAncestorSize(getRawMemPoolVerboseData.getAncestorsize());
		tx.setDepends(getRawMemPoolVerboseData.getDepends());
		tx.setDescendantCount(getRawMemPoolVerboseData.getDescendantcount());
		tx.setDescendantSize(getRawMemPoolVerboseData.getDescendantsize());
		tx.setSize(getRawMemPoolVerboseData.getSize());
		tx.setTimeInSecs(getRawMemPoolVerboseData.getTime());
		
		Fees fees = new Fees();
		fees.setBase(JSONUtils.JSONtoAmount(getRawMemPoolVerboseData.getFees().getBase()));
		fees.setModified(JSONUtils.JSONtoAmount(getRawMemPoolVerboseData.getFees().getModified()));
		fees.setAncestor(JSONUtils.JSONtoAmount(getRawMemPoolVerboseData.getFees().getAncestor()));
		fees.setDescendant(JSONUtils.JSONtoAmount(getRawMemPoolVerboseData.getFees().getDescendant()));
		tx.setFees(fees);
		
		// Estos datos se tienen que rellenar via query a getRawTransaction.
		// tx.setTxInputs(txInputs);
		// tx.setTxOutputs(txOutputs);
		// tx.setSatBytes(satBytes);
		// tx.setvSize(vSize);
		return tx;
	}

}
