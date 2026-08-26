package ceri.ffm.type;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import ceri.common.array.Array;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.text.Regex;
import ceri.common.text.Strings;
import ceri.ffm.core.Layouts;

/**
 * Validated type access path.
 */
public final class Route<P extends PointerType.Indexable<P, ?, ?>> {
	private static final Pattern STEP_REGEX =
		Regex.compile("\\G(\\*|\\[\\d*\\]|\\+\\d*|\\.\\d+|\\.%s)", Regex.Common.JAVA_NAME);
	private final List<Element> elements;
	private final Support<?, ?, ?, ?> root;
	private final Support<?, ?, P, ?> support;
	private final int indexes;

	/**
	 * A routing element.
	 */
	private sealed interface Element extends Functions.Consumer<State>
		permits Deref, Offset, Index {}

	/**
	 * A pointer de-reference routing element.
	 */
	private record Deref(AddressLayout layout) implements Element {
		private static final Deref DEF = new Deref(Layouts.POINTER);

		/**
		 * De-reference constructor.
		 */
		private static Deref of(AddressLayout layout) {
			if (Objects.equals(layout, DEF.layout())) return DEF;
			return new Deref(layout);
		}

		@Override
		public void accept(State state) {
			state.deref(layout());
		}
	}

	/**
	 * A fixed offset routing element.
	 */
	private record Offset(long offset) implements Element {
		@Override
		public void accept(State state) {
			state.offset(offset());
		}
	}

	/**
	 * A dynamic indexed offset routing element.
	 */
	private record Index(int argIndex, int max, long size) implements Element {
		@Override
		public void accept(State state) {
			state.index(argIndex(), max(), size());
		}
	}

	/**
	 * Keeps track of the memory segment, offset and open indexes.
	 */
	private static final class State {
		private final int[] indexes;
		private MemorySegment memory;
		private long offset;

		private State(MemorySegment memory, long offset, int[] indexes) {
			this.memory = memory;
			this.offset = offset;
			this.indexes = indexes;
		}

		private void deref(AddressLayout layout) {
			if (Memory.isNull(memory))
				throw new IllegalArgumentException("Unable to dereference a null pointer");
			memory = memory.get(layout, offset);
			offset = 0L;
		}

		private void index(int argIndex, int max, long size) {
			int index = Array.INT.at(indexes, argIndex, 0);
			if (index < 0)
				throw Exceptions.illegalArg("Index %d must be >= 0: %d", argIndex, index);
			if (max > 0 && index >= max)
				throw Exceptions.illegalArg("Index %d must be < %d: %d", argIndex, max, index);
			offset(index * size);
		}

		private void offset(long offset) {
			this.offset += offset;
		}

		private MemorySegment resize(long length) {
			if (Memory.size(memory) >= offset + length) return Memory.slice(memory, offset);
			return Memory.resize(memory, offset, length); // only resize if smaller
		}
	}

	/**
	 * Builds a route by navigating types.
	 */
	public static final class Builder {
		private final List<Element> elements = Lists.of();
		private final Support<?, ?, ?, ?> root;
		private Support<?, ?, ?, ?> support;
		private int indexes = 0;

		private Builder(Support<?, ?, ?, ?> support) {
			root = support;
			this.support = support;
		}

		/**
		 * Parses the pattern into steps: {@code * [] [i] () (i) .i .name} for dereference, open
		 * array index, array index i, open offset index, offset index i, field by index, and field
		 * by name respectively.
		 */
		public Builder parse(String pattern) {
			if (Strings.isEmpty(pattern)) return this;
			var m = STEP_REGEX.matcher(pattern);
			int last = 0;
			while (m.find()) {
				parseStep(m.group(1));
				last = m.end();
			}
			if (last < pattern.length()) throw Exceptions.illegalArg("Invalid pattern from %d: %s",
				last, pattern.substring(last));
			return this;
		}

