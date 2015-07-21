package edu.upenn.cis455.storage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Channel {

	@PrimaryKey
	private String name; // name is unique in this case, need to check name
							// conflict before user create a channel
	private String creator;
	private List<String> xPaths;
	private Map<String, Long> matchedFiles;
	private String XSL;
	private long lastModifiedTime;

	public Channel() {
	}

	public Channel(String name, String creator) {
		this.name = name;
		this.creator = creator;
		this.XSL = null;
		this.lastModifiedTime = -1L;
		this.xPaths = new LinkedList<String>();
		this.matchedFiles = new HashMap<String, Long>();
	}

	public Channel(String name, String creator, String XSL) {
		this.name = name;
		this.creator = creator;
		this.XSL = XSL;
		this.lastModifiedTime = -1L;
		this.xPaths = new LinkedList<String>();
		this.matchedFiles = new HashMap<String, Long>();
	}

	public Channel(String name, String creator, List<String> xPaths, String XSL) {
		this.name = name;
		this.creator = creator;
		this.xPaths = xPaths;
		this.XSL = XSL;
		this.lastModifiedTime = -1L;
		this.matchedFiles = new HashMap<String, Long>();
	}

	public void setXPaths(List<String> xPaths) {
		this.xPaths = xPaths;
	}

	public void setXSL(String XSL) {
		this.XSL = XSL;
	}

	public void addXPath(String XPath) {
		xPaths.add(XPath);
	}

	public String getName() {
		return name;
	}

	public String getCreator() {
		return creator;
	}

	public String getXSL() {
		return XSL;
	}

	public List<String> getXPaths() {
		return xPaths;
	}

	public Map<String, Long> getMatchedFile() {
		return matchedFiles;
	}

	public synchronized void addMatchedFile(String URL, long lastModified) {
		if (!matchedFiles.containsKey(URL)) {
			matchedFiles.put(URL, System.currentTimeMillis());
			this.lastModifiedTime = System.currentTimeMillis();
		} else {
			long time = matchedFiles.get(URL);
			if (lastModified == -1 || time < lastModified) {
				matchedFiles.put(URL, System.currentTimeMillis());
				this.lastModifiedTime = System.currentTimeMillis();
			}
		}
	}

	public long getLastModified() {
		return lastModifiedTime;
	}

	// TODO:
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Channel Name: " + name + "\n");
		for (Entry<String, Long> channelName : matchedFiles.entrySet()) {
			sb.append("File: " + channelName.getKey() + " Last Modified Time: "
					+ channelName.getValue() + "\n");
		}
		return sb.toString();
	}
}
