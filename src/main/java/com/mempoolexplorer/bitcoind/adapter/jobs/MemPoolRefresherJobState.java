package com.mempoolexplorer.bitcoind.adapter.jobs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemPoolRefresherJobState {
    private boolean firstMemPoolRefresh = true;
    private int blockNum = -1;
}
