package edu.upenn.cis455.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

/*
 * Database Wrapper class;
 * Usage:
 * 	DBWrapper db = new DBWrapper("directory");
 * 	db.start();
 *  ......
 *  db.sync();
 *  db.shutdown();
 */
public class DBWrapper {

	private static final String STORE_NAME = "CIS455HW2_STORE";
	private static String envDirectory = null;

	private static Environment myEnv;
	private static EntityStore store;
	private PrimaryIndex<String, User> userPriIndex;
	private PrimaryIndex<String, Channel> channelPriIndex;
	private PrimaryIndex<String, CrawledFile> filePriIndex;
	private PrimaryIndex<String, CrawlerStatistic> crawlerPriIndex;

	public DBWrapper(String envDirectory) {
		DBWrapper.envDirectory = envDirectory;
		File directory = new File(envDirectory);
		if (!directory.exists()) {
			directory.mkdir();
		}
		
	}

	/*
	 * return true if successfully start; return false if error happens
	 */
	public boolean start() {
		try {
			EnvironmentConfig envConfig = new EnvironmentConfig();
			// envConfig.setLocking(false);
			StoreConfig storeConfig = new StoreConfig();
			envConfig.setAllowCreate(true);
			storeConfig.setAllowCreate(true);
			myEnv = new Environment(new File(envDirectory), envConfig);
			store = new EntityStore(myEnv, STORE_NAME, storeConfig);
			userPriIndex = store.getPrimaryIndex(String.class, User.class);
			channelPriIndex = store
					.getPrimaryIndex(String.class, Channel.class);
			filePriIndex = store.getPrimaryIndex(String.class,
					CrawledFile.class);
			crawlerPriIndex = store.getPrimaryIndex(String.class,
					CrawlerStatistic.class);
			return true;
		} catch (DatabaseException dbe) {
			dbe.printStackTrace();
			return false;
		}
	}

	public void shutdown() {
		if (store != null)
			store.close();
		if (myEnv != null)
			myEnv.close();
	}

	public void sync() {
		if (store != null)
			store.sync();
		if (myEnv != null)
			myEnv.sync();
	}

	public void saveUser(User user) {
		userPriIndex.put(user);
	}

	public User getUser(String username) {
		return userPriIndex.get(username);
	}

	public void saveChannel(Channel channel) {
		channelPriIndex.put(channel);
	}

	public Channel getChannel(String channelName) {
		return channelPriIndex.get(channelName);
	}

	public void removeChannel(String channelName) {
		channelPriIndex.delete(channelName);
	}

	public List<Channel> getChannels() {
		List<Channel> channels = new ArrayList<Channel>();
		EntityCursor<Channel> channelCursor = channelPriIndex.entities();
		Iterator<Channel> iterator = channelCursor.iterator();
		while (iterator.hasNext())
			channels.add(iterator.next());
		channelCursor.close();
		return channels;
	}

	public void saveFile(CrawledFile file) {
		filePriIndex.put(file);
	}

	public CrawledFile getFile(String URL) {
		return filePriIndex.get(URL);
	}

	public CrawlerStatistic getCrawlerInfo() {
		return crawlerPriIndex.get("CIS455Crawler");
	}

	public void updateCrawlerInfo(CrawlerStatistic crawlerInfo) {
		crawlerPriIndex.put(crawlerInfo);
	}

	/* TODO: write object store wrapper for BerkeleyDB */

	public static void main(String... args) {
		DBWrapper db = new DBWrapper("/Users/YunchenWei/Desktop/DBTest");
		db.start();
		db.shutdown();
	}
}
