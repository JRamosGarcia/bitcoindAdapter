package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

public class FeesData {
	private Double base;
	private Double modified;
	private Double ancestor;
	private Double descendant;
	
	
	public Double getBase() {
		return base;
	}
	public void setBase(Double base) {
		this.base = base;
	}
	public Double getModified() {
		return modified;
	}
	public void setModified(Double modified) {
		this.modified = modified;
	}
	public Double getAncestor() {
		return ancestor;
	}
	public void setAncestor(Double ancestor) {
		this.ancestor = ancestor;
	}
	public Double getDescendant() {
		return descendant;
	}
	public void setDescendant(Double descendant) {
		this.descendant = descendant;
	}
	@Override
	public String toString() {
		return "FeesData [base=" + base + ", modified=" + modified + ", ancestor=" + ancestor + ", descendant="
				+ descendant + "]";
	}
	
	
}
