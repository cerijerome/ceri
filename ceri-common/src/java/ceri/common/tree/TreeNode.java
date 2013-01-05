package ceri.common.tree;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import ceri.common.util.HashCoder;

/**
 * Abstract class for a node in a tree. Supports immutability through builder construction.
 * Subclass as follows: 
 * MyNode extends TreeNode<MyNode>
 * MyNode.Builder extends TreeNode.Builder<MyNode>
 */
public abstract class TreeNode<T extends TreeNode<T>> implements Parent<T> {
	protected final Class<T> cls;
	private final T parent;
	public final int level;
	public final int id;
	private final Collection<T> children;
	private final int hashCode;

	protected TreeNode(Class<T> cls, T parent, Builder<T> builder) {
		if (!getClass().equals(cls)) throw new IllegalArgumentException(
			"Class must match this.getClass(): " + getClass());
		this.cls = cls;
		this.parent = parent;
		this.id = builder.id;
		level = parent == null ? 0 : parent.level + 1;
		Collection<T> children = new LinkedHashSet<>();
		for (Builder<T> child : builder.children)
			children.add(child.build(typedThis()));
		this.children = Collections.unmodifiableCollection(children);
		hashCode = HashCoder.create().add(id).add(cls).add(this.children).hashCode();
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
		return hashCode;
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
		Collection<Builder<T>> children = new LinkedHashSet<>();
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
