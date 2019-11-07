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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetVerboseRawTransactionOutputVector [value=");
		builder.append(value);
		builder.append(", n=");
		builder.append(n);
		builder.append(", scriptpubkey=");
		builder.append(scriptPubKey);
		builder.append("]");
		return builder.toString();
	}

}
