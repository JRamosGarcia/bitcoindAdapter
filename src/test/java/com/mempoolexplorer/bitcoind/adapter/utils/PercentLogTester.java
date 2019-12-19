package com.mempoolexplorer.bitcoind.adapter.utils;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class PercentLogTester {
	@Test
	public void tester1() {
		PercentLog pl = new PercentLog(1333);
		IntStream.range(0, 1333).forEach(i -> {
			pl.update(i, (p) -> System.out.println(p));
		});
	}

	@Test
	public void tester2() {
		PercentLog pl = new PercentLog(6666, 5);
		IntStream.range(0, 10000).forEach(i -> {
			pl.update(i, (p) -> System.out.println(p));
		});

	}
}
