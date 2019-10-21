package com.mempoolexplorer.bitcoind.adapter.entities;

import java.util.List;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.ImmutableList;

/**
 * Immutable class representing TxOutput
 * 
 * @author tomillo
 *
 */
public final class ImmutableTxOutput {

	private final ImmutableList<String> addressIds;// Several addresses if P2PSH, none if unrecognized script.
	private final Long amount;// In Satoshis.
	private final Integer index;// Begins in 0

	private ImmutableTxOutput(List<String> addressIds, Integer index, Long amount) {
		// If addressIds is null due to script not recognized avoid null Collections
		if (null == addressIds) {
			this.addressIds = ImmutableList.of();
		} else {
			this.addressIds = ImmutableList.copyOf(addressIds);
		}
		this.index = index;
		this.amount = amount;
	}

	public static class Builder {

		private List<String> addressIds;// Several addresses if P2PSH, none if unrecognized script.
		private Long amount;// In Satoshis.
		private Integer index;// Begins in 0

		public Builder addressIds(List<String> addressIds) {
			this.addressIds = addressIds;
			return this;
		}

		public Builder amount(Long amount) {
			this.amount = amount;
			return this;
		}

		public Builder index(Integer index) {
			this.index = index;
			return this;
		}

		public ImmutableTxOutput build() {
			// addressIds can be null if script is not recognized.
			Validate.notNull(amount, "amount can't be null in a TxOutput");
			Validate.notNull(index, "index can't be null in a TxOutput");
			return new ImmutableTxOutput(addressIds, index, amount);
		}
	}

	public ImmutableList<String> getAddressIds() {
		return addressIds;
	}

	public Long getAmount() {
		return amount;
	}

	public Integer getIndex() {
		return index;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TxOutput [addressIds=");
		builder.append(addressIds);
		builder.append(", amount=");
		builder.append(amount);
		builder.append(", index=");
		builder.append(index);
		builder.append("]");
		return builder.toString();
	}

}
