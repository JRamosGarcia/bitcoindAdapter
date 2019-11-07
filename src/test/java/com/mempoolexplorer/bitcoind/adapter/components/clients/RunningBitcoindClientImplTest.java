package com.mempoolexplorer.bitcoind.adapter.components.clients;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mempoolexplorer.bitcoind.adapter.AppProfiles;

/**
 * Testing class for bitcoind client. It needs bitcoind daemon running on local
 * machine.
 *
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(AppProfiles.TEST)
@DirtiesContext
public class RunningBitcoindClientImplTest {

	//@Autowired
	//private BitcoindClient bitcoindClient;

	//@Test
	//public void getInfoResultTest() throws URISyntaxException {
		/*
		 * GetMemPoolInfo memPoolInfo = bitcoindClient.getMemPoolInfo();
		 * assertNull(memPoolInfo.getError()); System.out.println(memPoolInfo);
		 */	
	//}
}
