package com.mempoolexplorer.bitcoind.adapter.controllers.exceptions;

public class TransactionNotFoundInMemPoolException extends Exception {

	private static final long serialVersionUID = 1L;

	public TransactionNotFoundInMemPoolException() {
		super();
	}

	public TransactionNotFoundInMemPoolException(String message) {
		super(message);
	}

	public TransactionNotFoundInMemPoolException(String message, Throwable cause) {
		super(message, cause);
	}

}
