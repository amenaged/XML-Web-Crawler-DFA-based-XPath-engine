package test.edu.upenn.cis455;

import java.util.LinkedList;

import junit.framework.TestCase;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

public class DBWrapperTest extends TestCase {

	private DBWrapper database;

	protected void setUp() throws Exception {
		super.setUp();
		database = new DBWrapper("test_database");
		database.start();
	}

	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
		database.shutdown();
	}

	public void testSaveUser() {
		User user = new User("test", "test");
		database.saveUser(user);
		assertNotNull(database.getUser("test"));
	}

	public void testGetUser() {
		User user = new User("test", "test");
		database.saveUser(user);
		String username = "test";
		assertNotNull(database.getUser(username));
	}

	public void testSaveChannel() {
		LinkedList<String> xpaths = new LinkedList<String>();
		xpaths.add("/html");
		xpaths.add("/rss/channel");
		Channel channel = new Channel("TestChannel", "test", xpaths,
				"rss/rss.xsl");
		database.saveChannel(channel);
		assertNotNull(database.getChannel("TestChannel"));
		database.removeChannel("TestChannel");
	}

	public void testGetChannel() {
		LinkedList<String> xpaths = new LinkedList<String>();
		xpaths.add("/html");
		xpaths.add("/rss/channel");
		Channel channel = new Channel("TestChannel", "test", xpaths,
				"rss/rss.xsl");
		database.saveChannel(channel);
		channel = database.getChannel("TestChannel");
		assertNotNull(database.getChannel("TestChannel"));
		assertEquals(channel.getCreator(), "test");
		assertEquals(channel.getXPaths().size(), 2);
		assertEquals(channel.getXSL(), "rss/rss.xsl");
		database.removeChannel("TestChannel");
	}

	public void testRemoveChannel() {
		LinkedList<String> xpaths = new LinkedList<String>();
		xpaths.add("/html");
		xpaths.add("/rss/channel");
		Channel channel = new Channel("TestChannel", "test", xpaths,
				"rss/rss.xsl");
		database.saveChannel(channel);
		database.removeChannel("TestChannel");
		assertNull(database.getChannel("TestChannel"));
	}

	public void testGetChannels() {
		LinkedList<String> xpaths = new LinkedList<String>();
		xpaths.add("/html");
		xpaths.add("/rss/channel");
		Channel channel1 = new Channel("TestChannel1", "test", xpaths,
				"rss/rss.xsl");
		Channel channel2 = new Channel("TestChannel2", "test", xpaths,
				"rss/rss.xsl");
		database.saveChannel(channel1);
		database.saveChannel(channel2);
		// System.out.println(database.getChannels());
		assertEquals(database.getChannels().size(), 2);
	}
}
