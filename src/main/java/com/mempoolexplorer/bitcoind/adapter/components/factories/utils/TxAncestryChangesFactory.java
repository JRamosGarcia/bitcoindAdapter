package com.mempoolexplorer.bitcoind.adapter.components.factories.utils;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.RawMemPoolEntryData;
import com.mempoolexplorer.bitcoind.adapter.entities.Fees;
import com.mempoolexplorer.bitcoind.adapter.entities.TxAncestry;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxAncestryChanges;
import com.mempoolexplorer.bitcoind.adapter.utils.JSONUtils;

public class TxAncestryChangesFactory {

	private TxAncestryChangesFactory() {
		throw new IllegalStateException("Can't instantiate utility class");
	}

	public static TxAncestryChanges from(RawMemPoolEntryData rawMemPoolEntryData) {
		Fees fees = new Fees();
		fees.setAncestor(JSONUtils.jsonToAmount(rawMemPoolEntryData.getFees().getAncestor()));
		fees.setBase(JSONUtils.jsonToAmount(rawMemPoolEntryData.getFees().getBase()));
		fees.setDescendant(JSONUtils.jsonToAmount(rawMemPoolEntryData.getFees().getDescendant()));
		fees.setModified(JSONUtils.jsonToAmount(rawMemPoolEntryData.getFees().getModified()));
		TxAncestry txAncestry = new TxAncestry();
		txAncestry.setAncestorCount(rawMemPoolEntryData.getAncestorcount());
		txAncestry.setAncestorSize(rawMemPoolEntryData.getAncestorsize());
		txAncestry.setDepends(rawMemPoolEntryData.getDepends());
		txAncestry.setDescendantCount(rawMemPoolEntryData.getDescendantcount());
		txAncestry.setDescendantSize(rawMemPoolEntryData.getDescendantsize());
		txAncestry.setSpentby(rawMemPoolEntryData.getSpentby());
		return new TxAncestryChanges(fees, txAncestry);
	}
}
