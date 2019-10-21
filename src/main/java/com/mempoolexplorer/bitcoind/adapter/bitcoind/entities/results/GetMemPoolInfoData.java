package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

public class GetMemPoolInfoData {

	private Integer size; // The number of transactions currently in the memory pool
	private Integer bytes; // The total number of bytes in the transactions in the memory pool
	private Integer usage; // Total memory usage for the mempool in bytes
	private Integer maxmempool; // Maximum memory usage for the mempool in bytes
	private Integer mempoolminfee; // The lowest fee per kilobyte paid by any transaction in the memory pool

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Integer getBytes() {
		return bytes;
	}

	public void setBytes(Integer bytes) {
		this.bytes = bytes;
	}

	public Integer getUsage() {
		return usage;
	}

	public void setUsage(Integer usage) {
		this.usage = usage;
	}

	public Integer getMaxmempool() {
		return maxmempool;
	}

	public void setMaxmempool(Integer maxmempool) {
		this.maxmempool = maxmempool;
	}

	public Integer getMempoolminfee() {
		return mempoolminfee;
	}

	public void setMempoolminfee(Integer mempoolminfee) {
		this.mempoolminfee = mempoolminfee;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetMemPoolInfoData [size=");
		builder.append(size);
		builder.append(", bytes=");
		builder.append(bytes);
		builder.append(", usage=");
		builder.append(usage);
		builder.append(", maxmempool=");
		builder.append(maxmempool);
		builder.append(", mempoolminfee=");
		builder.append(mempoolminfee);
		builder.append("]");
		return builder.toString();
	}

}
