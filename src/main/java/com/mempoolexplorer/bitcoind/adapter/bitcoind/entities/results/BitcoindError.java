package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

public class BitcoindError {
 
	private Integer code;
	private String message;
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "BitcoindErrorResult [code=" + code + ", message=" + message + "]";
	}
	
	
}
