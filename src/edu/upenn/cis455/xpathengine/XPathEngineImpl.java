package edu.upenn.cis455.xpathengine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * XPathEngineImpl extends DefaultHandler
 * Sample Usage:
 * XPathEngineImpl xPathEngine =  (XPathEngineImpl) XPathEngineFactory
 *  						.getXPathEngine(); // need to be casted
 * xPathEngine.setXPaths(xpaths);
 * SAXParser parser = SAXParserFactory.newInstance();.newSAXParser();
 * parser.parse(inputStream, xPathEngine);
 * boolean results = xPathEngine.evaluate(null);
 * 
 */
public class XPathEngineImpl extends DefaultHandler implements XPathEngine {

	/*
	 * reset curPos, result array and path map so each each XPathEngine can be
	 * used multiple time instead of just once
	 */
	@Override
	public void startDocument() throws SAXException {
		curPos = 0;
		for (int i = 0; i < evalResults.length; i++)
			evalResults[i] = false;
		for (Path path : paths.values())
			path.reset();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		procNodeLst.clear();
		curPos++;
		List<PathNode> tmpCL = CL.get(qName);
		if (tmpCL == null)
			return;
		for (PathNode node : tmpCL) {
			if (node.pos == curPos && node.checkAtts(attributes)) {
				if (node.hasTxtPred()) {
					procNodeLst.add(node);
					continue;
				}
				if (node.children.size() == 0) {
					node.complete();
					if (node.isCompleted())
						evalResults[node.id] = true;
				} else
					for (PathNode child : node.children)
						copyFromWLtoCL(child);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		procNodeLst.clear();
		List<PathNode> tmpCL = CL.get(qName);
		if (tmpCL != null)
			for (PathNode node : tmpCL) {
				if (node.pos != curPos)
					continue;
				node.resetComplete();
				for (PathNode child : node.children)
					removeFromCL(child);
			}
		curPos--;
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String text = new String(ch, start, length);
		for (PathNode node : procNodeLst) {
			if (node.checkTxt(text))
				if (node.children.size() == 0) {
					node.complete();
					if (node.isCompleted())
						evalResults[node.id] = true;
				} else {
					for (PathNode child : node.children)
						copyFromWLtoCL(child);
				}
		}
	}

	// a helper function to retrieve proper test part of a xpath.
	// Ex. [xxx][xxx][asd[asd]] ==> [xxx]
	private static String retrieveTestPart(String test) {
		for (int i = 0, leftBracCount = 0; i < test.length(); i++) {
			char c = test.charAt(i);
			if (c == '[')
				leftBracCount++;
			else if (c == ']')
				leftBracCount--;
			if (leftBracCount == 0)
				return test.substring(0, i + 1);
		}
		return null;
	}

	private static boolean isValidTest(String test, Path path, PathNode current) {
		String testPart = retrieveTestPart(test);
		if (testPart == null)
			return false;
		else if (testPart.length() == test.length()) {
			test = test.substring(1, test.length() - 1).trim();
			if (isValidXPath("/" + test, path, current, current))
				return true;
			Matcher matcher = TEST_PATTERN.matcher(test);
			if (!matcher.matches())
				return false;
			if (matcher.group(1) != null)
				current.addTxtPred(matcher.group(1));
			else if (matcher.group(2) != null)
				current.addCntsTxtPred(matcher.group(2));
			else if (matcher.group(3) != null)
				current.addAttNamePred(matcher.group(3), matcher.group(5));
			return true;
		} else
			return isValidTest(testPart, path, current)
					&& isValidTest(test.substring(testPart.length()), path,
							current);
	}

	private static boolean isValidXPath(String XPath, Path path,
			PathNode ancestor, PathNode current) {
		if (XPath == null || !XPath.startsWith("/"))
			return false;
		Matcher matcher = STEP_PATTERN.matcher(XPath.substring(1).trim());
		if (!matcher.matches())
			return false;
		if (ancestor == null) {
			ancestor = new PathNode(matcher.group(1), path.id);
			current = ancestor;
			path.head = ancestor;
		} else {
			PathNode newCurrent = new PathNode(matcher.group(1), path.id,
					current.pos + 1, ancestor);
			current.addChild(newCurrent);
			current = newCurrent;
		}
		if (matcher.group(2) != null && matcher.group(2).length() > 0)
			if (!isValidTest(matcher.group(2), path, current))
				return false;
		if (matcher.group(4) != null)
			return isValidXPath(matcher.group(4), path, ancestor, current);
		return true;
	}

	// copy path node from wating list to candidate list
	private void copyFromWLtoCL(PathNode node) {
		String name = node.elem;
		List<PathNode> lst = CL.get(name);
		if (lst == null) {
			lst = new LinkedList<PathNode>();
			CL.put(name, lst);
		}
		lst.add(node);
	}

	// delete path node from candidate list
	private void removeFromCL(PathNode node) {
		if (node.pos == 1)
			return;
		String name = node.elem;
		List<PathNode> lst = CL.get(name);
		if (lst != null)
			lst.remove(node);
	}

	private static final Pattern STEP_PATTERN = Pattern.compile(
			"(?!xml)([a-z_][\\w-_,]*)\\s*((\\[.*?\\])*)(/.*)?",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern TEST_PATTERN = Pattern
			.compile("text\\s*\\(\\s*\\)\\s*=\\s*\"(.*)\"|contains\\s*\\(\\s*text\\s*\\(\\s*\\)\\s*,\\s*\"(.*)\"\\)|@\\s*([a-zA-Z_:]([\\w_-]|[,:])*)\\s*=\\s*\"(.*)\"");

	private int curPos;
	private List<PathNode> procNodeLst;

	private boolean[] isValid;
	private boolean[] evalResults;
	private Map<Integer, Path> paths;
	private Map<String, List<PathNode>> CL;
	private Map<String, List<PathNode>> WL;

	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor!!
		paths = new HashMap<Integer, Path>();
		procNodeLst = new LinkedList<PathNode>();
		CL = new HashMap<String, List<PathNode>>();
		WL = new HashMap<String, List<PathNode>>();
	}

	public void setXPaths(String[] s) {
		/* TODO: Store the XPath expressions that are given to this method */
		CL.clear();
		WL.clear();
		paths.clear();
		isValid = new boolean[s.length];
		evalResults = new boolean[s.length];
		for (int i = 0; i < s.length; i++) {
			Path path = new Path(i);
			if (isValidXPath(s[i], path, null, null)) {
				isValid[i] = true;
				paths.put(i, path);
				path.normalizePath();
			}
		}
		for (Path path : paths.values()) {
			PathNode head = path.head;
			if (CL.get(head.elem) == null)
				CL.put(head.elem, new LinkedList<PathNode>());
			CL.get(head.elem).add(head);
			Queue<PathNode> queue = new LinkedList<PathNode>();
			queue.addAll(head.children);
			while (!queue.isEmpty()) {
				PathNode node = queue.poll();
				if (WL.get(node.elem) == null)
					WL.put(node.elem, new LinkedList<PathNode>());
				WL.get(node.elem).add(node);
				queue.addAll(node.children);
			}
		}
	}

	public boolean isValid(int i) {
		/* TODO: Check which of the XPath expressions are valid */
		return isValid[i];
	}

	public boolean[] evaluate(Document d) {
		/* TODO: Check whether the document matches the XPath expressions */
		return evalResults;
	}

}
