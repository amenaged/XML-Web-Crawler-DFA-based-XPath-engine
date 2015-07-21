package edu.upenn.cis455.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogOutServlet extends HttpServlet {

	private static final long serialVersionUID = 3377544493053400699L;
	private static final String USERNAME = "USERNAME";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession();
		session.removeAttribute(USERNAME);
		resp.sendRedirect("signin");
	}
}
