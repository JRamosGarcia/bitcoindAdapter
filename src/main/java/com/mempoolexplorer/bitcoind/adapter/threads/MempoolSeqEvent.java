package com.mempoolexplorer.bitcoind.adapter.threads;

import java.util.Optional;

import lombok.Value;

@Value
public class MempoolSeqEvent {
    String hash;
    MempoolEventEnum event;
    Optional<Integer> mempoolSequence;// empty for block events
    int zmqSequence;
}
