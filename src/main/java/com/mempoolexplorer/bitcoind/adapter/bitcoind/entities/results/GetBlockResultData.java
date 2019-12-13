package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import java.util.List;

public class GetBlockResultData {

	private String hash;
	private Integer height;
	private Long time;// Set by miners, can be in the future!
	private Long mediantime;// Always increases with block height
	private List<String> tx;

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Long getMediantime() {
		return mediantime;
	}

	public void setMediantime(Long mediantime) {
		this.mediantime = mediantime;
	}

	public List<String> getTx() {
		return tx;
	}

	public void setTx(List<String> tx) {
		this.tx = tx;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetBlockResultData [hash=");
		builder.append(hash);
		builder.append(", height=");
		builder.append(height);
		builder.append(", time=");
		builder.append(time);
		builder.append(", mediantime=");
		builder.append(mediantime);
		builder.append(", tx=");
		builder.append(tx);
		builder.append("]");
		return builder.toString();
	}

}
