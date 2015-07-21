package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

public class UnSubscribeChannelServlet extends HttpServlet {

	private static final long serialVersionUID = 4179705161782614960L;
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
		String channelName = req.getParameter("channelname");
		PrintWriter pw = resp.getWriter();
		ServletHelper.WriteHeader(pw, "Unsubscribe Channel");
		if (username == null) {
			pw.print("<p>Please log in first to unsubscribe channels.</p>");
		} else if (channelName == null) {
			pw.print("<p>No Channel Selected.</p>");
		} else {
			Channel channel = database.getChannel(channelName);
			if (channel == null) {
				pw.print("<p>Channel is not existed in Database.</p>");
			} else {
				User user = database.getUser(username);
				if (user == null) {
					pw.print("<p>User not exists in the database.</p>");
				} else {
					user.unSubscribe(channelName);
					pw.print("<p>You have been successfully unsubscibe this channel.</p>");
					database.saveUser(user);
					database.sync();
				}
			}
		}
		pw.print("<a href=\"homepage\"><button type=\"button\">Home Page</button></a>");
		ServletHelper.WriteTail(pw);
	}
}
