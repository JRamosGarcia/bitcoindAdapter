package com.mempoolexplorer.bitcoind.adapter.threads;

import lombok.Value;

@Value
public class MempoolSeqEvent {
    String hash;
    MempoolEventEnum event;
    int mempoolSequence;
    int zmqSequence;
}
