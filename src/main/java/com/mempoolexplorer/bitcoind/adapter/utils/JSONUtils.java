package com.mempoolexplorer.bitcoind.adapter.utils;

public class JSONUtils {
	public static long satoshisPerBitcoin = 100000000L;

	// Cambia una cantidad de bitcoin en decimales a numero de satoshis.
	public static Long JSONtoAmount(Double value) {
		return Long.valueOf((long) (value * satoshisPerBitcoin));
	}
}
