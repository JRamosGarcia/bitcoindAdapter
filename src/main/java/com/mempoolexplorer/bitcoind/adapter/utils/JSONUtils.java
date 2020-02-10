package com.mempoolexplorer.bitcoind.adapter.utils;

import java.math.BigDecimal;

public class JSONUtils {
	//public static long satoshisPerBitcoin = 100000000L;

	// Cambia una cantidad de bitcoin en decimales a numero de satoshis.
	//public static Long JSONtoAmount(Double value) {
	//	return Long.valueOf(Math.round(value * 1e8));
	//}

	public static Long JSONtoAmount(BigDecimal value) {
		return value.movePointRight(8).longValue();
		//return Long.valueOf(Math.round(value * 1e8));
	}


}
