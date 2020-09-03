package com.mempoolexplorer.bitcoind.adapter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bitcoindadapter")
public class BitcoindAdapterProperties {

	private Integer refreshIntervalSec = 5;
	private Boolean loadDBOnStart = true;
	private Boolean saveDBOnStart = true;
	private Boolean saveDBOnRefresh = true;
	private Integer memPoolChangesSize = 10;
	private Integer newBlockListSize = 3;
	private Boolean sendAllTxOnStart = true;

	public Boolean getLoadDBOnStart() {
		return loadDBOnStart;
	}

	public void setLoadDBOnStart(Boolean loadDBOnStart) {
		this.loadDBOnStart = loadDBOnStart;
	}

	public Boolean getSaveDBOnStart() {
		return saveDBOnStart;
	}

	public void setSaveDBOnStart(Boolean saveDBOnStart) {
		this.saveDBOnStart = saveDBOnStart;
	}

	public Boolean getSaveDBOnRefresh() {
		return saveDBOnRefresh;
	}

	public void setSaveDBOnRefresh(Boolean saveDBOnRefresh) {
		this.saveDBOnRefresh = saveDBOnRefresh;
	}

	public Integer getMemPoolChangesSize() {
		return memPoolChangesSize;
	}

	public void setMemPoolChangesSize(Integer memPoolChangesSize) {
		this.memPoolChangesSize = memPoolChangesSize;
	}

	public Integer getNewBlockListSize() {
		return newBlockListSize;
	}

	public void setNewBlockListSize(Integer newBlockListSize) {
		this.newBlockListSize = newBlockListSize;
	}

	public Integer getRefreshIntervalSec() {
		return refreshIntervalSec;
	}

	public void setRefreshIntervalSec(Integer refreshIntervalSec) {
		this.refreshIntervalSec = refreshIntervalSec;
	}

	public Boolean getSendAllTxOnStart() {
		return sendAllTxOnStart;
	}

	public void setSendAllTxOnStart(Boolean sendAllTxOnStart) {
		this.sendAllTxOnStart = sendAllTxOnStart;
	}

}
