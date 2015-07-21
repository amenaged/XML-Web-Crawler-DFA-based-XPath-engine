package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.CrawlerStatistic;
import edu.upenn.cis455.storage.DBWrapper;

public class CrawlerServlet extends HttpServlet {

	private static final long serialVersionUID = 5898587205820474622L;
	private static final String USERNAME = "USERNAME";
	private static final String ADMIN = "admin";
	private DBWrapper database;
	private String BDBpath;
	private XPathCrawler crawler;

	@Override
	public void destroy() {
		super.destroy();
		database.sync();
	}

	@Override
	public void init() throws ServletException {
		super.init();
		BDBpath = getServletContext().getInitParameter("BDBpath");
		database = new DBWrapper(BDBpath);
		database.start();
		crawler = new XPathCrawler();
		crawler.setDatabasePath(BDBpath);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (!ADMIN.equals(req.getSession().getAttribute(USERNAME))) {
			resp.sendError(403);
			return;
		}
		PrintWriter pw = resp.getWriter();
		ServletHelper.WriteHeader(pw, "Crawler Interface");
		if ("true".equals(req.getParameter("stop"))) {
			// stop crawler
			crawler.stop();
			resp.sendRedirect("crawler");
		}
		if (!crawler.isRunning()) {
			pw.write("<h4>Crawler Monitor</h4>");
			pw.print("<form action=\"\" method=\"post\">");
			pw.write("Start URL: <br>");
			pw.write("<input type=\"text\" name=\"starturl\">");
			pw.write("<br>");
			pw.write("Maximum File Size:(In megabytes) <br>");
			pw.write("<input type=\"text\" name=\"sizelimit\">");
			pw.write("<br>");
			pw.write("Number Of Files: <br>");
			pw.write("<input type=\"text\" name=\"filenumber\">");
			pw.write("<br><br>");
			pw.write("<input type=\"submit\" value=\"Start Crawler\">");
			pw.write("</form>");
			pw.write("<br>");
			pw.print("<a href=\"homepage\"><button type=\"button\">Home Page</button></a>");
		} else {
			ServletHelper.WriteHeader(pw, "Crawler Interface");
			pw.write("<h4>Crawler is Running...</h4>");
			ServletHelper.WriteTail(pw);
			pw.write("<a href=\"crawler?stop=true\"><button type=\"button\">Stop Crawler</button></a>");
			pw.write("<br>");
			pw.print("<a href=\"homepage\"><button type=\"button\">Home Page</button></a>");
			pw.write("<p>Number of HTML pages processed during this run: "
					+ crawler.getCurrentHtmlCount() + "</p>");
			pw.write("<p>Number of XML documents processed during this run: "
					+ crawler.getCurrentXMLCount() + "</p>");
		}
		CrawlerStatistic crawlerInfo = crawler.getCrawlerInfo();
		List<Channel> channels = database.getChannels();
		pw.write("<p>Total Number of HTML Retrieved: "
				+ crawlerInfo.getHtmlNumber() + "</p>");
		pw.write("<p>Total Number of XML documents retrieved: "
				+ crawlerInfo.getXmlNumber() + "</p>");
		pw.write("<p>Total Number of Servers visitied: "
				+ crawlerInfo.getServerCount() + "</p>");
		pw.write("<p>Amount of data downloaded: "
				+ crawlerInfo.getRetrieveDataAmount() + " MB</p>");
		pw.print("<table border=\"1\" width=\"80%\" align=\"middle\"><col width=\"50%\" /><col width=\"50%\" />");
		pw.print("<tr align=\"middle\"><th>Channel Name</th><th>Number of XML files</th></tr>");
		for (Channel channel : channels) {
			pw.write("<tr align=\"middle\"><td>" + channel.getName()
					+ "</td><td>" + channel.getMatchedFile().size() + "</td>");
		}
		pw.print("</table>");
		ServletHelper.WriteTail(pw);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (crawler.isRunning()) {
			resp.sendRedirect("crawler");
			return;
		}
		try {
			// PrintWriter pw = resp.getWriter();
			String url = req.getParameter("starturl");
			double sizeLimit = Double
					.parseDouble(req.getParameter("sizelimit"));
			int fileNumber = Integer.parseInt(req.getParameter("filenumber"));
			List<String> seedURLs = new LinkedList<String>();
			seedURLs.add(url.trim());
			crawler.setSeed(seedURLs);
			crawler.setFileLimit(fileNumber);
			crawler.setFileSizeLimit(sizeLimit);
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						crawler.start();
					} catch (ParserConfigurationException | SAXException e) {
						e.printStackTrace();
					}
				}
			});
			thread.start();
			resp.sendRedirect("crawler");
		} catch (Exception e) {
			resp.sendRedirect("crawler");
			e.printStackTrace();
		}

	}

}
