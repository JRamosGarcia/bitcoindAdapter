package com.mempoolexplorer.bitcoind.adapter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
@ConditionalOnProperty(name = "spring.cloud.config.enabled")
public class CloudConfig {

}
