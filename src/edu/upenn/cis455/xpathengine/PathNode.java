package edu.upenn.cis455.xpathengine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xml.sax.Attributes;

/*
 * Path Node is the basic unit of a path.
 * Each PathNode has a list of children, an internal path id and some predication.
 * 
 */
class PathNode {

	String elem;
	int id;
	int pos;
	// private int relativePos;
	// private int level;
	// int compCount;

	PathNode ancestor;
	List<PathNode> children;
	Set<PathNode> completeNodes;

	List<String> txtPred;
	List<String> cntsTxtPred;
	Map<String, String> attNamePred;

	public PathNode(String elem, int id) {
		this.elem = elem;
		this.id = id;
		this.pos = 1;
		this.ancestor = this;
		children = new LinkedList<PathNode>();
		completeNodes = new HashSet<PathNode>();
	}

	public PathNode(String elem, int id, int pos, PathNode ancestor) {
		this.elem = elem;
		this.id = id;
		this.pos = pos;
		this.ancestor = ancestor;
		children = new LinkedList<PathNode>();
		completeNodes = new HashSet<PathNode>();
	}

	public boolean isCompleted() {
		PathNode iter = this;
		while (iter != iter.ancestor)
			iter = iter.ancestor;
		return iter.completeNodes.size() >= iter.children.size();
	}

	public void complete() {
		ancestor.completeNodes.add(this);
		if (ancestor.children.size() == ancestor.completeNodes.size())
			ancestor.complete();
	}

	public void resetComplete() {
		completeNodes.clear();
		for (PathNode child : children)
			child.resetComplete();
	}

	public void addChild(PathNode node) {
		children.add(node);
	}

	public void addTxtPred(String text) {
		if (txtPred == null)
			txtPred = new LinkedList<String>();
		txtPred.add(text);
	}

	public void addCntsTxtPred(String text) {
		if (cntsTxtPred == null)
			cntsTxtPred = new LinkedList<String>();
		cntsTxtPred.add(text);
	}

	public void addAttNamePred(String attName, String attValue) {
		if (attNamePred == null)
			attNamePred = new HashMap<String, String>();
		attNamePred.put(attName, attValue);
	}

	public boolean checkTxt(String text) {
		if (txtPred != null)
			for (String txt : txtPred)
				if (!txt.equals(text))
					return false;
		if (cntsTxtPred != null)
			for (String txt : cntsTxtPred)
				if (!text.contains(txt))
					return false;
		return true;
	}

	public boolean hasTxtPred() {
		return txtPred != null || cntsTxtPred != null;
	}

	public boolean checkAtts(Attributes atts) {
		if (attNamePred == null)
			return true;
		for (Entry<String, String> att : attNamePred.entrySet())
			if (!att.getValue().equals(atts.getValue(att.getKey())))
				return false;
		return true;
	}

	public String toString() {
		return "Q" + id + "-" + pos + " " + elem + ", Ancestor: " + "Q"
				+ ancestor.id + "-" + ancestor.pos + " " + ancestor.elem;
	}

}
