package com.mempoolexplorer.bitcoind.adapter.components;

import org.springframework.stereotype.Component;

import com.mempoolexplorer.bitcoind.adapter.entities.AppStateEnum;

@Component
public class AppState {

	private AppStateEnum state = AppStateEnum.STARTING;

	public AppStateEnum getState() {
		return state;
	}

	public void setState(AppStateEnum state) {
		this.state = state;
	}

}
