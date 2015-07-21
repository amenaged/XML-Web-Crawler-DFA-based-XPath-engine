package edu.upenn.cis455.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

import edu.upenn.cis455.crawler.Client;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {

	/* TODO: Implement user interface for XPath engine here */

	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		/* TODO: Implement user interface for XPath engine here */
		String xpaths = request.getParameter("xpath");
		String url = request.getParameter("url");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			if (!isValidInput(url, xpaths)) {
				pw.print("<!DOCTYPE html><html><head><title>XPath Evaluate Result</title></head>");
				pw.print("<body>");
				pw.write("<h4>Please Enter correct XPath and Document URL.</h4>");
				pw.print("</body></html>");
				return;
			}
			Client client = Client.getInstance(url);
			if (!isValidResponse(client)) {
				ServletHelper.WriteHeader(pw, "Xpath Evaluate Result");
				pw.write("<h4>The Given URL can not be retrieved. Please check your input.</h4>");
				ServletHelper.WriteTail(pw);
				if (client != null)
					client.closeConnection();
				return;
			}
			InputStream is = convertToXML(client);
			if (is == null) {
				ServletHelper.WriteHeader(pw, "Xpath Evaluate Result");
				pw.write("<h4>The Given URL is not a valid XML file. Please check your input.</h4>");
				ServletHelper.WriteTail(pw);
				client.closeConnection();
				return;
			}
			try {
				XPathEngineImpl xPathEngine = (XPathEngineImpl) XPathEngineFactory
						.getXPathEngine();
				String[] xPaths = xpaths.split(";");
				xPathEngine.setXPaths(xPaths);
				SAXParser parser = SAXParserFactory.newInstance()
						.newSAXParser();
				parser.parse(is, xPathEngine);
				ServletHelper.WriteHeader(pw, "Xpath Evaluate Result");
				pw.write("<table border=\"1\" width=\"80%\" align=\"middle\">");
				pw.write("<tr align=\"middle\"><th>XPath</th><th>Evaluate Result</th></tr>");
				boolean[] results = xPathEngine.evaluate(null);
				for (int i = 0; i < xPaths.length; i++) {
					pw.write("<tr align=\"middle\"><td>");
					pw.write(xPaths[i]);
					pw.write("</td><td>");
					if (!xPathEngine.isValid(i))
						pw.write("Invalid XPath");
					else
						pw.write(results[i] == true ? "Match" : "Not Match");
					pw.write("</td></tr>");
				}
				pw.write("</table>");
				ServletHelper.WriteTail(pw);
			} catch (SAXException | ParserConfigurationException e) {
				e.printStackTrace();
				ServletHelper.WriteHeader(pw, "Xpath Evaluate Result");
				pw.write("<h4>Errors happen when parsing. Maybe the URL is not a well-formated html/xml file</h4>");
				ServletHelper.WriteTail(pw);
			} finally {
				client.closeConnection();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		/* TODO: Implement user interface for XPath engine here */
		try {
			PrintWriter pw = response.getWriter();
			pw.print("<!DOCTYPE html><html><head><title>XPath Evaluator</title></head>");
			pw.print("<body>");
			pw.print("<h3>Full Name: Yunchen Wei</h3>");
			pw.print("<h3>SEAS Login Name: yunchenw</h3>");
			pw.print("<form action=\"\" method=\"post\">");
			pw.print("XPath(Use semi-colon for seperation if you have multiple XPaths):<br><input type=\"text\" name=\"xpath\">");
			pw.print("<br>");
			pw.print("Document URL:<br><input type=\"text\" name=\"url\">");
			pw.print("<br><br>");
			pw.print("<input type=\"submit\" value=\"Submit\">");
			pw.print("</form></body></html>");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// given URL and xpaths, determine if user input is valid or not?
	public static boolean isValidInput(String url, String xpaths) {
		if (xpaths == null || url == null || xpaths.trim().length() == 0
				|| url.trim().length() == 0)
			return false;
		return true;
	}

	// execute the client, return if the client get successful response from
	// server
	public static boolean isValidResponse(Client client) {
		if (client == null || !client.execute()
				|| client.getStatusCode() != 200)
			return false;
		return true;
	}

	/*
	 * For a successful client execution, if the response contentType is
	 * text/html, use JTidy to convert it to xml file, which can then can be
	 * evaluated by a SAXParser.
	 * 
	 * If the contentType is already in xml format, then just return the
	 * response body. If it's is in other format, then return null ==> in this
	 * case, client should be closed properly.
	 */
	public static InputStream convertToXML(Client client) {
		if (client.getContentType() == null)
			return null;
		if (client.getContentType().equals("text/html")) {
			Tidy tidy = new Tidy();
			tidy.setShowWarnings(false);
			tidy.setShowErrors(0);
			tidy.setQuiet(true);
			tidy.setInputEncoding(client.getCharSet());
			tidy.setXHTML(true);
			tidy.setMakeClean(true);
			tidy.setForceOutput(true);
			tidy.setDocType("omit");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			tidy.parseDOM(client.getResponseBody(), baos);
			ByteArrayInputStream bais = new ByteArrayInputStream(
					baos.toByteArray());
			return bais;
		}
		if (client.getContentType().matches("text/xml|application/xml|.*+xml"))
			return client.getResponseBody();
		return null;
	}

}
