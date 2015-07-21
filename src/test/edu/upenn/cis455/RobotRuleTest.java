package test.edu.upenn.cis455;

import junit.framework.TestCase;
import edu.upenn.cis455.crawler.RobotRule;

public class RobotRuleTest extends TestCase {

	public void testIsEmpty() {
		RobotRule rule1 = new RobotRule("www.ababa.com/test");
		assertTrue(rule1.isEmpty());
		RobotRule rule2 = new RobotRule(
				"https://dbappserv.cis.upenn.edu/robots.txt");
		assertFalse(rule2.isEmpty());
	}

	public void testGetDelay() {
		RobotRule rule = new RobotRule(
				"https://dbappserv.cis.upenn.edu/robots.txt");
		assertEquals(5000, rule.getDelay());
	}

	public void testGetAllows() {
		RobotRule rule = new RobotRule(
				"https://dbappserv.cis.upenn.edu/robots.txt");
		assertTrue(rule.getAllows().isEmpty());
	}

	public void testGetDisallows() {
		RobotRule rule = new RobotRule(
				"https://dbappserv.cis.upenn.edu/robots.txt");
		assertTrue(rule.getDisallows().contains("/maven/"));
		assertTrue(rule.getDisallows().contains("/crawltest/foo/"));
	}

}
