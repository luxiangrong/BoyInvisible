package com.kai.boyinvisible.downloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import com.kai.boyinvisible.exception.ApplicationRuntimeException;

public class RmdownDownloader extends DefaultDownloader {

	private static Log logger = LogFactory.getLog(RmdownDownloader.class);

	public RmdownDownloader() {
		super();
	}

	public RmdownDownloader(CloseableHttpClient httpClient) {
		super(httpClient);
	}

	@Override
	public File download(String url, String savePath, String saveName) {
		String html = fetch(url);

		Document doc = Jsoup.parse(html);
		Elements forms = doc.select("form");
		if (forms.isEmpty()) {
			throw new ApplicationRuntimeException("网盘 rmdown 页面解析发生错误");
		}

		forms.last().setBaseUri(url);
		String actionUrl = forms.last().absUrl("action");

		HttpPost httpost = new HttpPost(actionUrl);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		Elements elements = doc.select("input");
		for (Element element : elements) {
			nvps.add(new BasicNameValuePair(element.attr("name"), element.attr("value")));
		}

		CloseableHttpResponse response = null;

		try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps, getCharset(html)));
			FileDownloadResponseHandler rh = new FileDownloadResponseHandler(getCharset(html), url, savePath, saveName);
			logger.info("开始下载文件" + saveName);
			return httpClient.execute(httpost, rh);

		} catch (IOException e) {
			throw new ApplicationRuntimeException("下载失败，发生网络错误", e);
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

	private String getCharset(String html) {
		Document doc = Jsoup.parse(html);
		Elements eles = doc.select("meta[http-equiv=Content-Type]");
		if (eles.isEmpty()) {
			return "utf-8";
		}
		for (Element element : eles) {
			String content = element.attr("content");
			if (StringUtils.hasText(content)) {
				int index = content.lastIndexOf("charset=");
				if (index != -1) {
					return content.substring(index + 8);
				}
			}
		}
		return "utf-8";
	}

}
