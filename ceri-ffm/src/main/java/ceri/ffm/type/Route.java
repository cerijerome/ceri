package ceri.ffm.type;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Objects;
import ceri.common.array.Array;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Layouts;

/**
 * Validated type access path.
 */
public sealed class Route<T, P extends PointerType.Raw>
	permits Route.OfBool, Route.OfChar, Route.OfByte, Route.OfShort, Route.OfInt, Route.OfLong,
	Route.OfFloat, Route.OfDouble, Route.Typed {
	private final List<Element> elements;
	private final Support<?, ?, ?, ?> root;
	private final Support<T, ?, P, ?> support;
	private final int indexes;

	/**
	 * A route ending with the primitive type.
	 */
	public static final class OfBool extends Route<Boolean, Pointer.OfBool> {
		private OfBool(Support<?, ?, ?, ?> root, Primitive.OfBool support, List<Element> elements,
			int indexes) {
			super(root, support, elements, indexes);
		}
	}

	/**
	 * A route ending with the primitive type.
	 */
	public static final class OfChar extends Route<Character, Pointer.OfChar> {
		private OfChar(Support<?, ?, ?, ?> root, Primitive.OfChar support, List<Element> elements,
			int indexes) {
			super(root, support, elements, indexes);
		}
	}

	/**
	 * A route ending with the primitive type.
	 */
	public static final class OfByte extends Route<Byte, Pointer.OfByte> {
		private OfByte(Support<?, ?, ?, ?> root, Primitive.OfByte support, List<Element> elements,
			int indexes) {
			super(root, support, elements, indexes);
		}
	}

	/**
	 * A route ending with the primitive type.
	 */
	public static final class OfShort extends Route<Short, Pointer.OfShort> {
		private OfShort(Support<?, ?, ?, ?> root, Primitive.OfShort support, List<Element> elements,
			int indexes) {
			super(root, support, elements, indexes);
		}
	}

	/**
	 * A route ending with the primitive type.
	 */
	public static final class OfInt extends Route<Integer, Pointer.OfInt> {
		private OfInt(Support<?, ?, ?, ?> root, Primitive.OfInt support, List<Element> elements,
			int indexes) {
			super(root, support, elements, indexes);
		}
	}

	/**
	 * A route ending with the primitive type.
	 */
	public static final class OfLong extends Route<Long, Pointer.OfLong> {
		private OfLong(Support<?, ?, ?, ?> root, Primitive.OfLong support, List<Element> elements,
			int indexes) {
			super(root, support, elements, indexes);
		}
	}

	/**
	 * A route ending with the primitive type.
	 */
	public static final class OfFloat extends Route<Float, Pointer.OfFloat> {
		private OfFloat(Support<?, ?, ?, ?> root, Primitive.OfFloat support, List<Element> elements,
			int indexes) {
			super(root, support, elements, indexes);
		}
	}

	/**
	 * A route ending with the primitive type.
	 */
	public static final class OfDouble extends Route<Double, Pointer.OfDouble> {
		private OfDouble(Support<?, ?, ?, ?> root, Primitive.OfDouble support,
			List<Element> elements, int indexes) {
			super(root, support, elements, indexes);
		}
	}

	/**
	 * A route ending with an object type.
	 */
	public static final class Typed<T> extends Route<T, Pointer<T>> {
		private Typed(Support<?, ?, ?, ?> root, Support.Typed<T, ?> support, List<Element> elements,
			int indexes) {
			super(root, support, elements, indexes);
		}
	}

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
		public static Deref of(AddressLayout layout) {
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
	 * Keeps track of the memory segment, offset and open array indexes.
	 */
	private static class State {
		private final int[] indexes;
		private MemorySegment memory;
		private long offset;

		private State(MemorySegment memory, long offset, int[] indexes) {
			this.memory = memory;
			this.offset = offset;
			this.indexes = indexes;
		}

		private void deref(AddressLayout layout) {
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

		private MemorySegment slice() {
			return Memory.slice(memory, offset);
		}
	}

	/**
	 * Builds a route by navigating types.
	 */
	public static class Builder {
		private final List<Element> elements = Lists.of();
		private final Support<?, ?, ?, ?> root;
		private Support<?, ?, ?, ?> support;
		private int indexes = 0;

		private Builder(Support<?, ?, ?, ?> support) {
			root = support;
			this.support = support;
		}

		/**
		 * Appends an existing route. Fails if the route's root does not match the current type.
		 */
		public Builder add(Route<?, ?> route) {
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
		 * Creates the route from current elements, whose end point must match the primitive type.
		 */
		public Route.OfBool asBool() {
			if (!(support instanceof Primitive.OfBool p)) throw wrongType(boolean.class);
			return new OfBool(root, p, reduce(elements), indexes);
		}

		/**
		 * Creates the route from current elements, whose end point must match the primitive type.
		 */
		public Route.OfChar asChar() {
			if (!(support instanceof Primitive.OfChar p)) throw wrongType(char.class);
			return new OfChar(root, p, reduce(elements), indexes);
		}

		/**
		 * Creates the route from current elements, whose end point must match the primitive type.
		 */
		public Route.OfByte asByte() {
			if (!(support instanceof Primitive.OfByte p)) throw wrongType(byte.class);
			return new OfByte(root, p, reduce(elements), indexes);
		}

		/**
		 * Creates the route from current elements, whose end point must match the primitive type.
		 */
		public Route.OfShort asShort() {
			if (!(support instanceof Primitive.OfShort p)) throw wrongType(short.class);
			return new OfShort(root, p, reduce(elements), indexes);
		}

		/**
		 * Creates the route from current elements, whose end point must match the primitive type.
		 */
		public Route.OfInt asInt() {
			if (!(support instanceof Primitive.OfInt p)) throw wrongType(int.class);
			return new OfInt(root, p, reduce(elements), indexes);
		}

		/**
		 * Creates the route from current elements, whose end point must match the primitive type.
		 */
		public Route.OfLong asLong() {
			if (!(support instanceof Primitive.OfLong p)) throw wrongType(long.class);
			return new OfLong(root, p, reduce(elements), indexes);
		}

		/**
		 * Creates the route from current elements, whose end point must match the primitive type.
		 */
		public Route.OfFloat asFloat() {
			if (!(support instanceof Primitive.OfFloat p)) throw wrongType(float.class);
			return new OfFloat(root, p, reduce(elements), indexes);
		}

		/**
		 * Creates the route from current elements, whose end point must match the primitive type.
		 */
		public Route.OfDouble asDouble() {
			if (!(support instanceof Primitive.OfDouble p)) throw wrongType(double.class);
			return new OfDouble(root, p, reduce(elements), indexes);
		}

		/**
		 * Creates the route from current elements, whose end point must be of the given type.
		 */
		public <T> Route.Typed<T> as(Class<T> cls) {
			cls = Reflect.unchecked(Reflect.boxed(cls));
			if (support.type() != cls || !(support instanceof Support.Typed)) throw wrongType(cls);
			return new Typed<>(root, Reflect.unchecked(support), reduce(elements), indexes);
		}

		/**
		 * Appends an existing route. Fails if the route's root does not match the current type.
		 */
		public <R extends Route<?, ?>> R as(R route) {
			add(route);
			return Reflect.unchecked(switch (route) {
				case OfBool _ -> asBool();
				case OfChar _ -> asChar();
				case OfByte _ -> asByte();
				case OfShort _ -> asShort();
				case OfInt _ -> asInt();
				case OfLong _ -> asLong();
				case OfFloat _ -> asFloat();
				case OfDouble _ -> asDouble();
				default -> as(support.type());
			});
		}

		private RuntimeException wrongType(Class<?> cls) {
			return Exceptions.illegalArg("Route end point must be of type %s: %s",
				Reflect.name(cls), support);
		}

		private Builder add(Element element) {
			elements.add(element);
			return this;
		}
	}

	// TODO:
	// - limit index to pointers?
	// - check for null pointers
	// - combine routes (builder from route, add route to builder)
	// - extend group to get/read/write fields from pointer
	// - build route from string representation

	/**
	 * Start building a route with the given type as root.
	 */
	public static Builder builder(Support<?, ?, ?, ?> support) {
		return new Builder(support);
	}

	private Route(Support<?, ?, ?, ?> root, Support<T, ?, P, ?> support, List<Element> elements,
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
	 * Returns the memory segment for the end of the route. The route starts at the provided memory
	 * segment offset, with given indexes applied as open indexes in order. Missing indexes are
	 * applied as 0.
	 */
	public MemorySegment memory(MemorySegment memory, long offset, int... indexes) {
		return state(memory, offset, indexes).slice();
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
