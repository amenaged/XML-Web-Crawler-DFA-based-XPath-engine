package test.edu.upenn.cis455;

import junit.framework.TestCase;
import edu.upenn.cis455.crawler.Client;

public class ClientTest extends TestCase {

	private Client client1;
	private Client client2;
	private Client client3;

	protected void setUp() throws Exception {
		// test HttpClient
		client1 = Client.getInstance("http://www.w3schools.com/xml/note.xml");
		// test HttpsClient
		client2 = Client.getInstance("https://www.google.com");
		// invalid URL
		client3 = Client.getInstance("https://abcm");
		client1.execute();
		client2.execute();
		client3.execute();
	}

	public void testGetStatusCode() {
		assertEquals(client1.getStatusCode(), 200);
		assertEquals(client2.getStatusCode(), 200);
		assertEquals(client3.getStatusCode(), -1);
	}

	public void testGetContentType() {
		assertEquals(client1.getContentType(), "text/xml");
		assertEquals(client2.getContentType(), "text/html");
		assertNull(client3.getContentType());
	}

	public void testGetContentLength() {
		assertTrue(client1.getContentLength() > 0);
		assertTrue(client2.getContentLength() >= -1);
		assertTrue(client3.getContentLength() == -1);
	}

	protected void tearDown() throws Exception {
		client1.closeConnection();
		client2.closeConnection();
		client3.closeConnection();
	}

}
