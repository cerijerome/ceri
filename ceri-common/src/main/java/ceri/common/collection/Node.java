package ceri.common.collection;

import static ceri.common.collection.ImmutableUtil.collectAsList;
import static ceri.common.collection.ImmutableUtil.convertAsMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.property.PathFactory;
import ceri.common.text.ToStringHelper;
import ceri.common.util.BasicUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.PrimitiveUtil;

/**
 * Encapsulates a tree node. Can have a name, and child nodes. Nodes are immutable if value type is
 * immutable.
 */
public class Node<T> {
	public static final Node<?> NULL = builder(null).build();
	public final String name;
	public final T value;
	public final List<Node<?>> children;
	private final Map<String, Node<?>> lookup;

	/**
	 * Returns an empty node.
	 */
	public static <T> Node<T> of() {
		return BasicUtil.uncheckedCast(NULL);
	}

	/**
	 * Creates an unnamed value node with no children.
	 */
	public static <T> Node<T> of(T value) {
		return builder(value).build();
	}

	/**
	 * Creates a named value node with no children.
	 */
	public static <T> Node<T> of(String name, T value) {
		return builder(value).name(name).build();
	}

	public static class Builder<T> {
		final T value;
		String name = null;
		Collection<Node.Builder<?>> children = List.of();

		Builder(T value) {
			this.value = value;
		}

		/**
		 * Sets a name for the node/value.
		 */
		public Builder<T> name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Adds a child.
		 */
		public Builder<T> add(Node.Builder<?> child) {
			if (children.isEmpty()) children = new LinkedHashSet<>();
			children.add(child);
			return this;
		}

		/**
		 * Builds the node, and all its children.
		 */
		public Node<T> build() {
			return new Node<>(this);
		}
	}

	public static <T> Builder<T> builder(T value) {
		return new Builder<>(value);
	}

	Node(Builder<T> builder) {
		name = builder.name;
		value = builder.value;
		children = collectAsList(builder.children.stream().map(Builder::build));
		lookup = convertAsMap(node -> node.name, //
			children.stream().filter(Node::isNamed), TreeMap::new);
	}

	/**
	 * Finds a child node from a dotted path. A number in the path indicates child list index,
	 * otherwise a child name. Returns an empty node if not found.
	 */
	public Node<?> find(String path) {
		return child(PathFactory.dot.split(path), 0);
	}

	/**
	 * Lists all named child paths recursively.
	 */
	public List<String> namedPaths() {
		return namedChildPathStream().collect(Collectors.toList());
	}

	public Set<String> childNames() {
		return lookup.keySet();
	}

	public Map<String, Node<?>> namedChildren() {
		return lookup;
	}

	public boolean hasChild(String name) {
		return lookup.containsKey(name);
	}

	public boolean hasChild(int index) {
		return index >= 0 && index < children.size();
	}

	public Node<?> child(String...names) {
		return child(Arrays.asList(names), 0);
	}

	public Node<?> child(int... indexes) {
		return child(indexes, 0);
	}

	public boolean isNull() {
		return !isNamed() && !hasValue() && !isParent();
	}

	public boolean isNamed() {
		return name != null;
	}

	public boolean hasValue() {
		return value != null;
	}

	public boolean isParent() {
		return !children.isEmpty();
	}

	public Boolean asBoolean() {
		return PrimitiveUtil.booleanValue(asString());
	}

	public boolean asBoolean(boolean def) {
		return PrimitiveUtil.valueOf(asString(), def);
	}

	public Integer asInt() {
		return PrimitiveUtil.intValue(asString());
	}

	public int asInt(int def) {
		return PrimitiveUtil.valueOf(asString(), def);
	}

	public Long asLong() {
		return PrimitiveUtil.longValue(asString());
	}

	public long asLong(long def) {
		return PrimitiveUtil.valueOf(asString(), def);
	}

	public Double asDouble() {
		return PrimitiveUtil.doubleValue(asString());
	}

	public double asDouble(double def) {
		return PrimitiveUtil.valueOf(asString(), def);
	}

	public String asString() {
		return value == null ? null : String.valueOf(value);
	}

	public <U> U asType(Class<U> cls) {
		return BasicUtil.castOrNull(cls, value);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(name, value, children);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Node)) return false;
		Node<?> other = (Node<?>) obj;
		if (!EqualsUtil.equals(name, other.name)) return false;
		if (!EqualsUtil.equals(value, other.value)) return false;
		if (!EqualsUtil.equals(children, other.children)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, name, value).childrens(children).toString();
	}

	private Stream<String> namedPathStream() {
		if (!isNamed()) return Stream.empty();
		return StreamUtil.prepend(
			namedChildPathStream().map(p -> PathFactory.dot.path(name, ".", p).value), name);
	}

	private Stream<String> namedChildPathStream() {
		return lookup.values().stream().flatMap(n -> n.namedPathStream());
	}

	private Node<?> child(int[] indexes, int i) {
		if (i >= indexes.length) return this;
		if (hasChild(indexes[i])) return children.get(indexes[i]).child(indexes, i + 1);
		return Node.NULL;
	}

	private Node<?> child(List<String> parts, int index) {
		if (index >= parts.size()) return this;
		Node<?> child = childFromPart(parts.get(index));
		return child == null ? Node.NULL : child.child(parts, index + 1);
	}

	private Node<?> childFromPart(String part) {
		Node<?> child = lookup.get(part);
		if (child != null) return child;
		Integer i = PrimitiveUtil.intValue(part);
		if (i != null && hasChild(i)) return children.get(i);
		return null;
	}

}
