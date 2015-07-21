package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.CrawledFile;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

public class DisplayChannelServlet extends HttpServlet {

	private static final long serialVersionUID = -3602872228309807820L;
	private static final String USERNAME = "USERNAME";
	private static final SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'hh:mm:ss");
	private static final Pattern instructionPattern = Pattern
			.compile("(\\s*<\\?xml.*\\?>\\s*)*(.+)?");
	private DBWrapper database;
	private String BDBpath;

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
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String channelName = req.getParameter("channelname");
		HttpSession session = req.getSession();
		resp.setCharacterEncoding("ISO-8859-1");
		PrintWriter pw = resp.getWriter();
		if (channelName == null) {
			ServletHelper.WriteHeader(pw, "Error Displaying Channel");
			pw.write("<p>No Channel Selected.</p>");
			ServletHelper.WriteTail(pw);
			return;
		}
		Channel channel = database.getChannel(channelName);
		if (channel == null) {
			ServletHelper.WriteHeader(pw, "Error Displaying Channel");
			pw.write("<p>Channel's not existed in the database.</p>");
			ServletHelper.WriteTail(pw);
			return;
		}
		User user = null;
		if (session.getAttribute(USERNAME) != null) {
			user = database.getUser((String) session.getAttribute(USERNAME));
			user.viewChannel(channelName);
			database.saveUser(user);
			database.sync();
		}
		resp.setContentType("text/xml");
		resp.setCharacterEncoding("ISO-8859-1");
		pw.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		pw.println("<?xml-stylesheet type=\"text/xsl\" href=\""
				+ channel.getXSL() + "\"?>");
		// pw.println("<?xml-stylesheet type=\"text/xsl\" href=\"rss/rss.xsl\"?>");
		pw.println("<documentcollection>");
		for (String fileName : channel.getMatchedFile().keySet()) {
			CrawledFile file = database.getFile(fileName);
			pw.println("<document crawled=\""
					+ formatter.format(new Date(file.getLastCrawled()))
					+ "\" location=\"" + file.getURL() + "\">");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					file.getContent(), file.getCharSet()));
			String line;
			while ((line = br.readLine()) != null) {
				Matcher m = instructionPattern.matcher(line);
				if (m.matches() && m.group(2) != null) {
					pw.println(m.group(2));
				}
			}
			br.close();
			pw.println("</document>");
			// break;
		}
		pw.println("</documentcollection>");
	}
}
