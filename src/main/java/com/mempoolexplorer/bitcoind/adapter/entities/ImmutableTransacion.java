package com.mempoolexplorer.bitcoind.adapter.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.ImmutableList;

public final class ImmutableTransacion {

	private final String txId;
	//TODO: Hay que quitar las listas inmutables de guava por las de java y quitar la librería guava como dependencia.
	private final ImmutableList<ImmutableTxInput> txInputs;
	private final ImmutableList<ImmutableTxOutput> txOutputs;
	private final Integer size;// In bytes
	private final Integer vSize;// In bytes
	private final Long fee;
	private final Double satBytes;
	private final Long timeInSecs;
	private final Integer descendantcount;// The number of in-mempool descendant transactions (including this one)
	private final Integer descendantsize;// The size of in-mempool descendants (including this one)
	private final Integer descendantfees;// The modified fees (see modifiedfee above) of in-mempool descendants
	// (including this one)
	private final Integer ancestorcount;// The number of in-mempool ancestor transactions (including this one)
	private final Integer ancestorsize;// The size of in-mempool ancestors (including this one)
	private final Integer ancestorfees;// The modified fees (see modifiedfee above) of in-mempool ancestors (including
										// this one)
	private final ImmutableList<String> depends;// An array holding TXIDs of unconfirmed transactions (encoded as hex in
												// RPC

	private ImmutableTransacion(String txId, List<ImmutableTxInput> txInputs, List<ImmutableTxOutput> txOutputs,
			Integer size, Integer vSize, Long fee, Double satBytes, Long timeInSecs, Integer descendantcount,
			Integer descendantsize, Integer descendantfees, Integer ancestorcount, Integer ancestorsize,
			Integer ancestorfees, List<String> depends) {
		super();
		this.txId = txId;
		this.txInputs = ImmutableList.copyOf(txInputs);
		this.txOutputs = ImmutableList.copyOf(txOutputs);
		this.size = size;
		this.vSize = vSize;
		this.fee = fee;
		this.satBytes = satBytes;
		this.timeInSecs = timeInSecs;
		this.descendantcount = descendantcount;
		this.descendantsize = descendantsize;
		this.descendantfees = descendantfees;
		this.ancestorcount = ancestorcount;
		this.ancestorsize = ancestorsize;
		this.ancestorfees = ancestorfees;
		this.depends = ImmutableList.copyOf(depends);
	}

	public static class Builder {

		private String txId;
		private List<ImmutableTxInput> txInputs = new ArrayList<>();
		private List<ImmutableTxOutput> txOutputs = new ArrayList<>();
		private Integer size;// In bytes
		private Integer vSize;// In bytes
		private Long fee;
		private Double satBytes;// Redundant but convenient (vSize/fee)
		private Long timeInSecs;
		private Integer descendantCount;
		private Integer descendantSize;
		private Integer descendantFees;
		private Integer ancestorCount;
		private Integer ancestorSize;
		private Integer ancestorFees;
		private List<String> depends = new ArrayList<>();

		public Builder txId(String txId) {
			this.txId = txId;
			return this;
		}

		public Builder txInputs(List<ImmutableTxInput> txInputs) {
			this.txInputs = txInputs;
			return this;
		}

		public Builder txOutputs(List<ImmutableTxOutput> txOutputs) {
			this.txOutputs = txOutputs;
			return this;
		}

		public Builder size(Integer size) {
			this.size = size;
			return this;
		}

		public Builder vSize(Integer vSize) {
			this.vSize = vSize;
			return this;
		}

		public Builder fee(Long fee) {
			this.fee = fee;
			return this;
		}

		public Builder satBytes(Double satBytes) {
			this.satBytes = satBytes;
			return this;
		}

		public Builder timeInSecs(Long timeInSecs) {
			this.timeInSecs = timeInSecs;
			return this;
		}

		public Builder descendantCount(Integer descendantCount) {
			this.descendantCount = descendantCount;
			return this;
		}

