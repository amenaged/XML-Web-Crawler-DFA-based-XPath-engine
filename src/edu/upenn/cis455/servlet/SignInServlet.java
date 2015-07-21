package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

public class SignInServlet extends HttpServlet {

	private static final long serialVersionUID = 6812305772807717045L;
	private static final String SIGN_IN_STATE = "SIGN_IN_STATE";
	private static final String USERNAME = "USERNAME";
	private static final String STATE_INVALID = "INVALID USERNAME|PASSWORD";
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
		if (session.getAttribute(USERNAME) != null) {
			resp.sendRedirect("homepage");
			return;
		}
		List<Channel> channels = database.getChannels();
		PrintWriter pw = resp.getWriter();
		ServletHelper.WriteHeader(pw, "Sign In");
		pw.print("<h3>Full Name: Yunchen Wei</h3>");
		pw.print("<h3>SEAS Login Name: yunchenw</h3>");
		pw.print("<h4>SIGN UP: </h4>");
		if (session.getAttribute(SIGN_IN_STATE) != null
				&& session.getAttribute(SIGN_IN_STATE).equals(STATE_INVALID)) {
			pw.print("<p>Username or Password is not correct.</p>");
			session.removeAttribute(SIGN_IN_STATE);
		}
		pw.print("<form action=\"\" method=\"post\">");
		pw.print("Username:<br><input type=\"text\" name=\"username\">");
		pw.print("<br>");
		pw.print("Password:<br><input type=\"password\" name=\"password\">");
		pw.print("<br>");
		pw.print("<input type=\"submit\" value=\"Login\">");
		pw.print("</form>");
		pw.print("<a href=\"signup\"><button type=\"button\">Sign Up</button></a>");
		pw.print("<br>");
		pw.write("<a href=\"crawler\"><button type=\"button\">Crawler Interface</button></a>");
		pw.print("<table border=\"1\" width=\"80%\" align=\"middle\"><col width=\"50%\" /><col width=\"50%\" />");
		pw.print("<tr align=\"middle\"><th>Channel Name</th><th>Creator</th></tr>");
		for (Channel channel : channels) {
			pw.print("<tr align=\"middle\"><td><a href=\"display?channelname="
					+ channel.getName() + "\">" + channel.getName()
					+ "</a></td><td>" + channel.getCreator() + "</td></tr>");
		}
		pw.print("</table>");
		ServletHelper.WriteTail(pw);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		if (username == null || password == null
				|| username.trim().length() == 0 || password.length() == 0) {
			HttpSession session = req.getSession();
			session.setAttribute(SIGN_IN_STATE, STATE_INVALID);
			resp.sendRedirect("signin");
			return;
		}
		User user = database.getUser(username.trim());
		if (user == null || !user.getPassword().equals(password)) {
			HttpSession session = req.getSession();
			session.setAttribute(SIGN_IN_STATE, STATE_INVALID);
			resp.sendRedirect("signin");
			return;
		}
		HttpSession session = req.getSession();
		session.setAttribute(USERNAME, username.trim());
		resp.sendRedirect("homepage");
	}

}
