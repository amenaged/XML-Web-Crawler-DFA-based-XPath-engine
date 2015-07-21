package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;

public class CreateChannelServlet extends HttpServlet {

	private static final long serialVersionUID = 3710047163962865027L;
	private static final String CREATE_CHANNEL_STATE = "CREATE_CHANNEL_STATE";
	private static final String USERNAME = "USERNAME";
	private static final String STATE_INVALID = "INVALID_CHANNEL";
	private static final String STATE_DUPLICATE = "DUPLICATE_CHANNEL";
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
		PrintWriter pw = resp.getWriter();
		ServletHelper.WriteHeader(pw, "Create a Channel");
		pw.print("<h4>Create a new Channel</h4>");
		HttpSession session = req.getSession();
		if (session.getAttribute(CREATE_CHANNEL_STATE) != null) {
			if (session.getAttribute(CREATE_CHANNEL_STATE)
					.equals(STATE_INVALID))
				pw.print("<p>Channel Name / Xpaths / XSL Stylesheet not Invalid.</p>");
			else if (session.getAttribute(CREATE_CHANNEL_STATE).equals(
					STATE_DUPLICATE))
				pw.print("<p>Channel Name Already Exists.</p>");
			session.removeAttribute(CREATE_CHANNEL_STATE);
		}
		pw.print("<form action=\"\" method=\"post\">");
		pw.print("Name:<br><input type=\"text\" name=\"name\">");
		pw.print("<br>");
		pw.print("XPaths(Use semicolon ; for seperation):<br><input type=\"text\" name=\"xpaths\">");
		pw.print("<br>");
		pw.print("XSL stylesheet URL:<br><input type=\"text\" name=\"xsl\">");
		pw.print("<br>");
		pw.print("<input type=\"submit\" value=\"Create\">");
		pw.print("</form>");
		ServletHelper.WriteTail(pw);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String name = req.getParameter("name");
		String xpaths = req.getParameter("xpaths");
		String xsl = req.getParameter("xsl");
		if (name == null || xpaths == null || xsl == null
				|| name.trim().length() == 0 || xpaths.trim().length() == 0
				|| xsl.trim().length() == 0) {
			HttpSession session = req.getSession();
			session.setAttribute(CREATE_CHANNEL_STATE, STATE_INVALID);
			resp.sendRedirect("create");
			return;
		}
		name = name.trim();
		xpaths = xpaths.trim();
		xsl = xsl.trim();
		Channel channel = database.getChannel(name);
		if (channel != null) {
			HttpSession session = req.getSession();
			session.setAttribute(CREATE_CHANNEL_STATE, STATE_DUPLICATE);
			resp.sendRedirect("create");
			return;
		}
		String creator = (String) req.getSession().getAttribute(USERNAME);
		String[] xPaths = xpaths.split("\\s*;\\s*");
		List<String> lst = new LinkedList<String>();
		for (String xpath : xPaths)
			lst.add(xpath);
		channel = new Channel(name, creator, lst, xsl);
		database.saveChannel(channel);
		database.sync();
		resp.sendRedirect("homepage");
	}
}
