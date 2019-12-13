package com.mempoolexplorer.bitcoind.adapter.components.clients;

import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.test.context.ActiveProfiles;

import com.mempoolexplorer.bitcoind.adapter.AppProfiles;
import com.mempoolexplorer.bitcoind.adapter.properties.BitcoindProperties;

@RestClientTest({BitcoindClient.class,BitcoindProperties.class})
@ActiveProfiles(AppProfiles.TEST)
public class BitcoindClientImplTest {

/*	@Autowired
	private BitcoindClient bitcoindClient;
	
	@Autowired
    private MockRestServiceServer server;

	@Test
	void getBlockCountTest() {
		this.server.expect(requestTo("/")).andRespond(withSuccess("{\n" + 
				"    \"result\": 607503,\n" + 
				"    \"error\": null,\n" + 
				"    \"id\": null\n" + 
				"}", MediaType.APPLICATION_JSON));
		Integer blockNumber=this.bitcoindClient.getBlockCount();
		assertThat(blockNumber).isEqualTo(607503);
	}
*/
}