package com.mempoolexplorer.bitcoind.adapter.components.factories.utils;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.RawMemPoolEntryData;
import com.mempoolexplorer.bitcoind.adapter.entities.Fees;
import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.TxAncestry;
import com.mempoolexplorer.bitcoind.adapter.utils.JSONUtils;

public class TransactionFactory {

	public static Transaction from(String txId, RawMemPoolEntryData getRawMemPoolVerboseData) {

		Transaction tx = new Transaction();
		tx.setTxId(txId);
		TxAncestry txa = new TxAncestry();

		txa.setAncestorCount(getRawMemPoolVerboseData.getAncestorcount());
		txa.setAncestorSize(getRawMemPoolVerboseData.getAncestorsize());
		txa.setDepends(getRawMemPoolVerboseData.getDepends());
		txa.setSpentby(getRawMemPoolVerboseData.getSpentby());
		txa.setDescendantCount(getRawMemPoolVerboseData.getDescendantcount());
		txa.setDescendantSize(getRawMemPoolVerboseData.getDescendantsize());

		tx.setTxAncestry(txa);
		tx.setSize(getRawMemPoolVerboseData.getSize());
		tx.setTimeInSecs(getRawMemPoolVerboseData.getTime());
		tx.setBip125Replaceable(getRawMemPoolVerboseData.getBip125Replaceable());

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
