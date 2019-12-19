package ceri.common.collection;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Used for building nodes.
 */
public class NodeBuilder<T> {
	private final Node.Builder<T> root;
	private final Deque<Node.Builder<?>> stack = new LinkedList<>();

	public static <T> NodeBuilder<T> of() {
		return new NodeBuilder<>();
	}

	NodeBuilder() {
		root = Node.builder(null);
		stack.add(root);
	}

	public Node<T> build() {
		return root.build();
	}

	public <U> NodeBuilder<T> startGroup(String name, U value) {
		Node.Builder<U> builder = Node.builder(value).name(name);
		stack.getLast().add(builder);
		stack.add(builder);
		return this;
	}

	public <U> NodeBuilder<T> value(String name, U value) {
		stack.getLast().add(Node.builder(value).name(name));
		return this;
	}

	public NodeBuilder<T> closeGroup() {
		stack.removeLast();
		return this;
	}

}
