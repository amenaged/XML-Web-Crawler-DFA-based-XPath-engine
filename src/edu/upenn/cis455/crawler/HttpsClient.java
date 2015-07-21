package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

/*
 * HttpsClient 
 * subclass of Client
 */
class HttpsClient extends Client {

	private URL url;
	private HttpsURLConnection conn;

	private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(
			"(.*?)\\s*(;\\s*charset\\s*=\\s*(.*))?", Pattern.CASE_INSENSITIVE);

	public HttpsClient(String docURL) {
		setUp(docURL);
		try {
			url = new URL("https", hostName, portNumber, filePath);
		} catch (MalformedURLException e) {
			// e.printStackTrace();
			badUrlFlag = true;
		}
	}

	public boolean execute() {
		if (badUrlFlag)
			return false;
		try {
			conn = (HttpsURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod(method);
			for (Entry<String, String> header : headers.entrySet())
				conn.setRequestProperty(header.getKey(), header.getValue());
			conn.connect();
			if (conn.getResponseCode() >= 400)
				in = conn.getErrorStream();
			else
				in = conn.getInputStream();
			String tmp = conn.getContentType();
			if (tmp != null) {
				Matcher m = CONTENT_TYPE_PATTERN.matcher(tmp);
				if (m.matches()) {
					contentType = m.group(1);
					if (m.group(3) != null)
						charSet = m.group(3);
				}
			}
			return true;
		} catch (IOException e) {
			// e.printStackTrace();
			return false;
		}
	}

	// null if no header
	public long getLastModified() {
		return conn.getLastModified();
	}

	// -1 if no response
	public int getStatusCode() {
		try {
			int sc = conn.getResponseCode();
			return sc;
		} catch (IOException e) {
			return -1;
		}
	}

	// null if no header
	public String getContentType() {
		return contentType;
	}

	// -1 if no header
	public long getContentLength() {
		return conn.getContentLengthLong();
	}

	public void closeConnection() {
		try {
			if (conn != null)
				conn.disconnect();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public String getCharSet() {
		return charSet;
	}
}
