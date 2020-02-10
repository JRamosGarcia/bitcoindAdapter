package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import java.math.BigDecimal;

public class FeesData {
	private BigDecimal base;
	private BigDecimal modified;
	private BigDecimal ancestor;
	private BigDecimal descendant;
	
	
	public BigDecimal getBase() {
		return base;
	}
	public void setBase(BigDecimal base) {
		this.base = base;
	}
	public BigDecimal getModified() {
		return modified;
	}
	public void setModified(BigDecimal modified) {
		this.modified = modified;
	}
	public BigDecimal getAncestor() {
		return ancestor;
	}
	public void setAncestor(BigDecimal ancestor) {
		this.ancestor = ancestor;
	}
	public BigDecimal getDescendant() {
		return descendant;
	}
	public void setDescendant(BigDecimal descendant) {
		this.descendant = descendant;
	}
	@Override
	public String toString() {
		return "FeesData [base=" + base + ", modified=" + modified + ", ancestor=" + ancestor + ", descendant="
				+ descendant + "]";
	}
	
	
}
