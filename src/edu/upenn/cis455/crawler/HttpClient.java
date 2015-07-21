package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * HttpClient 
 * subclass of Client
 */
class HttpClient extends Client {

	// for request
	private Socket socket;

	// for response
	private int statusCode;
	private long contentLength;
	private long lastModified;

	private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(
			"Content-Type\\s*:\\s*(.*?)\\s*(;\\s*charset\\s*=\\s*(.*))?",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern CONTENT_LENGTH_PATTERN = Pattern.compile(
			"Content-Length\\s*:\\s*(\\d*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern LAST_MODIFIED_PATTERN = Pattern.compile(
			"Last-Modified\\s*:\\s*(.*)", Pattern.CASE_INSENSITIVE);
	private static final SimpleDateFormat dateFormator = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss zzz");

	public HttpClient(String docURL) {
		setUp(docURL);
		if (hostName == null)
			badUrlFlag = true;
		statusCode = -1;
		contentLength = -1;
		lastModified = -1;
	}

	private static String readLine(InputStream in) throws IOException {
		StringBuilder line = new StringBuilder();
		while (true) {
			int ch = in.read();
			if (ch == -1 || ch == '\n')
				return line.toString().trim();
			line.append((char) ch);
		}
	}

	/*
	 * send the request and parse the response header if there is no
	 * Content-Length header in response, then it is not static file (ignore
	 * it?)
	 * 
	 * return true if success, return false if error happens
	 */
	public boolean execute() {
		if (badUrlFlag)
			return false;
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(hostName, portNumber), 5000);
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			in = socket.getInputStream();
			out.print(method + " " + filePath + " " + PROTOCAL_VERSION + "\r\n");
			for (Entry<String, String> header : headers.entrySet()) {
				out.print(header.getKey() + ": " + header.getValue() + "\r\n");
			}
			out.print("\r\n");
			out.flush();
			String line = readLine(in); // initial Line;
			if (line == null || line.trim().length() == 0)
				return false; // no response at all;
			statusCode = Integer.parseInt(line.split("\\s+")[1]);
			while ((line = readLine(in)).length() != 0) {
				Matcher m = CONTENT_TYPE_PATTERN.matcher(line);
				if (m.matches()) {
					contentType = m.group(1);
					if (m.group(3) != null)
						charSet = m.group(3);
					continue;
				}
				m = CONTENT_LENGTH_PATTERN.matcher(line);
				if (m.matches()) {
					contentLength = Long.parseLong(m.group(1));
					continue;
				}
				m = LAST_MODIFIED_PATTERN.matcher(line);
				if (m.matches()) {
					try {
						lastModified = (dateFormator.parse(m.group(1)))
								.getTime();
					} catch (ParseException e) {
						// e.printStackTrace();
					}
					continue;
				}
			}
			return true;
		} catch (IOException e) {
			// e.printStackTrace();
			return false;
		}
	}

	// -1 if no header
	public long getLastModified() {
		return lastModified;
	}

	// -1 if no response
	public int getStatusCode() {
		return statusCode;
	}

	// null if no header
	public String getContentType() {
		return contentType;
	}

	// -1 if no header
	public long getContentLength() {
		return contentLength;
	}

	public void closeConnection() {
		try {
			if (socket != null)
				socket.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public String getCharSet() {
		return charSet;
	}

}
