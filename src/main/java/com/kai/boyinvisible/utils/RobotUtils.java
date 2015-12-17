package com.kai.boyinvisible.utils;

import java.net.URL;

import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import com.kai.boyinvisible.downloader.Downloader;
import com.kai.boyinvisible.downloader.DownloaderFactory;

/**
 * 工具类
 */
public class RobotUtils {

	/**
	 * 根据网页内容返回编码方式,默认返回utf-8
	 *
 	 * @param String html
	 * @return String 编码方式
	 */
	public static String getCharset(String html) {
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
	

	public static String getCharset(URL url) {
		Downloader downloader = DownloaderFactory.createDownloader(url.toString());
		return getCharset(downloader.fetch(url));
	}

	public static String getCharset(URL url, CloseableHttpClient httpClient) {
		Downloader downloader = DownloaderFactory.createDownloader(url.toString(), httpClient);
		return getCharset(downloader.fetch(url));
	}
}
