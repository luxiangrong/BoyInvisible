package com.kai.boyinvisible;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class MainTest {

	@Test
	public void testLogin() throws Exception {
		SexInSexBot bot = new SexInSexBot("丽沙霞", "1qazxsw2");
	}
	
	@Test 
	public void testDownloadAll() {
		SexInSexBot bot = new SexInSexBot("丽沙霞", "1qazxsw2");
		bot.downloadAll("http://www.sexinsex.net/bbs/forumdisplay.php?fid=230&filter=type&typeid=634&page=1");
	}

	@Test
	public void testDownloadFromListPage() {
		SexInSexBot bot = new SexInSexBot("丽沙霞", "1qazxsw2");
		bot.downloadFromListPage("http://www.sexinsex.net/bbs/forumdisplay.php?fid=143");
	}
	
	@Test
	public void testDownload() throws Exception {
		SexInSexBot bot = new SexInSexBot("丽沙霞", "1qazxsw2");
		bot.downloadDirectly("http://www.sexinsex.net/bbs/forumdisplay.php?fid=230");
	}
	
	@Test
	public void testDownloadFromDetailPage() {
		SexInSexBot bot = new SexInSexBot("丽沙霞", "1qazxsw2");
		bot.downloadFromDetailPage("http://www.sexinsex.net/bbs/viewthread.php?tid=6051501&extra=page%3D1%26amp%3Bfilter%3Dtype%26amp%3Btypeid%3D634");
	}

	@Test
	public void testProxy() throws ClientProtocolException, IOException {
		HttpHost proxy = new HttpHost("127.0.0.1", 8580, "http");
		String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.2)";

		CloseableHttpClient httpclient = HttpClients.custom().setUserAgent("userAgent").setProxy(proxy).build();
		try {
			Properties p = new Properties(System.getProperties());
			p.put("http.agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
			System.setProperties(p);
			HttpHost target = new HttpHost("www.sexinsex.net", 80, "http");
			//			HttpHost proxy = new HttpHost("127.0.0.1", 8087, "http");

			RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
			HttpGet request = new HttpGet("/bbs/index.php");
			//			request.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.2)");
			//			request.setConfig(config);

			System.out.println("executing request to " + target + " via " + proxy);
			CloseableHttpResponse response = httpclient.execute(target, request);
			try {
				HttpEntity entity = response.getEntity();

				System.out.println("----------------------------------------");
				System.out.println(response.getStatusLine());
				Header[] headers = response.getAllHeaders();
				for (int i = 0; i < headers.length; i++) {
					System.out.println(headers[i]);
				}
				System.out.println("----------------------------------------");

				if (entity != null) {
					System.out.println(EntityUtils.toString(entity));
				}
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}
}
