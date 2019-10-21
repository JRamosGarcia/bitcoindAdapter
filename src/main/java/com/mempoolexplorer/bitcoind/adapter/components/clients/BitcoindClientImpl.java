package com.mempoolexplorer.bitcoind.adapter.components.clients;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.requests.BooleanArrayParamRequest;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.requests.ObjectArrayParamRequest;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.requests.StringArrayParamRequest;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockTemplateResult;
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
		StringArrayParamRequest stringParams = new StringArrayParamRequest();

		stringParams.setId("4");
		stringParams.setMethod("getblocktemplate");
		stringParams.setParams(new ArrayList<String>());

		return restTemplate.postForObject("/", stringParams, GetBlockTemplateResult.class);
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

}
