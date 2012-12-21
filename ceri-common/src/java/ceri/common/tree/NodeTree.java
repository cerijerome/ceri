package ceri.common.tree;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NodeTree<T extends TreeNode<T>> {
	public final T root;
	public final Map<Integer, T> lookup;

	private NodeTree(T root) {
		this.root = root;
		Map<Integer, T> lookup = new HashMap<>();
		process(lookup, root);
		this.lookup = Collections.unmodifiableMap(lookup);
	}

	public T get(int id) {
		return lookup.get(id);
	}
	
	public static <T extends TreeNode<T>> NodeTree<T> create(T root) {
		return new NodeTree<>(root);
	}
	
	private void process(Map<Integer, T> lookup, T node) {
		Integer id = node.id;
		if (lookup.containsKey(id)) throw new IllegalStateException(
			"Failed to add " + node + "; id " + id + " already exists in tree");
		lookup.put(id, node);
		for (T child : node.children())
			process(lookup, child);
	}

}