		/**
		 * Appends an existing route. Fails if the route's root does not match the current type.
		 */
		public Builder add(Route<?> route) {
			if (!support.equals(route.root)) throw Exceptions
				.illegalArg("Route start type must match %s: %s", support, route.root);
			for (var element : route.elements)
				if (!(element instanceof Index index)) add(element);
				else add(new Index(indexes++, index.max(), index.size()));
			support = route.support;
			return this;
		}

		/**
		 * Adds a pointer de-reference routing element. Fails if the current type is not a pointer.
		 */
		public Builder deref() {
			if (!(support instanceof PointerType.Supporter<?> pointer))
				throw new IllegalArgumentException("Must be a pointer type: " + support);
			support = pointer.support();
			return add(Deref.of(pointer.layout()));
		}

		/**
		 * Adds an open array index offset for the current type. Fails if the current type is not an
		 * array.
		 */
		public Builder array() {
			if (!(support instanceof Support.OfArray<?> array))
				throw new IllegalArgumentException("Must be an array type: " + support);
			support = array.elementSupport();
			return add(new Index(indexes++, array.elements(), support.layoutSize()));
		}

		/**
		 * Adds a fixed array index offset for the current type. Fails if the current type is not an
		 * array.
		 */
		public Builder array(int i) {
			if (!(support instanceof Support.OfArray<?> array))
				throw new IllegalArgumentException("Must be an array type: " + support);
			if (i < 0) throw Exceptions.illegalArg("Index must be >= 0: %d", i);
			int max = array.elements();
			if (max > 0 && i >= max) throw Exceptions.illegalArg("Index must be < %d: %d", max, i);
			support = array.elementSupport();
			return add(new Offset(i * support.layoutSize()));
		}

		/**
		 * Adds an open index offset for the current type.
		 */
		public Builder index() {
			return add(new Index(indexes++, 0, support.layoutSize()));
		}

		/**
		 * Adds a fixed index offset for the current type.
		 */
		public Builder index(int i) {
			if (i < 0) throw Exceptions.illegalArg("Index must be >= 0: %d", i);
			return add(new Offset(i * support.layoutSize()));
		}

		/**
		 * Adds a field offset by index. Fails if the current type is not a struct or union.
		 */
		public Builder field(int i) {
			if (!(support instanceof Group.Supporter<?, ?> group))
				throw new IllegalArgumentException("Must be a union or struct: " + support);
			var member = group.config.member(i);
			if (member == null) throw Exceptions.illegalArg("No member at index %d: %s", i,
				Reflect.name(group.type()));
			support = member.support();
			return add(new Offset(member.offset()));
		}

		/**
		 * Adds a field offset by name. Fails if the current type is not a struct or union.
		 */
		public Builder field(String name) {
			if (!(support instanceof Group.Supporter<?, ?> group))
				throw new IllegalArgumentException("Must be a union or struct: " + support);
			var member = group.config.member(name);
			if (member == null)
				throw Exceptions.illegalArg("No member '%s': %s", name, Reflect.name(group.type()));
			support = member.support();
			return add(new Offset(member.offset()));
		}

		/**
		 * Creates the route from current elements, with unverified end point type.
		 */
		public <P extends PointerType.Indexable<P, ?, ?>> Route<P> as() {
			return new Route<>(root, Reflect.unchecked(support), reduce(elements), indexes);
		}

		/**
		 * Creates the route from current elements, whose end point must be of the given type.
		 */
		public <P extends PointerType.Indexable<P, ?, ?>> Route<P> as(Class<?> cls) {
			if (support.type() != cls) throw wrongType(cls);
			return new Route<>(root, Reflect.unchecked(support), reduce(elements), indexes);
		}

		/**
		 * Appends an existing route. Fails if the route's root does not match the current type.
		 */
		public <P extends PointerType.Indexable<P, ?, ?>> Route<P> as(Route<P> route) {
			return add(route).as();
		}

