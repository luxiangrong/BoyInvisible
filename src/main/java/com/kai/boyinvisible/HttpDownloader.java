package com.kai.boyinvisible;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.kai.boyinvisible.exception.ApplicationRuntimeException;

public class HttpDownloader {

	private CloseableHttpClient httpClient;
	private RequestConfig config;
	private static Log logger = LogFactory.getLog(HttpDownloader.class);
	private int retryTimes = 0;

	public HttpDownloader(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
		config = RequestConfig.custom().setSocketTimeout(5000)
				.setConnectTimeout(5000).build();
	}

	public HttpDownloader(CloseableHttpClient httpClient, RequestConfig config) {
		this.httpClient = httpClient;
		this.config = config;
	}

	/**
	 * 以字符串的形式返回请求内容
	 * 
	 * @param urlString
	 * @return
	 */
	public String download(String urlString) {
		try {
			return download(new URL(urlString));
		} catch (MalformedURLException e) {
			throw new ApplicationRuntimeException("不是正确的URL字符串", e);
		}
	}

	/**
	 * 以字符串的形式返回请求内容
	 * 
	 * @param urlString
	 * @return
	 */
	public String download(URL url) {
		return download(url, 0);
	}
	
	/**
	 * 以字符串的形式返回请求内容
	 * 
	 * @param urlString
	 * @return
	 */
	public String download(URL url, int retryTimes) {
		CloseableHttpResponse response = null;
		try {
			HttpGet httpGet = new HttpGet(url.toURI());
			httpGet.setConfig(config);
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "gbk");
			EntityUtils.consume(entity);
			retryTimes ++;
			return responseString;
		} catch (ConnectionClosedException e) {
			logger.info("网络异常，重试中。。。" + retryTimes);
			if(retryTimes <= 5) {
				return download(url, retryTimes);
			} else {
				logger.info("网络异常，我认命了");
				return null;
			}
		}catch (IOException | URISyntaxException e) {
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

	/**
	 * 下载文件,保存到指定的文件夹下
	 * 
	 * @param urlString
	 * @param savePath
	 */
	public void download(String urlString, String savePath) {
		try {
			download(new URL(urlString), savePath);
		} catch (MalformedURLException e) {
			throw new ApplicationRuntimeException("不是正确的URL字符串", e);
		}

	}

	/**
	 * 下载并保存到指定的文件夹下
	 * 
	 * @param url
	 * @param savePath
	 * @return 保存的文件
	 */
	public File download(URL url, String savePath) {
		return download(url, savePath, 0);
	}
	
	/**
	 * 下载并保存到指定的文件夹下
	 * 
	 * @param url
	 * @param savePath
	 * @return 保存的文件
	 */
	public File download(URL url, String savePath, int retryTimes) {
		HttpGet httpGet;
		try {
			httpGet = new HttpGet(url.toURI());
			httpGet.setConfig(config);
			FileDownloadResponseHandler rh = new FileDownloadResponseHandler(
					url, savePath);
			retryTimes ++;
			return httpClient.execute(httpGet, rh);

		} catch (ConnectionClosedException e) {
			logger.info("网络异常，重试中。。。" + retryTimes);
			if(retryTimes <= 5) {
				return download(url, savePath, retryTimes);
			} else {
				logger.info("网络异常，我认命了");
				return null;
			}
		} catch (URISyntaxException e) {
			throw new ApplicationRuntimeException("不是正确的URI格式", e);
		} catch (ClientProtocolException e) {
			throw new ApplicationRuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new ApplicationRuntimeException("下载文件时发生网络异常", e);
		}

	}

	private class FileDownloadResponseHandler implements ResponseHandler<File> {

		private String savePath;
		private URL requestURL;

		public FileDownloadResponseHandler(URL requestURL, String savePath) {
			super();
			this.requestURL = requestURL;
			this.savePath = savePath;
		}

		public File handleResponse(HttpResponse response) {
			FileOutputStream out = null;
			try {
				String fileName = generateFileName(response);
				BufferedHttpEntity buffer;
				buffer = new BufferedHttpEntity(response.getEntity());
				File saveFile = new File(savePath, fileName);
				out = new FileOutputStream(saveFile);
				buffer.writeTo(out);
				return saveFile;
			} catch (IOException e) {
				throw new ApplicationRuntimeException("保存文件时发生异常", e);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
					}
				}
			}

		}

		private String generateFileName(HttpResponse response) {
			String fileName = null;
			Header[] headers = response.getHeaders("Content-Disposition");
			if (headers.length > 0) {
				String value = headers[0].getValue();
				Pattern p = Pattern.compile(".*filename=\"(.*)\"");
				Matcher matcher = p.matcher(value);

				if (matcher.find()) {
					String str = matcher.group(1);
					try {
						fileName = new String(str.getBytes("ISO8859-1"), "gbk");
						fileName = fileName
								.replaceAll("[\\/:\\*\\?<>\\|]", "_");
					} catch (UnsupportedEncodingException e) {
						// don't need to handle this exception
					}
				}
			}
			if (fileName == null) {
				fileName = DigestUtils.md5Hex(requestURL.toString());
			}
			return fileName;
		}

	}

}
