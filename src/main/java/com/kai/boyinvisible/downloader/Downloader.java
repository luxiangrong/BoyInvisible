package com.kai.boyinvisible.downloader;

import java.io.File;
import java.net.URL;

public interface Downloader {
	
	
	/**
	 * 抓取指定页面内容，并以字符串的形式返回
	 * 
	 * @param url
	 * @return
	 */
	String fetch(String url);
	
	String fetch(URL url);
	
	/**
	 * 根据url下载文件
	 * 
	 * @param url
	 * @param savePath
	 * @param saveName
	 * @return
	 */
	File download(String url, String savePath, String saveName);
}
