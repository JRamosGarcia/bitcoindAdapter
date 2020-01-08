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

//TODO:Meter sonarq cuando tengas mucho tiempo libre.
@SpringBootApplication
//@RefreshScope
public class BitcoindAdapterApplication {

	@Autowired
	private BitcoindProperties bitcoindProperties;

	// Donde realmente está la chicha es en AppLifeCyle y en MemPoolRefresherJob
	public static void main(String[] args) {
		// If no profiles are set through cmd line argument or system environment or jvm
		// arguments then use prod and mainNet profiles
		SpringApplication app = new SpringApplication(BitcoindAdapterApplication.class);
//		SimpleCommandLinePropertySource source = new SimpleCommandLinePropertySource(args);
//		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
//		List<String> jvmArguments = runtimeMxBean.getInputArguments();
//		if (!source.containsProperty("spring.profiles.active") && !System.getenv().containsKey("SPRING_PROFILES_ACTIVE")
//				&& !containsProfilesActiveArgument(jvmArguments)) {
//			app.setAdditionalProfiles(AppProfiles.PROD, AppProfiles.MAIN_NET);
//		}
		app.run(args);
		// Application really begins to work in class {@code AppLifeCycle}
	}

	/*
	 * private static boolean containsProfilesActiveArgument(List<String> jvmArgs) {
	 * for (String arg : jvmArgs) { if (arg.startsWith("-Dspring.profiles.active"))
	 * { return true; } } return false; }
	 */
	
	
	@Bean
	public RestTemplate getBitcoindClient(RestTemplateBuilder restTemplateBuilder)
			throws NumberFormatException, URISyntaxException {
		return restTemplateBuilder.basicAuthentication(bitcoindProperties.getUser(), bitcoindProperties.getPassword())
				.additionalMessageConverters(new MappingJackson2HttpMessageConverter())
				.rootUri(UriComponentsBuilder.newInstance().scheme("http").host(bitcoindProperties.getHost())
						.port(Integer.valueOf(bitcoindProperties.getPort())).toUriString())
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
