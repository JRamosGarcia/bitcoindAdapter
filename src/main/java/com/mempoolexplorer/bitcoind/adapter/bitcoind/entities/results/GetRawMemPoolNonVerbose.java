package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@NoArgsConstructor
public class GetRawMemPoolNonVerbose extends BitcoindResult {

	@JsonProperty("result")
	List<String> trxHashList;

}
