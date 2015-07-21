package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Test {

	public static void main(String[] args) throws IOException, SAXException,
			ParserConfigurationException {
		// TODO Auto-generated method stub
		// DBWrapper db = new DBWrapper("/Users/YunchenWei/Desktop/DBTest");
		// db.start();
		// Channel channel = db.getChannel("War and Peace");
		// System.out.println(channel.getMatchedFile());
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'hh:mm:ss");
		System.out.println(formatter.format(new Date()));
	}
}
