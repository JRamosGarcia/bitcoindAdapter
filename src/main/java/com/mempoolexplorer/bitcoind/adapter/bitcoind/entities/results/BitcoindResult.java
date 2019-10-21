package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

public class BitcoindResult {

	private BitcoindError error;
	private String id;
	
	public BitcoindError getError() {
		return error;
	}
	public void setError(BitcoindError error) {
		this.error = error;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return "BitcoindResult [error=" + error + ", id=" + id + "]";
	}
	
}
