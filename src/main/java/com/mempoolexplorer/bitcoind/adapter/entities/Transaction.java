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
	private Integer size;// In bytes
	private Integer vSize;// In bytes
	private Long fee;
	private Double satBytes;
	private Long timeInSecs;// Epoch time in seconds since the transaction entered.
	private Integer descendantCount;// The number of in-mempool descendant transactions (including this one)
	private Integer descendantSize;// The size of in-mempool descendants (including this one)
	private Integer descendantFees;// The modified fees (see modifiedfee above) of in-mempool descendants
	// (including this one)
	private Integer ancestorCount;// The number of in-mempool ancestor transactions (including this one)
	private Integer ancestorSize;// The size of in-mempool ancestors (including this one)
	private Integer ancestorFees;// The modified fees (see modifiedfee above) of in-mempool ancestors (including
									// this one)
	private List<String> depends = new ArrayList<>();// An array holding TXIDs of unconfirmed transactions (encoded as
														// hex in
	// RPC

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public void setTxInputs(List<TxInput> txInputs) {
		this.txInputs = txInputs;
	}

	public void setTxOutputs(List<TxOutput> txOutputs) {
		this.txOutputs = txOutputs;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public void setvSize(Integer vSize) {
		this.vSize = vSize;
	}

	public void setFee(Long fee) {
		this.fee = fee;
	}

	public void setSatBytes(Double satBytes) {
		this.satBytes = satBytes;
	}

	public void setTimeInSecs(Long timeInSecs) {
		this.timeInSecs = timeInSecs;
	}

	public void setDescendantCount(Integer descendantCount) {
		this.descendantCount = descendantCount;
	}

	public void setDescendantSize(Integer descendantSize) {
		this.descendantSize = descendantSize;
	}

	public void setDescendantFees(Integer descendantFees) {
		this.descendantFees = descendantFees;
	}

	public void setAncestorCount(Integer ancestorCount) {
		this.ancestorCount = ancestorCount;
	}

	public void setAncestorSize(Integer ancestorSize) {
		this.ancestorSize = ancestorSize;
	}

	public void setAncestorFees(Integer ancestorFees) {
		this.ancestorFees = ancestorFees;
	}

	public void setDepends(List<String> depends) {
		this.depends = depends;
	}

	public String getTxId() {
		return txId;
	}

	public List<TxInput> getTxInputs() {
		return txInputs;
	}

	public List<TxOutput> getTxOutputs() {
		return txOutputs;
	}

	public Integer getSize() {
		return size;
	}

	public Integer getvSize() {
		return vSize;
	}

	public Long getFee() {
		return fee;
	}

	public Double getSatBytes() {
		return satBytes;
	}

	public Long getTimeInSecs() {
		return timeInSecs;
	}

	public Integer getDescendantCount() {
		return descendantCount;
	}

	public Integer getDescendantSize() {
		return descendantSize;
	}

	public Integer getDescendantFees() {
		return descendantFees;
	}

	public Integer getAncestorCount() {
		return ancestorCount;
	}

	public Integer getAncestorSize() {
		return ancestorSize;
	}

	public Integer getAncestorFees() {
		return ancestorFees;
	}

	public List<String> getDepends() {
		return depends;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Transaction [txId=");
		builder.append(txId);
		builder.append(", txInputs=");
		builder.append(txInputs);
		builder.append(", txOutputs=");
		builder.append(txOutputs);
		builder.append(", size=");
		builder.append(size);
		builder.append(", vSize=");
		builder.append(vSize);
		builder.append(", fee=");
		builder.append(fee);
		builder.append(", satBytes=");
		builder.append(satBytes);
		builder.append(", timeInSecs=");
		builder.append(timeInSecs);
		builder.append(", descendantCount=");
		builder.append(descendantCount);
		builder.append(", descendantSize=");
		builder.append(descendantSize);
		builder.append(", descendantFees=");
		builder.append(descendantFees);
		builder.append(", ancestorCount=");
		builder.append(ancestorCount);
		builder.append(", ancestorSize=");
		builder.append(ancestorSize);
		builder.append(", ancestorFees=");
		builder.append(ancestorFees);
		builder.append(", depends=");
		builder.append(depends);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((txId == null) ? 0 : txId.hashCode());
		return result;
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
