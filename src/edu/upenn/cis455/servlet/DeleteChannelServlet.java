package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;

public class DeleteChannelServlet extends HttpServlet {

	private static final long serialVersionUID = 4431980190257456288L;
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
		String channelName = req.getParameter("channelname");
		PrintWriter pw = resp.getWriter();
		ServletHelper.WriteHeader(pw, "Delete a Channel");
		if (channelName == null) {
			pw.print("<p>No Channel Selected.</p>");
		} else {
			Channel channel = database.getChannel(channelName);
			if (channel == null) {
				pw.print("<p>Channel is not existed in Database.</p>");
			} else if (!channel.getCreator().equals(
					req.getSession().getAttribute(USERNAME))) {
				pw.print("<p>You don't have the right to delete this channel.</p>");
			} else {
				database.removeChannel(channelName);
				database.sync();
				pw.print("<p>This channel has been deleted from database.</p>");
			}
		}
		pw.print("<a href=\"homepage\"><button type=\"button\">Home Page</button></a>");
		ServletHelper.WriteTail(pw);
	}
}
