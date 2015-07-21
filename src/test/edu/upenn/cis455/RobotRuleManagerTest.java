package test.edu.upenn.cis455;

import edu.upenn.cis455.crawler.RobotRuleManager;
import junit.framework.TestCase;

public class RobotRuleManagerTest extends TestCase {

	public void testGetRelativeURL() {
		assertEquals("/abc",
				RobotRuleManager.getRelativeURL("www.google.com/abc"));
		assertEquals("/~cis455/assignments",
				RobotRuleManager.getRelativeURL("http://www.cis.upenn.edu/~cis455/assignments"));
	}

	public void testGetHostName() {
		assertEquals("www.google.com",
				RobotRuleManager.getHostName("www.google.com/abc"));
		assertEquals("www.cis.upenn.edu",
				RobotRuleManager.getHostName("http://www.cis.upenn.edu/~cis455/assignments"));
	}

	public void testGetRobotURL() {
		assertEquals("http://www.google.com/robots.txt",
				RobotRuleManager.getRobotURL("www.google.com/abc"));
		assertEquals("http://www.cis.upenn.edu/robots.txt",
				RobotRuleManager.getRobotURL("http://www.cis.upenn.edu/~cis455/assignments"));
	}

}
