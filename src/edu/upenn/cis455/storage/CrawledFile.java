package edu.upenn.cis455.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/*
 *  
 */
@Entity
public class CrawledFile {

	@PrimaryKey
	private String URL;
	private String charSet;
	private long lastCrawled;
	private byte[] content;

	public CrawledFile() {
	}

	public CrawledFile(String URL, String charSet, long lastCrawled,
			InputStream is) {
		this.URL = URL;
		this.charSet = charSet;
		this.lastCrawled = lastCrawled;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int next;
		try {
			while ((next = is.read()) != -1) {
				bos.write(next);
			}
			bos.flush();
			content = bos.toByteArray();
			bos.close();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	public String getURL() {
		return URL;
	}

	public boolean isModified(long time) {
		return time > lastCrawled;
	}

	public long getLastCrawled() {
		return lastCrawled;
	}

	public void setLastCrawled(long time) {
		this.lastCrawled = time;
	}

	public String getCharSet() {
		return charSet;
	}
	
	public long getFileSize() {
		return content.length;
	}

	public InputStream getContent() {
		return new ByteArrayInputStream(content);
	}

}
