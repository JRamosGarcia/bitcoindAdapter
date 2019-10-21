package com.mempoolexplorer.bitcoind.adapter.controllers.exceptions;

public class MemPoolSizeTooBigException extends Exception {

	private static final long serialVersionUID = 1160954489278184918L;

	private int size;

	public MemPoolSizeTooBigException() {
		super();
	}

	public MemPoolSizeTooBigException(String message) {
		super(message);
	}

	public MemPoolSizeTooBigException(String message, int size) {
		super(message);
		this.size = size;
	}

	public MemPoolSizeTooBigException(String message, Throwable cause) {
		super(message, cause);
	}

	public int getSize() {
		return size;
	}

}
