package com.mempoolexplorer.bitcoind.adapter.components.clients;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockResult;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockTemplateResult;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetMemPoolInfo;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolNonVerbose;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolVerbose;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionResult;

public interface BitcoindClient {

	GetRawMemPoolNonVerbose getRawMemPoolNonVerbose();

	GetRawMemPoolVerbose getRawMemPoolVerbose();

	GetBlockTemplateResult getBlockTemplateResult();

	GetMemPoolInfo getMemPoolInfo();

	GetVerboseRawTransactionResult getVerboseRawTransaction(String txId);

	Integer getBlockCount(); 
	
	GetBlockResult getBlock(Integer blockHeight);
}
