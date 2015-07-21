package edu.upenn.cis455.storage;

import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class User {

	@PrimaryKey
	private String username;
	private String password;
	private Map<String, Long> subscribes;

	public User() {
	}

	public User(String username, String password) {
		this.username = username;
		this.password = password;
		subscribes = new HashMap<String, Long>();
	}

	public void setUserName(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Map<String, Long> getSubscribes() {
		return subscribes;
	}

	public void subscribe(String channel) {
		if (!subscribes.containsKey(channel))
			subscribes.put(channel, -1L);
	}

	public void unSubscribe(String channel) {
		subscribes.remove(channel);
	}

	public void viewChannel(String channel) {
		subscribes.put(channel, System.currentTimeMillis());
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Username: " + username + ", Password:" + password + "\n");
		sb.append("Subscribed subscribes: \n");
		for (String channel : subscribes.keySet()) {
			sb.append(channel + "\n");
		}
		return sb.toString();
	}
}
