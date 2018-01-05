package cluster.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import javax.annotation.PostConstruct;

public class CuratorClientBuilder {

	private String connectString = "localhost:2181";
	private int betweenRetries = 1000;
	private int connectionTimeoutMs = 5000;
	private int sessionTimeoutMs = 4000;
	public static CuratorFramework client;

	public synchronized CuratorFramework build() {
		if (client == null) {
			client = CuratorFrameworkFactory.builder().connectString(connectString)
			                                .retryPolicy(new RetryNTimes(2147483647, betweenRetries))
			                                .connectionTimeoutMs(connectionTimeoutMs)
			                                .sessionTimeoutMs(sessionTimeoutMs).build();
			client.start();
		}
		return client;
	}

	public static CuratorFramework getCuratorClient() {
		if (client == null) {
			client = CuratorFrameworkFactory.builder().connectString("localhost:2181")
					.retryPolicy(new RetryNTimes(2147483647, 1000))
					.connectionTimeoutMs(5000)
					.sessionTimeoutMs(4000).build();
			client.start();
		}
		return client;
	}

	public String getConnectString() {
		return connectString;
	}

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	public int getBetweenRetries() {
		return betweenRetries;
	}

	public void setBetweenRetries(int betweenRetries) {
		this.betweenRetries = betweenRetries;
	}

	public int getConnectionTimeoutMs() {
		return connectionTimeoutMs;
	}

	public void setConnectionTimeoutMs(int connectionTimeoutMs) {
		this.connectionTimeoutMs = connectionTimeoutMs;
	}

	public int getSessionTimeoutMs() {
		return sessionTimeoutMs;
	}

	public void setSessionTimeoutMs(int sessionTimeoutMs) {
		this.sessionTimeoutMs = sessionTimeoutMs;
	}

}
