package com.mempoolexplorer.bitcoind.adapter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bitcoind")
public class BitcoindProperties {

	/**
	 * User in bitcoind authentication.
	 */
	private String user;

	/**
	 * Password in bitcoind authentication.
	 */
	private String password;

	/**
	 * bitcoind host address.
	 */
	private String host;

	/**
	 * bitcoind port (normally 8332)
	 */
	private String port;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

}
