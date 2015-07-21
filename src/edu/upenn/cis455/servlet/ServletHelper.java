package edu.upenn.cis455.servlet;

import java.io.PrintWriter;

public class ServletHelper {

	public static void WriteHeader(PrintWriter pw, String title) {
		pw.write("<!DOCTYPE html><html><head><title>");
		pw.write(title);
		pw.write("</title></head><body>");
	}

	public static void WriteTail(PrintWriter pw) {
		pw.print("</body></html>");
	}

}
