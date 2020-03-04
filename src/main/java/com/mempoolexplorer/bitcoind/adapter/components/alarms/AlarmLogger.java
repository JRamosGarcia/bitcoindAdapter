package com.mempoolexplorer.bitcoind.adapter.components.alarms;

import java.util.List;

public interface AlarmLogger {

	void addAlarm(String alarm);

	void prettyPrint();

	List<String> getAlarmList();
	

}