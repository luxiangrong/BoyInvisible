package com.kai.boyinvisible.downloader;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.impl.client.CloseableHttpClient;

public class DownloaderFactory {

	/**
	 * 根据url返回相应的下载器
	 * 
	 * @param url
	 * @return
	 */
	public static Downloader createDownloader(String urlString) {

		try {
			URL url = new URL(urlString);
			String host = url.getHost();

			if (host.indexOf("rmdown") != -1) {
				return new RmdownDownloader();
			}

		} catch (MalformedURLException e) {
			return new DefaultDownloader();
		}

		return new DefaultDownloader();
	}

	/**
	 * 根据url返回相应的下载器
	 * 
	 * @param url
	 * @return
	 */
	public static Downloader createDownloader(String urlString, CloseableHttpClient httpClient) {

		try {
			URL url = new URL(urlString);
			String host = url.getHost();

			if (host.indexOf("rmdown") != -1) {
				return new RmdownDownloader(httpClient);
			}

		} catch (MalformedURLException e) {
			return new DefaultDownloader();
		}

		return new DefaultDownloader();
	}
}
