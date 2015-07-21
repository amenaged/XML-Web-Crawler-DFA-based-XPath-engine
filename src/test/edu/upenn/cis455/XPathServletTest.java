package test.edu.upenn.cis455;

import junit.framework.TestCase;
import edu.upenn.cis455.crawler.Client;
import edu.upenn.cis455.servlet.XPathServlet;

public class XPathServletTest extends TestCase {

	public void testIsValidInput() {
		assertEquals(false, XPathServlet.isValidInput("", null));
		assertEquals(false, XPathServlet.isValidInput("", "  "));
		assertEquals(true, XPathServlet.isValidInput("http://www.google.com",
				"/company/staff"));
	}

	public void testIsValidResponse() {
		Client client1 = Client.getInstance("http://www.google.com");
		Client client2 = Client.getInstance("http://123");
		Client client3 = Client.getInstance("http://en.wikipedia.org/123213");
		assertEquals(true, XPathServlet.isValidResponse(client1));
		assertEquals(false, XPathServlet.isValidResponse(client2));
		assertEquals(false, XPathServlet.isValidResponse(client3));
	}

	public void testConvertToXML() {
		Client client1 = Client.getInstance("http://www.google.com");
		Client client2 = Client
				.getInstance("https://dbappserv.cis.upenn.edu/crawltest.html");
		client1.execute();
		client2.execute();
		assertNotNull(XPathServlet.convertToXML(client1));
		assertNotNull(XPathServlet.convertToXML(client2));

	}

}
