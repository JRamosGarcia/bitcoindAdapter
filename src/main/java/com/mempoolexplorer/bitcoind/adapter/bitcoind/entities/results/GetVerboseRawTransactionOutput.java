package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

/**
 * An array of objects each describing an output vector (vout) for this
 * transaction. Output objects will have the same order within the array as they
 * have in the transaction, so the first output listed will be output 0
 * 
 * @author tomillo
 *
 */
public class GetVerboseRawTransactionOutput {

	private Double value;// The number of bitcoins paid to this output. May be 0
	private Integer n;// The output index number of this output within this transaction
	private ScriptPubKey scriptPubKey;// the pubkey script
	private String blockHash;// (Optional) If the transaction has been included in a block on the local best
								// block chain, this is the hash of that block encoded as hex in RPC byte order
	private Integer confirmations;// If the transaction has been included in a block on the local best block
									// chain, this is how many confirmations it has. Otherwise, this is 0
	private Integer time;// If the transaction has been included in a block on the local best block
							// chain, this is the block header time of that block (may be in the future).
							// The transaction time in seconds since epoch (Jan 1 1970 GMT)
	private Integer blocktime; // The transaction time in seconds since epoch (Jan 1 1970 GMT). The block time
								// in seconds since epoch (Jan 1 1970 GMT)

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Integer getN() {
		return n;
	}

	public void setN(Integer n) {
		this.n = n;
	}

	public ScriptPubKey getScriptPubKey() {
		return scriptPubKey;
	}

	public void setScriptPubKey(ScriptPubKey scriptPubKey) {
		this.scriptPubKey = scriptPubKey;
	}

	public String getBlockHash() {
		return blockHash;
	}

	public void setBlockHash(String blockHash) {
		this.blockHash = blockHash;
	}

	public Integer getConfirmations() {
		return confirmations;
	}

	public void setConfirmations(Integer confirmations) {
		this.confirmations = confirmations;
	}

	public Integer getTime() {
		return time;
	}

	public void setTime(Integer time) {
		this.time = time;
	}

	public Integer getBlocktime() {
		return blocktime;
	}

	public void setBlocktime(Integer blocktime) {
		this.blocktime = blocktime;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetVerboseRawTransactionOutputVector [value=");
		builder.append(value);
		builder.append(", n=");
		builder.append(n);
		builder.append(", scriptpubkey=");
		builder.append(scriptPubKey);
		builder.append(", blockHash=");
		builder.append(blockHash);
		builder.append(", confirmations=");
		builder.append(confirmations);
		builder.append(", time=");
		builder.append(time);
		builder.append(", blocktime=");
		builder.append(blocktime);
		builder.append("]");
		return builder.toString();
	}

}
