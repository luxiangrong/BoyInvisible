package com.kai.boyinvisible;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import com.kai.boyinvisible.exception.ApplicationRuntimeException;

public class SexInSexBot {

	private static String CHARSET = "GBK";
	private static String PROXY_HOST = "127.0.0.1";
	private static int PROXY_PROT = 1080;
	private static String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.2)";
	private static String LOGIN_URL = "http://www.sexinsex.net/bbs/logging.php?action=login&loginsubmit=true";

	private static String DEFAULT_SAVE_DIRECTORY_PATH = "src/test/resources";
	private static String DEFAULT_TEMP_DIRECTORY_PATH = "";

	private String saveDirectoryPath = DEFAULT_SAVE_DIRECTORY_PATH;
	private String tempDirectoryPath = DEFAULT_TEMP_DIRECTORY_PATH;

	private static Log logger = LogFactory.getLog(SexInSexBot.class);

	private CloseableHttpClient httpClient;
	private RequestConfig config;

	public SexInSexBot() {
		this(null, null);
	}

	public SexInSexBot(String username, String password) {
		HttpHost proxy = new HttpHost(PROXY_HOST, PROXY_PROT, "http");
		httpClient = HttpClients.custom().disableContentCompression()
				.setUserAgent(USER_AGENT).setProxy(proxy).build();
		config = RequestConfig.custom().setSocketTimeout(20000)
				.setConnectTimeout(20000).build();
		// httpClient =
		// HttpClients.custom().disableContentCompression().setUserAgent(USER_AGENT).build();

		login(LOGIN_URL, username, password);
	}

	public void downloadAll(String pagnationUrl) {
		downloadFromListPage(pagnationUrl.toString());

		HttpDownloader downloader = new HttpDownloader(httpClient);
		String html = downloader.download(pagnationUrl);

		Document doc = Jsoup.parse(html);
		Elements pagnations = doc.select("div.threadlist + div.pages_btns");
		Elements nextPageLinks = pagnations.select("a.next");
		for (Element element : nextPageLinks) {
			try {
				URL nextListPage = new URL(new URL(pagnationUrl),
						element.attr("href"));
				logger.info("进入下一页" + nextListPage.toString() + "并下载");
				downloadAll(nextListPage.toString());
			} catch (MalformedURLException e) {
				throw new ApplicationRuntimeException("请求的url格式不正确");
			}
		}
	}

	public void downloadFromListPage(String listPageUrl) {
		HttpDownloader downloader = new HttpDownloader(httpClient);
		String html = downloader.download(listPageUrl);

		Document doc = Jsoup.parse(html);
		Elements detailPageLinks = doc
				.select("div.threadlist form table tr td.folder a");
		logger.info("本列表页共有" + detailPageLinks.size() + "条帖子");
		for (Element element : detailPageLinks) {
			try {
				URL detailPageURL = new URL(new URL(listPageUrl),
						element.attr("href"));
				downloadFromDetailPage(detailPageURL.toString());
			} catch (MalformedURLException e) {
				throw new ApplicationRuntimeException("请求的url格式不正确");
			}
		}
	}

	/**
	 * 根据附件所在页面的url下载附件
	 * 
	 * @param detailPageUrl
	 */
	public void downloadFromDetailPage(String detailPageUrl) {
		logger.info("进入详细页：" + detailPageUrl);
		HttpDownloader downloader = new HttpDownloader(httpClient);

		try {
			String html = downloader.download(detailPageUrl);
			Document doc = Jsoup.parse(html);
			Elements attachmentLinks = doc
					.select("div.postattachlist dl dt a[target]");
			for (Element element : attachmentLinks) {
				try {
					URL attachmentUrl = new URL(new URL(detailPageUrl),
							element.attr("href"));
					downloadDirectly(attachmentUrl.toString());
				} catch (MalformedURLException e) {
					throw new ApplicationRuntimeException("请求的url格式不正确");
				}
			}
		} catch (Throwable e) {

		}
	}

	/**
	 * 直接根据附件的url下载
	 * 
	 * @param downloadUrl
	 */
	public void downloadDirectly(String downloadUrl) {
		logger.info("开始下载");
		HttpDownloader downloader = new HttpDownloader(httpClient);
		try {
			downloader.download(downloadUrl, saveDirectoryPath);
		} catch (Throwable e) {

		}
		logger.info("下载成功");
	}

	private boolean login(String loginUrl, String username, String password) {
		// 如果没有设置用户名或密码，不需要登录
		if (!StringUtils.hasText(username + password)) {
			logger.debug("没有设置用户名或密码，不需要登录");
			return true;
		}

		HttpPost httpost = new HttpPost(loginUrl);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("cookietime", "2592000"));
		nvps.add(new BasicNameValuePair("loginfield", "username"));
		nvps.add(new BasicNameValuePair("userlogin", "true"));
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));

		CloseableHttpResponse response = null;

		try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps, CHARSET));
			response = httpClient.execute(httpost);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "gbk");
			EntityUtils.consume(entity);
			if (responseString.indexOf("欢迎您回来，" + username + "。现在将转入登录前页面。") != -1) {
				logger.info("模拟登录成功");
				return true;
			} else {
				logger.info("模拟登录失败");
				return false;
			}

		} catch (IOException e) {
			throw new ApplicationRuntimeException("登录失败，发生网络错误", e);
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

	public String getSaveDirectoryPath() {
		return saveDirectoryPath;
	}

	public void setSaveDirectoryPath(String saveDirectoryPath) {
		this.saveDirectoryPath = saveDirectoryPath;
	}

	public String getTempDirectoryPath() {
		return tempDirectoryPath;
	}

	public void setTempDirectoryPath(String tempDirectoryPath) {
		this.tempDirectoryPath = tempDirectoryPath;
	}

}
