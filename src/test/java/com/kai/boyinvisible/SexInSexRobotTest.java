package com.kai.boyinvisible;

import org.junit.Test;

public class SexInSexRobotTest {
	@Test
	public void testDownloadFromDetailPage() {
		SexInSexRobot sexInSexRobot = new SexInSexRobot("丽沙霞", "1qazxsw2");
		sexInSexRobot.downloadFromDetailPage("http://www.sexinsex.net/bbs/thread-5566906-1-1.html");
	}

	@Test
	public void testDownloadFromListPage() {
		SexInSexRobot sexInSexRobot = new SexInSexRobot("丽沙霞", "1qazxsw2");
		sexInSexRobot.downloadFromDetailPage("http://www.sexinsex.net/bbs/thread-5566906-1-1.html");
	}
}
