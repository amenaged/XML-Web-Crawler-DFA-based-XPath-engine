package edu.upenn.cis455.crawler;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * Abstraction of RobotRuleManager.
 * All robots.txt is buffered in memory. 
 * Crawler should ask RobotRuleManager for corresponding robots.txt files
 * 
 * If file does not exist, download one from website.
 * 
 */
public class RobotRuleManager {

	private Map<String, RobotRule> robotRuleMap;
	private Map<String, Long> hostNameCrawlTimeMap;
	private static Pattern urlPattern = Pattern
			.compile("(http://|https://)?(.+?)(/.*)?");

	public RobotRuleManager() {
		robotRuleMap = new HashMap<String, RobotRule>();
		hostNameCrawlTimeMap = new HashMap<String, Long>();
	}

	private RobotRule getRobot(String URL) {
		String hostName = getHostName(URL);
		RobotRule robotRule = robotRuleMap.get(hostName);
		if (robotRule != null) {
			// hostNameCrawlTimeMap.put(hostName, System.currentTimeMillis());
			return robotRule.isEmpty() ? null : robotRule;
		}
		robotRule = new RobotRule(getRobotURL(URL));
		robotRuleMap.put(hostName, robotRule);
		hostNameCrawlTimeMap.put(hostName, -1L);
		return robotRule.isEmpty() ? null : robotRule;
	}

	public static String getRelativeURL(String URL) {
		URL = URL.trim();
		Matcher m = urlPattern.matcher(URL);
		if (m.matches()) {
			return m.group(3) == null ? "/" : m.group(3);
		}
		return null;
	}

	public static String getHostName(String URL) {
		URL = URL.trim();
		Matcher m = urlPattern.matcher(URL);
		return m.matches() ? m.group(2) : null;
	}

	public static String getRobotURL(String URL) {
		// System.out.println(URL);
		URL = URL.trim();
		Matcher m = urlPattern.matcher(URL);
		if (m.matches())
			return (m.group(1) == null ? "http://" : m.group(1)) + m.group(2)
					+ "/robots.txt";
		return null;
	}

	// public boolean isInDelay(String URL) {
	// String hostName = getHostName(URL);
	// RobotRule robotRule = getRobot(URL);
	// if (robotRule == null) {
	// hostNameCrawlTimeMap.put(hostName, System.currentTimeMillis());
	// return false;
	// }
	// if (System.currentTimeMillis() - hostNameCrawlTimeMap.get(hostName) <
	// robotRule
	// .getDelay())
	// return true;
	// hostNameCrawlTimeMap.put(hostName, System.currentTimeMillis());
	// return false;
	// }

	// version 1: update crawl time
	public boolean isAllowed1(String URL) {
		String hostName = getHostName(URL);
		RobotRule robotRule = getRobot(URL);
		if (robotRule == null)
			return true;
		if (System.currentTimeMillis() - hostNameCrawlTimeMap.get(hostName) < robotRule
				.getDelay())
			return false;
		String relativeURL = getRelativeURL(URL);
		for (String url : robotRule.getAllows()) {
			if (relativeURL.startsWith(url)) {
				if (url.endsWith("/")) {
					hostNameCrawlTimeMap.put(hostName,
							System.currentTimeMillis());
					return true;
				}
				if (relativeURL.length() == url.length()
						|| relativeURL.indexOf(url.length()) == '/'
						|| relativeURL.indexOf(url.length()) == '.') {
					hostNameCrawlTimeMap.put(hostName,
							System.currentTimeMillis());
					return true;
				}
			}
		}
		for (String url : robotRule.getDisallows())
			if (relativeURL.startsWith(url)) {
				if (url.endsWith("/"))
					return false;
				if (relativeURL.length() == url.length()
						|| relativeURL.indexOf(url.length()) == '/'
						|| relativeURL.indexOf(url.length()) == '.')
					return false;
			}
		hostNameCrawlTimeMap.put(hostName, System.currentTimeMillis());
		return true;
	}

	// version 2: not update crawl time
	public boolean isAllowed2(String URL) {
		RobotRule robotRule = getRobot(URL);
		if (robotRule == null)
			return true;
		String relativeURL = getRelativeURL(URL);
		for (String url : robotRule.getAllows())
			if (relativeURL.startsWith(url)) {
				if (url.endsWith("/"))
					return true;
				if (relativeURL.length() == url.length()
						|| relativeURL.indexOf(url.length()) == '/'
						|| relativeURL.indexOf(url.length()) == '.')
					return true;
			}
		for (String url : robotRule.getDisallows())
			if (relativeURL.startsWith(url)) {
				if (url.endsWith("/"))
					return false;
				if (relativeURL.length() == url.length()
						|| relativeURL.indexOf(url.length()) == '/'
						|| relativeURL.indexOf(url.length()) == '.')
					return false;
			}
		return true;
	}

	public static void main(String[] args) {
		// System.out.println(getRobotURL("https://www.google.com/123/123"));
		System.out.println(getRelativeURL("www.google.com/123/123	"));
	}

}
