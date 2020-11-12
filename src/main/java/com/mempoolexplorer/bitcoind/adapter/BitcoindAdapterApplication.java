package com.mempoolexplorer.bitcoind.adapter;

import java.net.URISyntaxException;
import java.time.Clock;

import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.mempoolexplorer.bitcoind.adapter.components.clients.BitcoindClientResponseErrorHandler;
import com.mempoolexplorer.bitcoind.adapter.properties.BitcoindProperties;

@SpringBootApplication
public class BitcoindAdapterApplication {

	@Autowired
	private BitcoindProperties bitcoindProperties;

	// Where really things are done: AppLifeCyle and MemPoolRefresherJob
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(BitcoindAdapterApplication.class);
		app.run(args);
	}

	@Bean
	public RestTemplate getBitcoindClient(RestTemplateBuilder restTemplateBuilder)
			throws NumberFormatException, URISyntaxException {
		return restTemplateBuilder.basicAuthentication(bitcoindProperties.getUser(), bitcoindProperties.getPassword())
				.additionalMessageConverters(new MappingJackson2HttpMessageConverter())
				.rootUri(UriComponentsBuilder.newInstance().scheme("http").host(bitcoindProperties.getHost())
						.port(Integer.valueOf(bitcoindProperties.getRpcPort())).toUriString())
				.errorHandler(new BitcoindClientResponseErrorHandler()).build();
	}

	@Bean
	public SchedulerFactory getSchedulerFactory() {
		return new StdSchedulerFactory();
	}

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}

}
