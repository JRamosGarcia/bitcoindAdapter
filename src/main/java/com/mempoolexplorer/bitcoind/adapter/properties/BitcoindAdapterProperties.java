package com.mempoolexplorer.bitcoind.adapter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "bitcoindadapter")
@Getter
@Setter
public class BitcoindAdapterProperties {

	private int refreshIntervalSec = 5;
	private boolean loadDBOnStart = true;
	private boolean saveDBOnStart = true;
	private boolean saveDBOnRefresh = true;
	private int memPoolChangesSize = 10;
	private int newBlockListSize = 3;
	private boolean sendAllTxOnStart = true;

}
