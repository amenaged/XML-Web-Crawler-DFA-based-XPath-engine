package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

public class SignUpServlet extends HttpServlet {

	private static final long serialVersionUID = -6177471475129608116L;
	private static final String SIGN_UP_STATE = "SIGN_UP_STATE";
	private static final String STATE_INVALID = "INVALID USERNAME|PASSWORD";
	private static final String STATE_DUPLICATE = "USERNAME HAS BEEN USED";
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
		ServletHelper.WriteHeader(pw, "Sign Up");
		pw.print("<h4>SIGN UP: </h4>");
		HttpSession session = req.getSession();
		if (session.getAttribute(SIGN_UP_STATE) != null) {
			if (session.getAttribute(SIGN_UP_STATE).equals(STATE_INVALID))
				pw.print("<p>Please Enter Non-Empty username and password.</p>");
			if (session.getAttribute(SIGN_UP_STATE).equals(STATE_DUPLICATE))
				pw.print("<p>Username has been registered by others.</p>");
			session.removeAttribute(SIGN_UP_STATE);
		}
		pw.print("<form action=\"\" method=\"post\">");
		pw.print("Please Enter Your Username:<br><input type=\"text\" name=\"username\">");
		pw.print("<br>");
		pw.print("Please Enter Your Password:<br><input type=\"password\" name=\"password\">");
		pw.print("<br><br>");
		pw.print("<input type=\"submit\" value=\"Sign Up\">");
		pw.print("</form>");
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
			session.setAttribute(SIGN_UP_STATE, STATE_INVALID);
			resp.sendRedirect("signup");
		} else if (database.getUser(username.trim()) != null) {
			HttpSession session = req.getSession();
			session.setAttribute(SIGN_UP_STATE, STATE_DUPLICATE);
			resp.sendRedirect("signup");
		} else {
			User user = new User(username.trim(), password);
			database.saveUser(user);
			database.sync();
			PrintWriter pw = resp.getWriter();
			ServletHelper.WriteHeader(pw, "Congratulations");
			pw.print("<h4>Congratulations! You have been successfully signed up!</h4>");
			pw.print("<a href=\"signin\"><button type=\"button\">Back to Sign In Page</button></a>");
			ServletHelper.WriteTail(pw);
		}
	}

}
