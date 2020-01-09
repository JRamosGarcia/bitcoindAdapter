package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

public class GetMemPoolInfoData {

	private Integer size; // Current tx count
	private Integer bytes; // Sum of all virtual transaction sizes as defined in BIP 141. Differs from
							// actual serialized size because witness data is discounted
	private Integer usage; // Total memory usage for the mempool in bytes
	private Integer maxmempool; // Maximum memory usage for the mempool in bytes
	private Integer mempoolminfee; // Minimum fee rate in BTC/kB for tx to be accepted. Is the maximum of
									// minrelaytxfee and minimum mempool fee
	private Integer minrelaytxfee; // Current minimum relay fee for transaction

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

	public Integer getMinrelaytxfee() {
		return minrelaytxfee;
	}

	public void setMinrelaytxfee(Integer minrelaytxfee) {
		this.minrelaytxfee = minrelaytxfee;
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
		builder.append(", minrelaytxfee=");
		builder.append(minrelaytxfee);
		builder.append("]");
		return builder.toString();
	}

}
