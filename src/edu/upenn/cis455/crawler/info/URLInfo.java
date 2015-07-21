package edu.upenn.cis455.crawler.info;

public class URLInfo {

	private String hostName;
	private int portNo;
	private String filePath;
	private boolean httpsFlag;

	/**
	 * Constructor called with raw URL as input - parses URL to obtain host name
	 * and file path
	 */
	public URLInfo(String docURL) {
		if (docURL == null || docURL.equals(""))
			return;
		docURL = docURL.trim();
		// Stripping off 'http://' or 'https://'
		if (docURL.startsWith("http://") && docURL.length() >= 8) {
			docURL = docURL.substring(7);
			httpsFlag = false;
		} else if (docURL.startsWith("https://") && docURL.length() >= 9) {
			docURL = docURL.substring(8);
			httpsFlag = true;
		} else
			httpsFlag = false;
		/*
		 * If starting with 'www.' , stripping that off too
		 * if(docURL.startsWith("www.")) docURL = docURL.substring(4);
		 */
		int i = 0;
		while (i < docURL.length()) {
			char c = docURL.charAt(i);
			if (c == '/')
				break;
			i++;
		}
		String address = docURL.substring(0, i);
		if (i == docURL.length())
			filePath = "/";
		else
			filePath = docURL.substring(i); // starts with '/'
		if (address.equals("/") || address.equals(""))
			return;
		if (address.indexOf(':') != -1) {
			String[] comp = address.split(":", 2);
			hostName = comp[0].trim();
			try {
				portNo = Integer.parseInt(comp[1].trim());
			} catch (NumberFormatException nfe) {
				portNo = (httpsFlag == false ? 80 : 443);
			}
		} else {
			hostName = address;
			portNo = (httpsFlag == false ? 80 : 443);
		}
	}

	public URLInfo(String hostName, String filePath) {
		this.hostName = hostName;
		this.filePath = filePath;
		this.portNo = 80;
	}

	public URLInfo(String hostName, int portNo, String filePath) {
		this.hostName = hostName;
		this.portNo = portNo;
		this.filePath = filePath;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String s) {
		hostName = s;
	}

	public int getPortNo() {
		return portNo;
	}

	public void setPortNo(int p) {
		portNo = p;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String fp) {
		filePath = fp;
	}

}
