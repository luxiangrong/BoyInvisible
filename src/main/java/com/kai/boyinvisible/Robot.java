package com.kai.boyinvisible;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

public abstract class Robot {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.2)";

	protected static String DEFAULT_SAVE_DIRECTORY_PATH = "src/test/resources";
	protected static String DEFAULT_TEMP_DIRECTORY_PATH = "";

	protected CloseableHttpClient httpClient;

	protected String charset;

	public Robot() {
		httpClient = buildHttpClient();
		charset = detectCharset();
	}


	private CloseableHttpClient buildHttpClient() {
		HttpClientBuilder builder = HttpClients.custom();
		builder.setUserAgent(USER_AGENT);
		builder = custom(builder);

		return builder.build();
	}

	/**
	 * 子类自定义HttpClient配置
	 * 
	 * @param builder
	 * @return
	 */
	protected abstract HttpClientBuilder custom(HttpClientBuilder builder);

	/**
	 * 需要登录的Robot正确实现此方法
	 */
	protected abstract void login(String loginUrl, String username, String password);

	protected String detectCharset() {
		return "UTF-8";
	}

}
