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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.kai.boyinvisible.downloader.Downloader;
import com.kai.boyinvisible.downloader.DownloaderFactory;
import com.kai.boyinvisible.exception.ApplicationRuntimeException;
import com.kai.boyinvisible.utils.RobotUtils;

public class SexInSexRobot extends Robot {

	private static String PROXY_HOST = "http://127.0.0.1";
	private static int PROXY_PROT = 8580;
	private static String BASE_URL = "http://www.sexinsex.net";
	private static String LOGIN_URL = "http://www.sexinsex.net/bbs/logging.php?action=login&loginsubmit=true";

	private static Log logger = LogFactory.getLog(SexInSexRobot.class);

	public SexInSexRobot() {
		super();
	}

	public SexInSexRobot(String username, String password) {
		this();
		login(LOGIN_URL, username, password);
	}

	@Override
	protected HttpClientBuilder custom(HttpClientBuilder builder) {
		HttpHost proxy = new HttpHost(PROXY_HOST, PROXY_PROT);
		builder.setProxy(proxy);
		return builder;
	}

	/**
	 * 根据附件所在页面的url下载附件
	 * 
	 * @param detailPageUrl
	 */
	public void downloadFromDetailPage(String detailPageUrl) {
		Downloader downloader = DownloaderFactory.createDownloader(detailPageUrl, httpClient);
		String html = downloader.fetch(detailPageUrl);

		Document doc = Jsoup.parse(html);
		Elements attachmentLinks = doc.select("div.postattachlist dl dt a[target]");
		for (Element element : attachmentLinks) {
			try {
				URL attachmentUrl = new URL(new URL(detailPageUrl), element.attr("href"));
				DownloaderFactory.createDownloader(attachmentUrl.toString(), httpClient).download(attachmentUrl.toString(), DEFAULT_SAVE_DIRECTORY_PATH, null);
			} catch (MalformedURLException e) {
				throw new ApplicationRuntimeException("请求的url格式不正确");
			}
		}
	}

	@Override
	protected void login(String loginUrl, String username, String password) {

		HttpPost httpost = new HttpPost(loginUrl);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("cookietime", "2592000"));
		nvps.add(new BasicNameValuePair("loginfield", "username"));
		nvps.add(new BasicNameValuePair("userlogin", "true"));
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));

		CloseableHttpResponse response = null;

		try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps, RobotUtils.getCharset(new URL(loginUrl))));
			response = httpClient.execute(httpost);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, charset);
			EntityUtils.consume(entity);
			if (responseString.indexOf("欢迎您回来，" + username + "。现在将转入登录前页面。") != -1) {
				logger.info("模拟登录成功");
			} else {
				logger.info("模拟登录失败");
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

	@Override
	protected String detectCharset() {
		try {
			return RobotUtils.getCharset(new URL(BASE_URL), httpClient);
		} catch (MalformedURLException e) {
			return "UTF-8";
		}
	}

}
