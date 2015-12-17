package com.kai.boyinvisible.downloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.kai.boyinvisible.exception.ApplicationRuntimeException;

/**
 * 下载器的默认实现，直接根据url下载页面或者文件
 * 
 * @author LuXiangrong
 *
 */
public class DefaultDownloader implements Downloader {

	protected CloseableHttpClient httpClient;
	protected String charset;

	public DefaultDownloader() {
		httpClient = HttpClients.createDefault();
	}

	public DefaultDownloader(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public String fetch(String url) {
		try {
			return fetch(new URL(url));
		} catch (MalformedURLException e) {
			throw new ApplicationRuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public String fetch(URL url) {
		CloseableHttpResponse response = null;
		try {
			HttpGet httpGet = new HttpGet(url.toURI());
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "gbk");
			EntityUtils.consume(entity);
			return responseString;
		} catch (IOException | URISyntaxException e) {
			throw new ApplicationRuntimeException(e.getMessage(), e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public File download(String url, String savePath, String saveName) {
		HttpGet httpGet;
		httpGet = new HttpGet(url);
		FileDownloadResponseHandler rh = new FileDownloadResponseHandler("utf-8", url, savePath, saveName);
		try {
			return httpClient.execute(httpGet, rh);
		} catch (IOException e) {
			throw new ApplicationRuntimeException("下载失败，发生网络错误", e);
		}
	}

}
