package com.mempoolexplorer.bitcoind.adapter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@RefreshScope
@EnableScheduling
@ConditionalOnProperty(name = "spring.cloud.config.enabled")
public class CloudConfig {

}
