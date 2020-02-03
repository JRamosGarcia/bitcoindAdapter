package com.mempoolexplorer.bitcoind.adapter.components.clients;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.requests.BooleanArrayParamRequest;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.requests.GetBlockTemplateRulesParams;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.requests.ObjectArrayParamRequest;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.requests.StringArrayParamRequest;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockCount;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockHashResult;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockResult;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockTemplateResult;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetMemPoolEntry;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetMemPoolInfo;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolNonVerbose;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolVerbose;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionResult;

@Component
public class BitcoindClientImpl implements BitcoindClient {

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public GetRawMemPoolNonVerbose getRawMemPoolNonVerbose() {
		BooleanArrayParamRequest boolParams = new BooleanArrayParamRequest();
		boolParams.setId("2");
		boolParams.setMethod("getrawmempool");
		List<Boolean> params = new ArrayList<Boolean>();
		params.add(false);
		boolParams.setParams(params);

		return restTemplate.postForObject("/", boolParams, GetRawMemPoolNonVerbose.class);
	}

	@Override
	public GetRawMemPoolVerbose getRawMemPoolVerbose() {
		BooleanArrayParamRequest boolParams = new BooleanArrayParamRequest();
		boolParams.setId("3");
		boolParams.setMethod("getrawmempool");
		List<Boolean> params = new ArrayList<Boolean>();
		params.add(true);
		boolParams.setParams(params);

		return restTemplate.postForObject("/", boolParams, GetRawMemPoolVerbose.class);
	}

	@Override
	public GetBlockTemplateResult getBlockTemplateResult() {
		ObjectArrayParamRequest objectParams = new ObjectArrayParamRequest();

		objectParams.setId("4");
		objectParams.setMethod("getblocktemplate");
		objectParams.setParams(new ArrayList<Object>());
		GetBlockTemplateRulesParams rulesParams = new GetBlockTemplateRulesParams();
		rulesParams.getRules().add("segwit");
		objectParams.getParams().add(rulesParams);

		return restTemplate.postForObject("/", objectParams, GetBlockTemplateResult.class);
	}

	@Override
	public GetMemPoolInfo getMemPoolInfo() {
		StringArrayParamRequest stringParams = new StringArrayParamRequest();

		stringParams.setId("5");
		stringParams.setMethod("getmempoolinfo");
		stringParams.setParams(new ArrayList<String>());

		return restTemplate.postForObject("/", stringParams, GetMemPoolInfo.class);
	}

	@Override
	public GetVerboseRawTransactionResult getVerboseRawTransaction(String txId) {
		ObjectArrayParamRequest objectParams = new ObjectArrayParamRequest();

		objectParams.setId("6");
		objectParams.setMethod("getrawtransaction");
		List<Object> params = new ArrayList<>();
		params.add(txId);
		params.add(Boolean.valueOf(true));
		objectParams.setParams(params);

		return restTemplate.postForObject("/", objectParams, GetVerboseRawTransactionResult.class);
	}

	@Override
	public Integer getBlockCount() {
		StringArrayParamRequest stringParams = new StringArrayParamRequest();
		stringParams.setId("7");
		stringParams.setMethod("getblockcount");
		stringParams.setParams(new ArrayList<String>());

		return restTemplate.postForObject("/", stringParams, GetBlockCount.class).getBlockNumber();
	}

	@Override
	public GetBlockResult getBlock(Integer blockHeight) {
		ObjectArrayParamRequest objectParams = new ObjectArrayParamRequest();

		objectParams.setId("8");
		objectParams.setMethod("getblockhash");
		List<Object> params = new ArrayList<>();
		params.add(blockHeight);
		objectParams.setParams(params);

		GetBlockHashResult getblockHashResult = restTemplate.postForObject("/", objectParams, GetBlockHashResult.class);

		String blockHash = getblockHashResult.getBlockHash();
		objectParams = new ObjectArrayParamRequest();
		objectParams.setId("9");
		objectParams.setMethod("getblock");
		params = new ArrayList<>();
		params.add(blockHash);
		params.add(1);// Verbosity level
		objectParams.setParams(params);
		return restTemplate.postForObject("/", objectParams, GetBlockResult.class);
	}

	@Override
	public GetMemPoolEntry getMempoolEntry(String txId) {
		ObjectArrayParamRequest objectParams = new ObjectArrayParamRequest();

		objectParams.setId("10");
		objectParams.setMethod("getmempoolentry");
		List<Object> params = new ArrayList<>();
		params.add(txId);
		objectParams.setParams(params);

		return restTemplate.postForObject("/", objectParams, GetMemPoolEntry.class);
	}
}
