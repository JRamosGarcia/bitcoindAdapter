package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import java.util.List;

public class GetRawMemPoolVerboseData {

	private Integer size;// The size of the serialized transaction in bytes
	private Double fee;// The transaction fee paid by the transaction in decimal bitcoins
	private Double modifiedfee;// The transaction fee with fee deltas used for mining priority in decimal
								// bitcoins
	private Long time;// The time the transaction entered the memory pool, Unix epoch time format
	private Integer height;// The time the transaction entered the memory pool, Unix epoch time format
	private Integer startingpriority;// The priority of the transaction when it first entered the memory pool
	private Integer currentpriority;// The current priority of the transaction
	private Integer descendantcount;// The number of in-mempool descendant transactions (including this one)
	private Integer descendantsize;// The size of in-mempool descendants (including this one)
	private Integer descendantfees;// The modified fees (see modifiedfee above) of in-mempool descendants
									// (including this one)
	private Integer ancestorcount;// The number of in-mempool ancestor transactions (including this one)
	private Integer ancestorsize;// The size of in-mempool ancestors (including this one)
	private Integer ancestorfees;// The modified fees (see modifiedfee above) of in-mempool ancestors (including
									// this one)
	private List<String> depends;// An array holding TXIDs of unconfirmed transactions (encoded as hex in RPC
									// byte order) this transaction depends
									// upon (parent transactions). Those transactions must be part of a block before
									// this transaction can be added to a block, although all transactions may be
									// included in the same block. The array may be empty

	public GetRawMemPoolVerboseData() {
		super();
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Double getFee() {
		return fee;
	}

	public void setFee(Double fee) {
		this.fee = fee;
	}

	public Double getModifiedfee() {
		return modifiedfee;
	}

	public void setModifiedfee(Double modifiedfee) {
		this.modifiedfee = modifiedfee;
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

	public Integer getStartingpriority() {
		return startingpriority;
	}

	public void setStartingpriority(Integer startingpriority) {
		this.startingpriority = startingpriority;
	}

	public Integer getCurrentpriority() {
		return currentpriority;
	}

	public void setCurrentpriority(Integer currentpriority) {
		this.currentpriority = currentpriority;
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

	public Integer getDescendantfees() {
		return descendantfees;
	}

	public void setDescendantfees(Integer descendantfees) {
		this.descendantfees = descendantfees;
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

	public Integer getAncestorfees() {
		return ancestorfees;
	}

	public void setAncestorfees(Integer ancestorfees) {
		this.ancestorfees = ancestorfees;
	}

	public List<String> getDepends() {
		return depends;
	}

	public void setDepends(List<String> depends) {
		this.depends = depends;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetRawMemPoolVerboseData [size=");
		builder.append(size);
		builder.append(", fee=");
		builder.append(fee);
		builder.append(", modifiedfee=");
		builder.append(modifiedfee);
		builder.append(", time=");
		builder.append(time);
		builder.append(", height=");
		builder.append(height);
		builder.append(", startingpriority=");
		builder.append(startingpriority);
		builder.append(", currentpriority=");
		builder.append(currentpriority);
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

}
