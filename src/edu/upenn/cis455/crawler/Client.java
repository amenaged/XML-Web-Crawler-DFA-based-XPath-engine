package edu.upenn.cis455.crawler;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;

import edu.upenn.cis455.crawler.info.URLInfo;

/* A simple client for HTTP/1.0.
 * 
 * Default Headers Setting includes:
 * 1. GET filepath HTTP/1.0
 * 2. User-Agent: cis455crawler
 * 3. Accept: *//*
 * 
 * This Client does not support Persistent Connection and Chunked Encoding 
 * as it is only supports for HTTP/1.0.
 * 
 * supported method: GET / HEAD
 * 
 * Example Usage: 
 * Client.getInstance(URL) --> URL could either follow Http or Https protocol
 * set everything before call client.execute()
 * 
 * If Protocal name is omitted, then Http is set by default
 * 
 * Default timeout is set to 5 sec.
 * 
 */
public abstract class Client {

	protected static final String USER_AGENT = "cis455crawler";
	protected static final String PROTOCAL_VERSION = "HTTP/1.0";
	protected static final String ACCEPT = "*/*";

	protected boolean badUrlFlag;
	protected int portNumber;
	protected String method;
	protected String hostName;
	protected String filePath;
	protected HashMap<String, String> headers;
	protected String charSet;
	protected String contentType;
	protected InputStream in;

	public static Client getInstance(String docURL) {
		// System.out.println(docURL);
		if (docURL.startsWith("https://"))
			return new HttpsClient(docURL);
		return new HttpClient(docURL);
	}

	protected void setUp(String docURL) {
		URLInfo urlInfo = new URLInfo(docURL);
		hostName = urlInfo.getHostName();
		filePath = urlInfo.getFilePath();
		portNumber = urlInfo.getPortNo();
		headers = new HashMap<String, String>();
		contentType = null;
		charSet = "ISO-8859-1";
		setMethod("GET");
		setHeader("User-Agent", USER_AGENT);
		setHeader("Accept", ACCEPT);
		setHeader("Host", hostName);
	}

	public void setMethod(String method) {
		this.method = method;
	};

	public void setHeader(String name, String value) {
		headers.put(name, value);
	};

	public InputStream getResponseBody() {
		return new BufferedInputStream(in);
	};

	public abstract boolean execute();

	public abstract void closeConnection();

	public abstract long getLastModified();

	public abstract int getStatusCode();

	public abstract String getContentType();

	public abstract long getContentLength();

	public abstract String getCharSet();

}
