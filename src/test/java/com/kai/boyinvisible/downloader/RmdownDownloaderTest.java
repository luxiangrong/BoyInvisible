package com.kai.boyinvisible.downloader;

import org.junit.Test;

public class RmdownDownloaderTest {

	@Test
	public void testDownload() {
		Downloader downloader = new RmdownDownloader();
		downloader.download("http://www.rmdown.com/link.php?hash=14118374324c7ef54896a048e5b63e0aa3881f1ba0f", "src/test/resources", null);
	}

}