		public Builder descendantSize(Integer descendantSize) {
			this.descendantSize = descendantSize;
			return this;
		}

		public Builder descendantFees(Integer descendantFees) {
			this.descendantFees = descendantFees;
			return this;
		}

		public Builder ancestorCount(Integer ancestorCount) {
			this.ancestorCount = ancestorCount;
			return this;
		}

		public Builder ancestorSize(Integer ancestorSize) {
			this.ancestorSize = ancestorSize;
			return this;
		}

		public Builder ancestorFees(Integer ancestorFees) {
			this.ancestorFees = ancestorFees;
			return this;
		}

		public Builder depends(List<String> depends) {
			this.depends = depends;
			return this;
		}

		public String getTxId() {
			return this.txId;
		}

		public List<ImmutableTxInput> getTxInputs() {
			return txInputs;
		}

		public List<ImmutableTxOutput> getTxOutputs() {
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

		public ImmutableTransacion build() {
			
			//TODO: Utilizar otra librería para validar y eliminar apache-commons-lang3 de las dependencias del proyecto.
			Validate.notNull(txId, "txId can't be null");
			Validate.notNull(txInputs, "txInputs can't be null");
			Validate.notNull(txOutputs, "txOutputs can't be null");
			Validate.notNull(size, "size can't be null");
			Validate.notNull(vSize, "vsize can't be null");
			Validate.notNull(fee, "fee can't be null");
			Validate.notNull(satBytes, "satBytes can't be null");
			Validate.notNull(timeInSecs, "timeInSecs can't be null");
			Validate.notNull(descendantCount, "descendantCount can't be null");
			Validate.notNull(descendantSize, "descendantSize can't be null");
			Validate.notNull(descendantFees, "descendantFees can't be null");
			Validate.notNull(ancestorCount, "ancestorCount can't be null");
			Validate.notNull(ancestorSize, "ancestorSize can't be null");
			Validate.notNull(ancestorFees, "ancestorFees can't be null");
			Validate.notNull(depends, "depends can't be null");

			Validate.notEmpty(txInputs, "txInputs can't be empty");
			Validate.notEmpty(txOutputs, "txOutputs can't be empty");

			return new ImmutableTransacion(txId, txInputs, txOutputs, size, vSize, fee, satBytes, timeInSecs,
					descendantCount, descendantSize, descendantFees, ancestorCount, ancestorSize, ancestorFees,
					depends);

		}
	}

	public String getTxId() {
		return txId;
	}

	public ImmutableList<ImmutableTxInput> getTxInputs() {
		return txInputs;
	}

	public ImmutableList<ImmutableTxOutput> getTxOutputs() {
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

	public Integer getDescendantcount() {
		return descendantcount;
	}

	public Integer getDescendantsize() {
		return descendantsize;
	}

	public Integer getDescendantfees() {
		return descendantfees;
	}

	public Integer getAncestorcount() {
		return ancestorcount;
	}

	public Integer getAncestorsize() {
		return ancestorsize;
	}

	public Integer getAncestorfees() {
		return ancestorfees;
	}

	public ImmutableList<String> getDepends() {
		return depends;
	}

	/**
	 * Devuelve todas las direcciones involucradas en la transacci'on, tanto de las
	 * entradas como de las salidas y con duplicados.
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
		builder.append(", descendantcount=");
		builder.append(descendantcount);
		builder.append(", descendantsize=");
		builder.append(descendantsize);
		builder.append(", descendantfees=");
		builder.append(descendantfees);
		builder.append(", ancestorcount=");
		builder.append(ancestorcount);
		builder.append(", ancestorsize=");
		builder.append(ancestorsize);
		builder.append(", ancestorfees=");
		builder.append(ancestorfees);
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
		ImmutableTransacion other = (ImmutableTransacion) obj;
		if (txId == null) {
			if (other.txId != null)
				return false;
		} else if (!txId.equals(other.txId))
			return false;
		return true;
	}

}
