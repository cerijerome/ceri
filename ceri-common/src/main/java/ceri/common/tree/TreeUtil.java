package ceri.common.tree;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import ceri.common.util.BasicUtil;

public class TreeUtil {

	private TreeUtil() {}

	public static <T extends Parent<T>> Collection<T> leaves(T node) {
		if (node == null) throw new NullPointerException("Node cannot be null");
		Set<T> leaves = new LinkedHashSet<>();
		for (T child : iterable(node))
			if (isLeaf(child)) leaves.add(child);
		return leaves;
	}

	public static <T extends Parent<T>> boolean isLeaf(T node) {
		return node.children().isEmpty();
	}

	public static <T extends Parent<T>> T rootOf(T node) {
		if (node == null) throw new NullPointerException("Node cannot be null");
		while (node.parent() != null)
			node = node.parent();
		return node;
	}

	public static <T extends Parent<T>> boolean isChild(T child, T parent) {
		if (child == null) throw new NullPointerException("Child cannot be null");
		if (parent == null) throw new NullPointerException("Parent cannot be null");
		if (child.parent() == null) return false;
		if (child.parent().equals(parent)) return true;
		return isChild(child.parent(), parent);
	}

	public static <T extends Parent<T>> Iterable<T> iterable(T node) {
		if (node == null) throw new NullPointerException("Node cannot be null");
		return BasicUtil.forEach(new TreeIterator<>(node));
	}

	public static <T extends Parent<T>> String toString(T node) {
		if (node == null) return String.valueOf(null);
		StringBuilder b = new StringBuilder();
		addString(b, node, 0);
		return b.toString();
	}

	private static <T extends Parent<T>> void addString(StringBuilder b, T node, int indent) {
		for (int i = 0; i < indent; i++)
			b.append('\t');
		b.append(node).append('\n');
		for (T child : node.children())
			addString(b, child, indent + 1);
	}

}