		private RuntimeException wrongType(Class<?> cls) {
			return Exceptions.illegalArg("Route end point must be of type %s: %s",
				Reflect.name(cls), support);
		}

		private Builder add(Element element) {
			elements.add(element);
			return this;
		}

		private Builder parseStep(String step) {
			int n = step.length();
			return switch (step.charAt(0)) {
				case '*' -> deref();
				case '[' -> n == 2 ? array() : array(Integer.parseInt(step.substring(1, n - 1)));
				case '+' -> n == 1 ? index() : index(Integer.parseInt(step.substring(1)));
				default -> Maths.within(step.charAt(1), '0', '9') ?
					field(Integer.parseInt(step.substring(1))) : field(step.substring(1));
			};
		}
	}

	/**
	 * Start building a route with the given type as root.
	 */
	public static Builder builder(Support<?, ?, ?, ?> support) {
		if (support == null) return null;
		return new Builder(support);
	}

	private Route(Support<?, ?, ?, ?> root, Support<?, ?, P, ?> support, List<Element> elements,
		int indexes) {
		this.root = root;
		this.elements = elements;
		this.support = support;
		this.indexes = indexes;
	}

	/**
	 * Returns the number of open indexes in the route.
	 */
	public int indexes() {
		return indexes;
	}

	/**
	 * Start extending the route.
	 */
	public Builder sub() {
		return builder(root).add(this);
	}

	/**
	 * Extends the route using the pattern, with unverified end point type.
	 */
	public <R extends PointerType.Indexable<R, ?, ?>> Route<R> sub(String pattern) {
		return sub().parse(pattern).as();
	}

	/**
	 * Extends with the given route.
	 */
	public <R extends PointerType.Indexable<R, ?, ?>> Route<R> sub(Route<R> route) {
		return sub().as(route);
	}

	/**
	 * Returns the memory segment for the end of the route. The route starts at the provided memory
	 * segment offset, with given indexes applied as open indexes in order. Missing indexes are
	 * applied as 0.
	 */
	public MemorySegment memory(MemorySegment memory, long offset, int... indexes) {
		return state(memory, offset, indexes).resize(support.layoutSize());
	}

	/**
	 * Returns the memory segment for the end of the route. The route starts at the provided memory
	 * segment offset, with given indexes applied as open indexes in order. Missing indexes are
	 * applied as 0.
	 */
	public MemorySegment memory(PointerType pointer, int... indexes) {
		return memory(PointerType.memory(pointer), 0L, indexes);
	}

	/**
	 * Returns a typed pointer for the end of the route. The route starts at the provided memory
	 * segment offset, with given indexes applied as open indexes in order. Missing indexes are
	 * applied as 0.
	 */
	public P pointer(MemorySegment memory, long offset, int... indexes) {
		return support.pointer(memory(memory, offset, indexes));
	}

	/**
	 * Returns a typed pointer for the end of the route. The route starts at the provided pointer,
	 * with given indexes applied as open indexes in order. Missing indexes are applied as 0.
	 */
	public P pointer(PointerType pointer, int... indexes) {
		return pointer(PointerType.memory(pointer), 0L, indexes);
	}

	// support

	private State state(MemorySegment memory, long offset, int... indexes) {
		var state = new State(memory, offset, indexes);
		elements.forEach(element -> element.accept(state));
		return state;
	}

	private static List<Element> reduce(List<Element> elements) {
		var compact = Lists.<Element>of();
		long offset = 0L;
		for (var element : elements) {
			switch (element) {
				case Deref _ -> {
					if (offset > 0L) compact.add(new Offset(offset));
					offset = 0L;
					compact.add(element);
				}
				case Offset o -> offset += o.offset();
				case Index _ -> compact.add(element);
			}
		}
		if (offset > 0L) compact.add(new Offset(offset));
		return Immutable.wrap(compact);
	}
}
