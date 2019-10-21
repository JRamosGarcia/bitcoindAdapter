package com.mempoolexplorer.bitcoind.adapter.entities;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Immutable class representing TxInput
 * 
 * @author tomillo
 *
 */
public final class ImmutableTxInput {

	private final ImmutableList<String> addressIds;// Several addresses if P2PSH, none if unrecognized script. or
													// coinBase transaction
	private final Long amount;// In Satoshis.
	private final String txId;// Transaction where output is being spent by this input
	private final Integer vOutIndex;// The output index number (vout) of the outpoint being spent. The first output
	// in a transaction has an index of 0. Not present if this is a coinbase
	// transaction

	private ImmutableTxInput(List<String> addressIds, Long amount, String txId, Integer vOutIndex) {
		// If addressIds is null due to script not recognized or coinbase Tx +savoid
		// null
		// Collections
		if (null == addressIds) {
			this.addressIds = ImmutableList.of();
		} else {
			this.addressIds = ImmutableList.copyOf(addressIds);
		}

		this.amount = amount;
		this.txId = txId;
		this.vOutIndex = vOutIndex;
	}

	public static class Builder {
		public Builder() {
		}

		private List<String> addressIds;// Several addresses if P2PSH, none if unrecognized script.
		private Long amount;// In Satoshis.
		private String txId;// Transaction where output is being spent by this input
		private Integer vOutIndex;// The output index number (vout) of the outpoint being spent. The first
									// output

		public Builder addressIds(List<String> addressIds) {
			this.addressIds = addressIds;
			return this;
		}

		public Builder amount(Long amount) {
			this.amount = amount;
			return this;
		}

		public Builder txId(String txId) {
			this.txId = txId;
			return this;
		}

		public Builder vOutIndex(Integer vOutIndex) {
			this.vOutIndex = vOutIndex;
			return this;
		}

		public ImmutableTxInput build() {
			// We don't validate for not nulls since all fields can be null if it's a
			// coinbase transaction.
			return new ImmutableTxInput(addressIds, amount, txId, vOutIndex);
		}

	}

	public ImmutableList<String> getAddressIds() {
		return addressIds;
	}

	public Long getAmount() {
		return amount;
	}

	public String getTxId() {
		return txId;
	}

	public Integer getvOutIndex() {
		return vOutIndex;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TxInput [addressIds=");
		builder.append(addressIds);
		builder.append(", amount=");
		builder.append(amount);
		builder.append(", txId=");
		builder.append(txId);
		builder.append(", vOutIndex=");
		builder.append(vOutIndex);
		builder.append("]");
		return builder.toString();
	}

}
