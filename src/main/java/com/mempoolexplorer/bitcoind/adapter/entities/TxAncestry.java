package com.mempoolexplorer.bitcoind.adapter.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TxAncestry {
	private Integer descendantCount;// The number of in-mempool descendant transactions (including this one)
	private Integer descendantSize;// virtual transaction size of in-mempool descendants (including this one)
	private Integer ancestorCount;// The number of in-mempool ancestor transactions (including this one)
	private Integer ancestorSize;// virtual transaction size of in-mempool ancestors (including this one)
	private List<String> depends = new ArrayList<>();// unconfirmed transactions used as inputs for this transaction
														// (txIds list)
	private List<String> spentby = new ArrayList<>();// unconfirmed transactions spending outputs from this transaction
														// (txIds list)
}
