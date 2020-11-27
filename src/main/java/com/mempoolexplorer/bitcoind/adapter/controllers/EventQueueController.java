package com.mempoolexplorer.bitcoind.adapter.controllers;

import com.mempoolexplorer.bitcoind.adapter.components.containers.MempoolSeqEventQueueContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pendingEvents")
public class EventQueueController {
    @Autowired
    private MempoolSeqEventQueueContainer queueContainer;

    @GetMapping("/size")
    public int getSize() {
        return queueContainer.getBlockingQueue().size();
    }

}
