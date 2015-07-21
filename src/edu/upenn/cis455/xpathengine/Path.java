package edu.upenn.cis455.xpathengine;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/*
 * Path represents the constructed tree of a given XPath.
 * Each path has a internal id (query id).
 * After Constructing a path, call path.normalizePath to modify some mal-constructed nodes.
 */
class Path {

	int id;
	PathNode head;

	public Path(int id) {
		this.id = id;
	}

	public Path(int id, int pId, Path parent) {
		this.id = id;
	}

	public void normalizePath() {
		Queue<PathNode> parents = new LinkedList<PathNode>();
		parents.add(head);
		while (!parents.isEmpty()) {
			PathNode parent = parents.poll();
			List<PathNode> children = parent.children;
			for (PathNode child : children) {
				child.ancestor = parent.children.size() > 1 ? parent
						: parent.ancestor;
				parents.add(child);
			}
		}
	}

	public void reset() {
		head.resetComplete();
	}

}
