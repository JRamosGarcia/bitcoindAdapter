package com.mempoolexplorer.bitcoind.adapter.components.clients;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class BitcoindClientResponseErrorHandler implements ResponseErrorHandler{
	Logger logger = LoggerFactory.getLogger(BitcoindClientResponseErrorHandler.class);
	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return response.getStatusCode().is4xxClientError()||response.getStatusCode().is5xxServerError();
	}

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
	       logger.error("Response error: {} {}."
	       		+ "Note that bitcoind can return error 500 indiscriminately (instead of 404 for example).", response.getStatusCode(), response.getStatusText());

	}

}
