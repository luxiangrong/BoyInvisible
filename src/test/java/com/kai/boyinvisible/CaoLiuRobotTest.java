package com.kai.boyinvisible;

import org.junit.Test;

public class CaoLiuRobotTest {

	@Test 
	public void testDownloadFromDetailPage() {
		CaoLiuRobot caoLiuRobot = new CaoLiuRobot();
		caoLiuRobot.downloadFromDetailPage("http://wo.yao.cl/htm_data/15/1411/1277278.html");
	}
	
	@Test 
	public void testDownloadFromListPage() {
		CaoLiuRobot caoLiuRobot = new CaoLiuRobot();
		caoLiuRobot.downloadFromListPage("http://wo.yao.cl/thread0806.php?fid=15&search=&page=6");
	}
	
}
