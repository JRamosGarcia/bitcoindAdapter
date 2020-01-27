package com.mempoolexplorer.bitcoind.adapter.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "txs")
public class Transaction {
	@Id
	private String txId;
	private List<TxInput> txInputs = new ArrayList<>();
	private List<TxOutput> txOutputs = new ArrayList<>();
	private Integer weight;// for SegWit
	// BE CAREFUL: THIS FIELD MUST KEPT UPDATED, COULD CHANGE ONCE RECEIVED!!!!
	private Fees fees;
	private Long timeInSecs;// Epoch time in seconds since the transaction entered.
	// BE CAREFUL: THIS FIELD MUST KEPT UPDATED, COULD CHANGE ONCE RECEIVED!!!!
	private TxAncestry txAncestry;
	private Boolean bip125Replaceable;
	private String hex;// Raw transaction in hexadecimal

	public double getvSize() {
		return weight / 4.0D;
	}

	/**
	 * Returns all addresses involved in this transaction, address in inputs,
	 * outputs and duplicated.
	 * 
	 */
	public List<String> listAddresses() {
		List<String> txInputsAddresses = txInputs.stream().map(txInput -> txInput.getAddressIds())
				.flatMap(addresses -> addresses.stream()).collect(Collectors.toList());
		return txOutputs.stream().map(txOutput -> txOutput.getAddressIds()).flatMap(addresses -> addresses.stream())
				.collect(Collectors.toCollection(() -> txInputsAddresses));
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public List<TxInput> getTxInputs() {
		return txInputs;
	}

	public void setTxInputs(List<TxInput> txInputs) {
		this.txInputs = txInputs;
	}

	public List<TxOutput> getTxOutputs() {
		return txOutputs;
	}

	public void setTxOutputs(List<TxOutput> txOutputs) {
		this.txOutputs = txOutputs;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Fees getFees() {
		return fees;
	}

	public void setFees(Fees fees) {
		this.fees = fees;
	}

	public Long getTimeInSecs() {
		return timeInSecs;
	}

	public void setTimeInSecs(Long timeInSecs) {
		this.timeInSecs = timeInSecs;
	}

	public TxAncestry getTxAncestry() {
		return txAncestry;
	}

	public void setTxAncestry(TxAncestry txAncestry) {
		this.txAncestry = txAncestry;
	}

	public Boolean getBip125Replaceable() {
		return bip125Replaceable;
	}

	public void setBip125Replaceable(Boolean bip125Replaceable) {
		this.bip125Replaceable = bip125Replaceable;
	}

	public String getHex() {
		return hex;
	}

	public void setHex(String hex) {
		this.hex = hex;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Transaction [txId=");
		builder.append(txId);
		builder.append(", txInputs=");
		builder.append(txInputs);
		builder.append(", txOutputs=");
		builder.append(txOutputs);
		builder.append(", weight=");
		builder.append(weight);
		builder.append(", vSize=");
		builder.append(getvSize());
		builder.append(", fees=");
		builder.append(fees);
		builder.append(", timeInSecs=");
		builder.append(timeInSecs);
		builder.append(", txAncestry=");
		builder.append(txAncestry);
		builder.append(", bip125Replaceable=");
		builder.append(bip125Replaceable);
		builder.append(", hex=");
		builder.append(hex);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return txId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (txId == null) {
			if (other.txId != null)
				return false;
		} else if (!txId.equals(other.txId))
			return false;
		return true;
	}
}
