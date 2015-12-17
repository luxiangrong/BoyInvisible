package com.kai.boyinvisible;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import com.kai.boyinvisible.downloader.Downloader;
import com.kai.boyinvisible.downloader.DownloaderFactory;
import com.kai.boyinvisible.exception.ApplicationRuntimeException;

public class CaoLiuRobot extends Robot {

	private static Log logger = LogFactory.getLog(CaoLiuRobot.class);

	@Override
	protected HttpClientBuilder custom(HttpClientBuilder builder) {
		return builder;
	}

	@Override
	protected void login(String loginUrl, String username, String password) {
	}

	public void downloadFromListPage(URL listPageURL) {
		HttpDownloader downloader = new HttpDownloader(httpClient);
		String html = downloader.download(listPageURL);

		Document doc = Jsoup.parse(html);
		Elements elements = doc.select("div.t td h3 a");

		for (Element element : elements) {
			logger.info("进入详细页" + element.absUrl("href"));
			element.setBaseUri(listPageURL.toString());
			downloadFromDetailPage(element.absUrl("href"));
		}
	}

	public void downloadFromListPage(String listPageURL) {
		try {
			downloadFromListPage(new URL(listPageURL));
		} catch (MalformedURLException e) {
			throw new ApplicationRuntimeException("不是正确的URL字符串", e);
		}
	}

	/**
	 * 从包含附件的帖子详细页面中解析出附件的下载地址， 并下载附件
	 * 
	 */
	public void downloadFromDetailPage(URL detailPageURL) {
		Downloader downloader = DownloaderFactory.createDownloader(detailPageURL.toString());

		String html = downloader.fetch(detailPageURL.toString());

		Document doc = Jsoup.parse(html);
		Elements elements = doc.select("div.tpc_content a");

		Elements titleElement = doc.select("title");
		String title = null;
		if (!titleElement.isEmpty()) {
			title = titleElement.last().text();
			title = title.replaceAll("草榴社區  - powered by phpwind.net", "");
			title = title.replaceAll("[\\/:\\*\\?<>\\|]", "_");
			title = StringUtils.trimWhitespace(title);
			title += ".torrent";
		}

		if (!elements.isEmpty()) {
			String downloadUrl = elements.last().text();
			try {
				URL downloadURL = null;
				if (downloadUrl.matches(":")) {
					downloadURL = new URL(downloadUrl);
				} else {
					downloadURL = new URL(detailPageURL, downloadUrl);
				}
				String savePath = generateSavePath(detailPageURL);

				DownloaderFactory.createDownloader(downloadURL.toString()).download(downloadURL.toString(), savePath,
						StringUtils.hasText(title) ? StringUtils.trimWhitespace(title) : null);
			} catch (MalformedURLException e) {
				throw new ApplicationRuntimeException("不是正确的URL字符串", e);
			}
		} else {
			logger.info("此页面没有发现附件，页面地址为：" + detailPageURL.toString());
		}
	}

	public void downloadFromDetailPage(String urlString) {
		try {
			downloadFromDetailPage(new URL(urlString));
		} catch (MalformedURLException e) {
			throw new ApplicationRuntimeException("不是正确的URL字符串", e);
		}
	}

	private String generateSavePath(URL detailPageURL) {
		String path = detailPageURL.getPath();
		String[] strings = path.split("/");
		String result = "";
		if (strings.length > 2) {
			result = DEFAULT_SAVE_DIRECTORY_PATH + "/" + strings[strings.length - 2];
		} else {
			result = DEFAULT_SAVE_DIRECTORY_PATH;
		}
		File directory = new File(result);
		if (!directory.isDirectory()) {
			directory.mkdir();
		}
		return result;
	}

}
