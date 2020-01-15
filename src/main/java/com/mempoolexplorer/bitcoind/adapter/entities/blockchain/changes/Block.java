package com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Block {
	private Instant changeTime;// This time is set by us
	private String hash;
	private Integer height;
	private Integer weight;// up to 4 millions (sum of vSize*4)
	private Instant minedTime;// This time is set by miners. Can be in the future!
	private Instant medianMinedTime;// This time always increases with respect height

	private List<String> txIds = new ArrayList<>();

	private String coinBaseTxId;// also in txIds but not in notInMemPoolTransactions
	private String coinBaseField;
	private Map<String, NotInMemPoolTx> notInMemPoolTransactions = new HashMap<>();

	public Instant getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(Instant changeTime) {
		this.changeTime = changeTime;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Instant getMinedTime() {
		return minedTime;
	}

	public void setMinedTime(Instant minedTime) {
		this.minedTime = minedTime;
	}

	public Instant getMedianMinedTime() {
		return medianMinedTime;
	}

	public void setMedianMinedTime(Instant medianMinedTime) {
		this.medianMinedTime = medianMinedTime;
	}

	public String getCoinBaseTxId() {
		return coinBaseTxId;
	}

	public void setCoinBaseTxId(String coinBaseTxId) {
		this.coinBaseTxId = coinBaseTxId;
	}

	public String getCoinBaseField() {
		return coinBaseField;
	}

	public void setCoinBaseField(String coinBaseField) {
		this.coinBaseField = coinBaseField;
	}

	public Map<String, NotInMemPoolTx> getNotInMemPoolTransactions() {
		return notInMemPoolTransactions;
	}

	public void setNotInMemPoolTransactions(Map<String, NotInMemPoolTx> notInMemPoolTransactions) {
		this.notInMemPoolTransactions = notInMemPoolTransactions;
	}

	public void setTxIds(List<String> txIds) {
		this.txIds = txIds;
	}

	public List<String> getTxIds() {
		return txIds;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Block [changeTime=");
		builder.append(changeTime);
		builder.append(", hash=");
		builder.append(hash);
		builder.append(", height=");
		builder.append(height);
		builder.append(", weight=");
		builder.append(weight);
		builder.append(", minedTime=");
		builder.append(minedTime);
		builder.append(", medianMinedTime=");
		builder.append(medianMinedTime);
		builder.append(", txIds=");
		builder.append(txIds);
		builder.append(", coinBaseTxId=");
		builder.append(coinBaseTxId);
		builder.append(", coinBaseField=");
		builder.append(coinBaseField);
		builder.append(", notInMemPoolTransactions=");
		builder.append(notInMemPoolTransactions);
		builder.append("]");
		return builder.toString();
	}

}
