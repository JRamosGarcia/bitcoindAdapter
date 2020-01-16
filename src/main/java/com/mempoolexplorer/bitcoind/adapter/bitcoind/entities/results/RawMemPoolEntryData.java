package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RawMemPoolEntryData {

	private Integer size;// virtual transaction size as defined in BIP 141.
							// This is different from actual serialized size for witness transactions as
							// witness data is discounted.
	private Long time;// The time the transaction entered the memory pool, Unix epoch time format
	private Integer height;// The time the transaction entered the memory pool, Unix epoch time format
	private Integer descendantcount;// The number of in-mempool descendant transactions (including this one)
	private Integer descendantsize;// The size of in-mempool descendants (including this one)
	private Integer ancestorcount;// The number of in-mempool ancestor transactions (including this one)
	private Integer ancestorsize;// The size of in-mempool ancestors (including this one)
	private FeesData fees;
	private List<String> depends = new ArrayList<>();// An array holding TXIDs of unconfirmed transactions
	private List<String> spentby = new ArrayList<>();// unconfirmed transactions spending outputs from this transaction
														// (txIds list)
	@JsonProperty("bip125-replaceable")
	private Boolean bip125Replaceable;

	public RawMemPoolEntryData() {
		super();
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getDescendantcount() {
		return descendantcount;
	}

	public void setDescendantcount(Integer descendantcount) {
		this.descendantcount = descendantcount;
	}

	public Integer getDescendantsize() {
		return descendantsize;
	}

	public void setDescendantsize(Integer descendantsize) {
		this.descendantsize = descendantsize;
	}

	public Integer getAncestorcount() {
		return ancestorcount;
	}

	public void setAncestorcount(Integer ancestorcount) {
		this.ancestorcount = ancestorcount;
	}

	public Integer getAncestorsize() {
		return ancestorsize;
	}

	public void setAncestorsize(Integer ancestorsize) {
		this.ancestorsize = ancestorsize;
	}

	public FeesData getFees() {
		return fees;
	}

	public void setFees(FeesData fees) {
		this.fees = fees;
	}

	public List<String> getDepends() {
		return depends;
	}

	public void setDepends(List<String> depends) {
		this.depends = depends;
	}

	public List<String> getSpentby() {
		return spentby;
	}

	public void setSpentby(List<String> spentby) {
		this.spentby = spentby;
	}

	public Boolean getBip125Replaceable() {
		return bip125Replaceable;
	}

	public void setBip125Replaceable(Boolean bip125Replaceable) {
		this.bip125Replaceable = bip125Replaceable;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RawMemPoolEntryData [size=");
		builder.append(size);
		builder.append(", time=");
		builder.append(time);
		builder.append(", height=");
		builder.append(height);
		builder.append(", descendantcount=");
		builder.append(descendantcount);
		builder.append(", descendantsize=");
		builder.append(descendantsize);
		builder.append(", ancestorcount=");
		builder.append(ancestorcount);
		builder.append(", ancestorsize=");
		builder.append(ancestorsize);
		builder.append(", fees=");
		builder.append(fees);
		builder.append(", depends=");
		builder.append(depends);
		builder.append(", spentby=");
		builder.append(spentby);
		builder.append(", bip125Replaceable=");
		builder.append(bip125Replaceable);
		builder.append("]");
		return builder.toString();
	}

}
