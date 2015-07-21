package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/*
 * Given a robotURL, retrieve the corresponding robots.txt
 * If the robots.txt does not exist for this hostName, isEmpty returns true.
 * Otherwise, return false;
 * 
 */
public class RobotRule {

	private boolean emptyFlag;
	private int delay;

	private Set<String> disallows;
	private Set<String> allows;

	public RobotRule(String robotURL) {
		Client client = Client.getInstance(robotURL);
		if (client == null || !client.execute()
				|| client.getStatusCode() != 200) {
			emptyFlag = true;
			if (client != null) {
				client.closeConnection();
			}
			return;
		}
		try {
			disallows = new HashSet<String>();
			allows = new HashSet<String>();
			BufferedReader bf = new BufferedReader(new InputStreamReader(
					client.getResponseBody(), client.getCharSet()));
			String line;
			boolean selfFlag = false;
			boolean flag = false;
			while ((line = bf.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0) {
					flag = false;
					if (selfFlag)
						break;
					continue;
				}
				if (line.startsWith("#"))
					continue;
				String[] tokens = line.split("\\s*:\\s*");
				switch (tokens[0].toLowerCase()) {
				case "user-agent":
					if (tokens.length > 1) {
						if (tokens[1].trim().equals("cis455crawler")) {
							selfFlag = true;
							flag = true;
							disallows.clear();
							allows.clear();
						} else if (tokens[1].trim().equals("*"))
							if (!selfFlag)
								flag = true;
					}
					break;
				case "disallow":
					if (flag && tokens.length > 1)
						disallows.add(tokens[1].trim());
					break;
				case "allow":
					if (flag && tokens.length > 1)
						allows.add(tokens[1].trim());
					break;
				case "crawl-delay":
					if (flag && tokens.length > 1)
						delay = 1000 * (int) Double.parseDouble(tokens[1]
								.trim());
					break;
				}
			}
			bf.close();
		} catch (IOException ee) {
			// e.printStackTrace();
			emptyFlag = true;
			return;
		} finally {
			client.closeConnection();
		}

	}

	// returns true if no robots.txt exists for this hostname
	public boolean isEmpty() {
		return emptyFlag;
	}

	// unit: millisecond (default 0)
	public int getDelay() {
		return delay;
	}

	// return set of allowed URLs under this hostname
	public Set<String> getAllows() {
		return allows;
	}

	// return set of disallowed URLs under this hostname
	public Set<String> getDisallows() {
		return disallows;
	}

}
