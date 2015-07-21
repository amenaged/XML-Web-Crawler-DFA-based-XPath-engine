package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.CrawledFile;
import edu.upenn.cis455.storage.CrawlerStatistic;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

/*
 * Main Entrance class for running the crawler.
 * Call below functions before start the crawler.
 * crawler.setSeed();
 * crawler.setDatabasePath();
 * crawler.setFileSizeLimit();
 * crawler.setFileLimit();
 * 
 * Note: crawler and servlets both require writting permission to the database. 
 * So they can not be running in two process at the same time.
 * 
 * 1. Start the server, run the crawler
 * 2. Start the crawler from the CrawlerServlet
 * 
 */
public class XPathCrawler {

	private SAXParser parser;
	private XPathEngineImpl xPathEngine;
	private RobotRuleManager robotManager;
	private Map<String, List<Channel>> xpathChannelMap;
	private String[] xpaths;
	private DBWrapper database;
	private LinkedList<String> URLFrontier;
	private Set<String> URLSet;
	private int crawleredFileCount;
	private int fileLimit;
	private long fileSizeLimit;

	private int currentHtmlFileCount;
	private int currentXMLFileCount;

	private static boolean isRunning;
	private static boolean stopFlag;
	private static CrawlerStatistic crawlerInfo;

	public XPathCrawler() {
		URLSet = new HashSet<String>();
		URLFrontier = new LinkedList<String>();
		xpathChannelMap = new HashMap<String, List<Channel>>();
		robotManager = new RobotRuleManager();
		crawleredFileCount = 0;
		fileSizeLimit = Long.MAX_VALUE;
		fileLimit = Integer.MAX_VALUE;
		isRunning = false;
		stopFlag = false;
	}

	private void reset() {
		URLSet = new HashSet<String>();
		xpathChannelMap = new HashMap<String, List<Channel>>();
		robotManager = new RobotRuleManager();
		crawleredFileCount = 0;
		currentHtmlFileCount = 0;
		currentXMLFileCount = 0;
	}

	public void stop() {
		stopFlag = true;
	}

	public void setDatabasePath(String path) {
		this.database = new DBWrapper(path);
		database.start();
		crawlerInfo = database.getCrawlerInfo();
		if (crawlerInfo == null)
			crawlerInfo = new CrawlerStatistic();
	}

	// The maximum size, in megabytes, of a document to be retrieved from a Web
	// server
	public void setFileSizeLimit(double limit) {
		this.fileSizeLimit = (int) (limit * 1_000_000);
	}

	public void setFileLimit(int count) {
		this.fileLimit = count;
	}

	public void setSeed(List<String> seedURLs) {
		URLFrontier.clear();
		URLFrontier.addAll(seedURLs);
	}

	private boolean isNewURL(String URL) {
		if (URL == null || URL.trim().length() == 0)
			return false;
		if (URLSet.contains(URL))
			return false;
		URLSet.add(URL);
		return true;
	}

	// fileType: 0 for html, 1 for xml
	private CrawledFile fetchFile(String URL, Client client, int fileType) {
		CrawledFile file = database.getFile(URL);
		// TODO: What if lastModified is not defined?
		if (file == null
				|| (client.getLastModified() < 0 || file.getLastCrawled() < client
						.getLastModified())) {
			client = Client.getInstance(URL);
			client.execute();
			System.out.println(URL + ": Downloading");
			if (fileType == 0)
				crawlerInfo.addHtmlNumber();
			else if (fileType == 1)
				crawlerInfo.addXmlNumber();
			file = new CrawledFile(URL, client.getCharSet(),
					System.currentTimeMillis(), client.getResponseBody());
			crawlerInfo.addDataAmount(file.getFileSize());
			client.closeConnection();
		} else {
			file.setLastCrawled(System.currentTimeMillis());
			System.out.println(URL + ": Not Modified");
		}
		database.saveFile(file);
		return file;
	}

	public CrawlerStatistic getCrawlerInfo() {
		return crawlerInfo;
	}

