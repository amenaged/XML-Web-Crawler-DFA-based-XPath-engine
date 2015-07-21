package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

public class HomePageServlet extends HttpServlet {

	private static final long serialVersionUID = -4310362374515822550L;
	private static final String USERNAME = "USERNAME";
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
		HttpSession session = req.getSession();
		String username = (String) session.getAttribute(USERNAME);
		if (username == null) {
			resp.sendRedirect("signin");
			return;
		}
		User user = database.getUser(username);
		if (user == null) {
			resp.sendRedirect("signin");
			return;
		}
		List<Channel> channels = database.getChannels();
		PrintWriter pw = resp.getWriter();
		ServletHelper.WriteHeader(pw, "Welcome to Your Homepage");
		pw.print("<h2>Hi, " + username + "</h2>");
		pw.print("<a href=\"create\"><button type=\"button\">Create a Channel</button></a>");
		pw.print("<br>");
		pw.print("<a href=\"logout\"><button type=\"button\">Log Out</button></a>");
		pw.print("<br>");
		pw.write("<a href=\"crawler\"><button type=\"button\">Crawler Interface</button></a>");
		pw.print("<h4>Manage Your Channels (Deletion)</h4>");
		pw.print("<table border=\"1\" width=\"80%\" align=\"middle\"><col width=\"50%\" /><col width=\"50%\" />");
		pw.print("<tr align=\"middle\"><th>Channel Name</th><th>DELETE</th></tr>");
		for (Channel channel : channels) {
			if (username.equals(channel.getCreator()))
				pw.print("<tr align=\"middle\"><td><a href=\"display?channelname="
						+ channel.getName()
						+ "\">"
						+ channel.getName()
						+ "</a></td><td><a href=\"delete?channelname="
						+ channel.getName()
						+ "\"><button type=\"button\">DELETE</button></a></td></tr>");
		}
		pw.print("</table>");
		pw.print("<h4>Channels Suscribed</h4>");
		pw.print("<table border=\"1\" width=\"80%\" align=\"middle\"><col width=\"40%\" /><col width=\"30%\" /><col width=\"30%\" />");
		pw.print("<tr align=\"middle\"><th>Channel Name</th><th>UnSubscribe</th><th>Updated??</th></tr>");
		Map<String, Long> subscribes = user.getSubscribes();
		for (Entry<String, Long> subscribe : subscribes.entrySet()) {
			Channel channel = database.getChannel(subscribe.getKey());
			if (channel == null) {
				user.unSubscribe(subscribe.getKey());
			} else {
				pw.print("<tr align=\"middle\"><td><a href=\"display?channelname="
						+ subscribe.getKey()
						+ "\">"
						+ subscribe.getKey()
						+ "</a></td><td><a href=\"unsubscribe?channelname="
						+ subscribe.getKey()
						+ "\"><button type=\"button\">UNSUBSCRIBE</button></a></td><td>"
						+ (subscribe.getValue() < channel.getLastModified() ? "YES"
								: "NO") + "</td></tr>");
			}
		}
		database.saveUser(user);
		pw.print("</table>");
		pw.print("<h4>Subscribe Your Channels (Subscribe)</h4>");
		pw.print("<table border=\"1\" width=\"80%\" align=\"middle\"><col width=\"50%\" /><col width=\"50%\" />");
		pw.print("<tr align=\"middle\"><th>Channel Name</th><th>Subscribe</th></tr>");
		for (Channel channel : channels) {
			pw.print("<tr align=\"middle\"><td><a href=\"display?channelname="
					+ channel.getName()
					+ "\">"
					+ channel.getName()
					+ "</a></td><td><a href=\"subscribe?channelname="
					+ channel.getName()
					+ "\"><button type=\"button\">SUBSCRIBE</button></a></td></tr>");
		}
		pw.print("</table>");
		ServletHelper.WriteTail(pw);
		database.sync();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	}
}
