package ceri.common.tree;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import ceri.common.util.BasicUtil;

/**
 * Abstract class for a node in a tree. Supports immutability through builder construction. Subclass
 * as follows: MyNode extends TreeNode<MyNode> constructor should have minimum of (MyNode parent,
 * MyNode.Builder builder) MyNode.Builder extends TreeNode.Builder<MyNode> constructor should have
 * minimum of (int id) build(MyNode parent) should construct MyNode(parent, this)
 */
public abstract class TreeNode<T extends TreeNode<T>> implements Parent<T> {
	protected final Class<T> cls;
	private final T parent;
	public final int level;
	public final int id;
	private final Set<T> children;

	protected TreeNode(T parent, Builder<T> builder) {
		this.cls = BasicUtil.uncheckedCast(getClass());
		this.parent = parent;
		this.id = builder.id;
		level = parent == null ? 0 : parent.level + 1;
		Set<T> children = new LinkedHashSet<>();
		for (Builder<T> child : builder.children)
			children.add(child.build(typedThis()));
		this.children = Collections.unmodifiableSet(children);
	}

	@Override
	public Collection<T> children() {
		return children;
	}

	@Override
	public T parent() {
		return parent;
	}

	public boolean isChildOf(T parent) {
		return TreeUtil.isChild(typedThis(), parent);
	}

	public boolean isRoot() {
		return parent == null;
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public Iterable<T> tree() {
		return TreeUtil.iterable(typedThis());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, cls, children);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TreeNode)) return false;
		TreeNode<?> node = (TreeNode<?>) obj;
		if (cls != node.cls) return false;
		if (id != node.id) return false;
		if (!children.equals(node.children)) return false;
		return true;
	}

	protected T typedThis() {
		return cls.cast(this);
	}

	public static abstract class Builder<T extends TreeNode<T>> {
		final Collection<Builder<T>> children = new LinkedHashSet<>();
		final int id;

		public Builder(int id) {
			this.id = id;
		}

		public Builder<T> child(Builder<T> child) {
			children.add(child);
			return this;
		}

		public T build() {
			return build(null);
		}

		protected abstract T build(T parent);
	}

}
