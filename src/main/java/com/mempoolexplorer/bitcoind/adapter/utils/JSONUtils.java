package com.mempoolexplorer.bitcoind.adapter.utils;

import java.math.BigDecimal;

public class JSONUtils {

	private JSONUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static Long jsonToAmount(BigDecimal value) {
		return value.movePointRight(8).longValue();
	}

}
