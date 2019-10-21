package com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions;


//TODO: Cambiar a TxPoolException
public class MemPoolException extends Exception {

	private static final long serialVersionUID = 7083210067936996120L;

	public MemPoolException(String msg) {
		super(msg);
	}

	public MemPoolException(String msg, Throwable cause) {
		super(msg,cause);
	}
	
	public MemPoolException(Throwable cause) {
		super(cause);
	}

}