	private void process(String URL) {
		Client client = Client.getInstance(URL);
		if (client == null)
			return;
		client.setMethod("HEAD");
		if (!client.execute()) {
			client.closeConnection();
			return;
		}
		client.closeConnection();
		if (client.getContentType() == null || client.getStatusCode() >= 400
				|| client.getContentLength() > fileSizeLimit)
			return;
		if (client.getContentType().equals("text/html")) {
			CrawledFile file = fetchFile(URL, client, 0);
			currentHtmlFileCount++;
			try {
				InputStream is = file.getContent();
				Document doc = Jsoup.parse(is, file.getCharSet(), URL);
				Elements links = doc.select("a[href]");
				for (Element link : links) {
					String url = link.absUrl("href");
					// int index = url.indexOf("?");
					// if (index != -1 && url.charAt(index - 1) != '/')
					if (isNewURL(url) && robotManager.isAllowed2(url))
						URLFrontier.add(url);
				}
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (client.getContentType().matches(
				"text/xml|application/xml|.*+xml")) {
			CrawledFile file = fetchFile(URL, client, 1);
			currentXMLFileCount++;
			try {
				InputStream is = file.getContent();
				parser.parse(is, xPathEngine);
				boolean[] results = xPathEngine.evaluate(null);
				for (int i = 0; i < results.length; i++) {
					if (results[i]) {
						List<Channel> channels = xpathChannelMap.get(xpaths[i]);
						for (Channel channel : channels)
							channel.addMatchedFile(URL,
									client.getLastModified());
					}
				}
				is.close();
			} catch (IOException | SAXException e) {
				e.printStackTrace();
			}
		}
	}

	public void start() throws ParserConfigurationException, SAXException {
		if (isRunning) {
			System.err.println("Crawler is running...");
			return;
		}
		reset();
		System.out.println("Crawler is about to start...");
		isRunning = true;
		xPathEngine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
		parser = SAXParserFactory.newInstance().newSAXParser();
		List<Channel> channels = database.getChannels();
		for (Channel channel : channels) {
			for (String xpath : channel.getXPaths()) {
				List<Channel> lst = xpathChannelMap.get(xpath);
				if (lst == null) {
					lst = new LinkedList<Channel>();
					xpathChannelMap.put(xpath, lst);
				}
				lst.add(channel);
			}
		}
		xpaths = new String[xpathChannelMap.keySet().size()];
		int i = 0;
		for (String xpath : xpathChannelMap.keySet())
			xpaths[i++] = xpath;
		xPathEngine.setXPaths(xpaths);
		while (!stopFlag && !URLFrontier.isEmpty()
				&& crawleredFileCount <= fileLimit) {
			Iterator<String> iterator = URLFrontier.iterator();
			while (iterator.hasNext()) {
				String URL = iterator.next();
				if (robotManager.isAllowed1(URL)) {
					iterator.remove();
					process(URL);
					crawleredFileCount++;
					break;
				}
			}
		}
		System.out.println("Crawler is about to stop...");
		for (Channel channel : channels)
			database.saveChannel(channel);
		database.updateCrawlerInfo(crawlerInfo);
		database.sync();
		isRunning = false;
		stopFlag = false;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public int getCurrentHtmlCount() {
		return currentHtmlFileCount;
	}

	public int getCurrentXMLCount() {
		return currentXMLFileCount;
	}

	public static void main(String... args)
			throws ParserConfigurationException, SAXException {
		if (args.length < 3) {
			System.err.println("Yunchen Wei --> yunchenw@seas.upenn.edu");
			System.err
					.println("Parameters Error: [seed URL] [path] [maximum file size] ([file numbers])");
			System.exit(1);
		}
		XPathCrawler crawler = new XPathCrawlerFactory().getCrawler();
		String seedURL = args[0];
		List<String> seeds = new LinkedList<String>();
		seeds.add(seedURL);
		crawler.setSeed(seeds);
		String BDBpath = args[1];
		crawler.setDatabasePath(BDBpath);
		crawler.setFileSizeLimit(Double.parseDouble(args[2]));
		if (args.length > 3) {
			crawler.setFileLimit(Integer.parseInt(args[3]));
		}
		crawler.start();
		crawler.database.shutdown();
	}
}
