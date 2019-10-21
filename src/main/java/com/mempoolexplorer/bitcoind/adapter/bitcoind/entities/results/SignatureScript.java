package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

/**
 * An object describing the signature script of this input. Not present if this
 * is a coinbase transaction
 * 
 * @author tomillo
 *
 */
public abstract class SignatureScript {

	private String asm;// The signature script in decoded form with non-data-pushing opcodes listed
	private String hex;// The signature script encoded as hex

	public String getAsm() {
		return asm;
	}

	public void setAsm(String asm) {
		this.asm = asm;
	}

	public String getHex() {
		return hex;
	}

	public void setHex(String hex) {
		this.hex = hex;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScriptSig [asm=");
		builder.append(asm);
		builder.append(", hex=");
		builder.append(hex);
		builder.append("]");
		return builder.toString();
	}

}
