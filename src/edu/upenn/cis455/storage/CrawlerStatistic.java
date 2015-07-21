package edu.upenn.cis455.storage;

import java.util.HashSet;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/*
 *  
 */
@Entity
public class CrawlerStatistic {
	@PrimaryKey
	private String name;
	private int htmlNumber;
	private int xmlNumber;
	private long dataAmount; // count in byte
	private Set<String> servers;

	// private int serverNumber;

	public CrawlerStatistic() {
		name = "CIS455Crawler";
		htmlNumber = 0;
		xmlNumber = 0;
		dataAmount = 0;
		servers = new HashSet<String>();
	}

	public int getHtmlNumber() {
		return htmlNumber;
	}

	public void addHtmlNumber() {
		htmlNumber++;
	}

	public int getXmlNumber() {
		return xmlNumber;
	}

	public void addXmlNumber() {
		xmlNumber++;
	}

	// return in Megabyte
	public double getRetrieveDataAmount() {
		return ((double) dataAmount) / (double) 1_000_000;
	}

	public void addDataAmount(long byteNumber) {
		dataAmount += byteNumber;
	}

	public void addServer(String hostName) {
		servers.add(hostName);
	}

	public int getServerCount() {
		return servers.size();
	}
}
