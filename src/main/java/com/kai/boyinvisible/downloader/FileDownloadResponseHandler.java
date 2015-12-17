package com.kai.boyinvisible.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.BufferedHttpEntity;
import org.springframework.util.StringUtils;

import com.kai.boyinvisible.exception.ApplicationRuntimeException;
import com.kai.boyinvisible.utils.RobotUtils;

public class FileDownloadResponseHandler implements ResponseHandler<File> {

	private static Log logger = LogFactory.getLog(FileDownloadResponseHandler.class);

	private String charset;
	private String savePath;
	private String requestURL;
	private String saveName;

	public FileDownloadResponseHandler(String charset, String url, String savePath, String saveName) {
		this.charset = charset;
		this.requestURL = url;
		this.savePath = savePath;
		this.saveName = saveName;
	}

	public File handleResponse(HttpResponse response) {
		logger.info("文件下载开始...");
		FileOutputStream out = null;
		try {
			String fileName = generateFileName(response);
			BufferedHttpEntity buffer;
			buffer = new BufferedHttpEntity(response.getEntity());
			File saveFile = new File(savePath, fileName);
			logger.info("文件保存为:" + saveFile.getAbsolutePath());
			out = new FileOutputStream(saveFile);
			buffer.writeTo(out);
			logger.info("文件下载完成。");
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
		if (StringUtils.hasText(saveName)) {
			return saveName;
		}

		String fileName = null;
		Header[] headers = response.getHeaders("Content-Disposition");
		if (headers.length > 0) {
			String value = headers[0].getValue();
			Pattern p = Pattern.compile(".*filename=\"(.*)\"");
			Matcher matcher = p.matcher(value);

			if (matcher.find()) {
				String str = matcher.group(1);
				try {
					try {
						charset = RobotUtils.getCharset(new URL(requestURL));
					} catch (MalformedURLException e) {
						charset = "UTF-8";
					}
					fileName = new String(str.getBytes("ISO8859-1"), charset);
				} catch (UnsupportedEncodingException e) {
					// don't need to handle this exception
				}
			}
		}
		if (fileName == null) {
			fileName = DigestUtils.md5Hex(requestURL);
		}
		return fileName;
	}

}
