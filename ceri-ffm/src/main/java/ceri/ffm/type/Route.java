package ceri.ffm.type;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import ceri.common.array.Array;
import ceri.common.array.RawArray;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.stream.LongStream;
import ceri.common.text.Regex;
import ceri.common.text.Strings;
import ceri.ffm.core.Formats;
import ceri.ffm.core.Layouts;
import ceri.ffm.reflect.Refine.Out;
import ceri.ffm.reflect.Refine.Size;
import ceri.ffm.type.Group.Fields;

/**
 * A validated type access path. Provides access to field pointers at the end of the route. Also
 * provides object value to memory synchronization if no pointer dereference is required.
 */
public final class Route<T, P extends PointerType.Indexable<P, ?, ?>> {
	private static final Pattern STEP_REGEX =
		Regex.compile("\\G(\\*|\\[\\d*\\]|\\+\\d*|\\.\\d+|\\.%s)", Regex.Common.JAVA_NAME);
	private final Support<T, ?, ?, ?> root;
	private final List<MemoryElement> memoryElements;
	private final List<ValueElement> valueElements;
	private final Support<?, ?, P, ?> support;
	private final int indexes;

	@Fields({ "i", "b", "bb", "pb", "s" })
	public static class A extends Struct<A> {
		public static final Supporter<A> $ = support(A.class);
		public static Route<A, Pointer.OfInt> B_I = $.route("+.b.i");
		public static Route<A, Pointer.OfInt> BB_II = $.route("+.bb[].ii[]");
		public @Out int i;
		public B b;
		public B[] bb = new B[3];
		public @Out Pointer<B> pb;
		public @Out @Size(4) String s;
	}

	@Fields({ "i", "ii", "s" })
	public static class B extends Struct<B> {
		public static final Supporter<B> $ = support(B.class);
		public int i;
		public int[] ii = new int[4];
		public @Out @Size(3) String s;
	}

	public static void main(String[] args) {
		var pa = A.$.pointerOfArray(3);
		var a = A.$.initArray(3);
		var times = Lists.<Long>of();
		// System.out.println(Caller.Transform.FULL.apply(a));
		for (int r = 0; r < 50; r++) {
			for (int i = 0; i < a.length; i++) {
				A.B_I.pointer(pa, i).write(i + 1);
				for (int j = 0; j < 3; j++) {
					for (int k = 0; k < 4; k++) {
						A.BB_II.pointer(pa, i, j, k).write(i * 100 + j * 10 + k);
					}
				}
			}
			var t0 = System.nanoTime();
			for (int i = 0; i < a.length; i++) {
				A.B_I.sync(a, pa, i).read();
				for (int j = 0; j < 3; j++) {
					for (int k = 0; k < 4; k++) {
						A.BB_II.sync(a, pa, i, j, k).read();
					}
				}
			}
			var t1 = System.nanoTime();
			times.add(t1 - t0);
			if (r < 5) times.clear();
			// System.out.println(Formats.verbose(a));
		}
		times.forEach(t -> System.out.println((t + 500) / 1000 + "us"));
		System.out.println(((long) LongStream.from(times).average() / 1000) + "us average");
	}

	/**
	 * Provides synchronization between an object value and a memory location for the end of a
	 * route,
	 */
	public interface Sync<P extends PointerType.Indexable<P, ?, ?>> {
		/**
		 * Updates the value from memory at the end of the route. Returns false if unable to update.
		 */
		boolean read();

		/**
		 * Writes the value to memory at the end of the route. Returns false if unable to write.
		 */
		boolean write();

		/**
		 * Returns a typed pointer to the end of the route.
		 */
		P pointer();
	}

	/**
	 * A routing element for values, that operates on navigation state.
	 */
	private sealed interface ValueElement extends Functions.Consumer<State<?>>
		permits ArrayElement, ArrayIndex, Member {
		/**
		 * Sets the route endpoint value.
		 */
		void set(State<?> state, Object value);

		/**
		 * Gets the route endpoint value.
		 */
		Object get(State<?> state);

		/**
		 * Adjust the element's open index based on the given starting index.
		 */
		default ValueElement shiftIndex(@SuppressWarnings("unused") int start) {
			return this;
		}
	}

	/**
	 * A fixed index array value routing element.
	 */
	private record ArrayElement(int index) implements ValueElement {
		@Override
		public void accept(State<?> state) {
			state.arrayElement(index());
		}

		@Override
		public void set(State<?> state, Object value) {
			state.setArrayElement(index(), value);
		}

		@Override
		public Object get(State<?> state) {
			return state.getArrayElement(index());
		}
	}

	/**
	 * A dynamically indexed array value routing element.
	 */
	private record ArrayIndex(int argIndex) implements ValueElement {
		@Override
		public void accept(State<?> state) {
			state.arrayIndex(argIndex());
		}

		@Override
		public void set(State<?> state, Object value) {
			state.setArrayIndex(argIndex(), value);
		}

		@Override
		public Object get(State<?> state) {
			return state.getArrayIndex(argIndex());
		}

		@Override
		public ValueElement shiftIndex(int start) {
			return new ArrayIndex(start + argIndex());
		}
	}

	/**
	 * A member field value routing element.
	 */
	private record Member(Group.Member<?> member) implements ValueElement {
		@Override
		public void accept(State<?> state) {
			state.member(member());
		}

		@Override
		public void set(State<?> state, Object value) {
			state.setMember(member(), value);
		}

		@Override
		public Object get(State<?> state) {
			return state.getMember(member());
		}
	}

	/**
	 * A routing element for memory, that operates on state.
	 */
	private sealed interface MemoryElement extends Functions.Consumer<State<?>>
		permits Deref, MemoryOffset, MemoryIndex {
		/**
		 * Adjust the element's open index based on the given starting index.
		 */
		default MemoryElement shiftIndex(@SuppressWarnings("unused") int start) {
			return this;
		}
	}

	/**
	 * A pointer de-reference routing element.
	 */
	private record Deref(AddressLayout layout) implements MemoryElement {
		private static final Deref DEF = new Deref(Layouts.POINTER);

		/**
		 * De-reference constructor.
		 */
		private static Deref of(AddressLayout layout) {
			if (Objects.equals(layout, DEF.layout())) return DEF;
			return new Deref(layout);
		}

		@Override
		public void accept(State<?> state) {
			state.deref(layout());
		}
	}

	/**
	 * A fixed memory offset routing element.
	 */
	private record MemoryOffset(long offset) implements MemoryElement {
		@Override
		public void accept(State<?> state) {
			state.offset(offset());
		}
	}

	/**
	 * A dynamically indexed memory offset routing element.
	 */
	private record MemoryIndex(int argIndex, int max, long size) implements MemoryElement {
		@Override
		public void accept(State<?> state) {
			state.index(argIndex(), max(), size());
		}

		@Override
		public MemoryElement shiftIndex(int start) {
			return new MemoryIndex(start + argIndex(), max(), size());
		}
	}

	/**
	 * Builds a route for memory and value access by navigating types.
	 */
	public static final class Builder<T> {
		private final Support<T, ?, ?, ?> root;
		private final List<MemoryElement> memoryElements = Lists.of();
		private List<ValueElement> valueElements = Lists.of(); // null if not supported
		private Support<?, ?, ?, ?> support;
		private int indexes = 0;

		private Builder(Support<T, ?, ?, ?> support) {
			root = support;
			this.support = support;
		}

		/**
		 * Parses the pattern into steps: {@code * [] [i] () (i) .i .name} for dereference, open
		 * array index, array index i, open offset index, offset index i, field by index, and field
		 * by name respectively.
		 */
		public Builder<T> parse(String pattern) {
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
		public Builder<T> add(Route<?, ?> route) {
			if (!support.equals(route.root)) throw Exceptions
				.illegalArg("Route start type must match %s: %s", support, route.root);
			if (route.isEmpty()) return this;
			for (var element : route.memoryElements)
				memoryElements.add(element.shiftIndex(indexes));
			if (!route.canSync()) noSync();
			if (canSync()) for (var element : route.valueElements)
				valueElements.add(element.shiftIndex(indexes));
			indexes += route.indexes();
			support = route.support;
			return this;
		}

		/**
		 * Adds a pointer de-reference routing element. Fails if the current type is not a pointer.
		 * From this point there is no source object reference, so a sync is not possible.
		 */
		public Builder<T> deref() {
			if (!(support instanceof PointerType.Supporter<?> pointer))
				throw new IllegalArgumentException("Must be a pointer type: " + support);
			support = pointer.support();
			memoryElements.add(Deref.of(pointer.layout()));
			noSync(); // no longer able to sync object values (object ref unavailable)
			return this;
		}

		/**
		 * Adds an open array index offset for the current type. Fails if the current type is not an
		 * array.
		 */
		public Builder<T> array() {
			if (!(support instanceof Support.OfArray<?> array))
				throw new IllegalArgumentException("Must be an array type: " + support);
			support = array.elementSupport();
			memoryElements.add(new MemoryIndex(indexes, array.elements(), support.layoutSize()));
			if (canSync()) valueElements.add(new ArrayIndex(indexes));
			indexes++;
			return this;
		}

		/**
		 * Adds a fixed array index offset for the current type. Fails if the current type is not an
		 * array.
		 */
		public Builder<T> array(int index) {
			if (!(support instanceof Support.OfArray<?> array))
				throw new IllegalArgumentException("Must be an array type: " + support);
			validateArrayIndex(index, array);
			support = array.elementSupport();
			memoryElements.add(new MemoryOffset(index * support.layoutSize()));
			if (canSync()) valueElements.add(new ArrayElement(index));
			return this;
		}

		/**
		 * Adds an open index offset for the current type.
		 */
		public Builder<T> index() {
			memoryElements.add(new MemoryIndex(indexes, 0, support.layoutSize()));
			if (canSync()) valueElements.add(new ArrayIndex(indexes));
			indexes++;
			return this;
		}

		/**
		 * Adds a fixed index offset for the current type.
		 */
		public Builder<T> index(int index) {
			if (index < 0) throw Exceptions.illegalArg("Index must be >= 0: %d", index);
			memoryElements.add(new MemoryOffset(index * support.layoutSize()));
			if (canSync()) valueElements.add(new ArrayElement(index));
			return this;
		}

		/**
		 * Adds a field offset by index. Fails if the current type is not a struct or union.
		 */
		public Builder<T> field(int i) {
			if (!(support instanceof Group.Supporter<?, ?> group))
				throw new IllegalArgumentException("Must be a union or struct: " + support);
			var member = group.config.member(i);
			if (member == null) throw Exceptions.illegalArg("No member at index %d: %s", i,
				Reflect.name(group.type()));
			return member(member);
		}

		/**
		 * Adds a field offset by name. Fails if the current type is not a struct or union.
		 */
		public Builder<T> field(String name) {
			if (!(support instanceof Group.Supporter<?, ?> group))
				throw new IllegalArgumentException("Must be a union or struct: " + support);
			var member = group.config.member(name);
			if (member == null)
				throw Exceptions.illegalArg("No member '%s': %s", name, Reflect.name(group.type()));
			return member(member);
		}

		/**
		 * Creates the route from current elements, with unverified end point type.
		 */
		public <P extends PointerType.Indexable<P, ?, ?>> Route<T, P> as() {
			return new Route<>(root, Reflect.unchecked(support), reduce(memoryElements),
				Immutable.wrap(valueElements), indexes);
		}

		/**
		 * Creates the route from current elements, whose end point must be of the given type.
		 */
		public <P extends PointerType.Indexable<P, ?, ?>> Route<T, P> as(Class<?> cls) {
			if (support.type() != cls) throw wrongType(cls);
			return new Route<>(root, Reflect.unchecked(support), reduce(memoryElements),
				Immutable.wrap(valueElements), indexes);
		}

		/**
		 * Appends an existing route. Fails if the route's root does not match the current type.
		 */
		public <P extends PointerType.Indexable<P, ?, ?>> Route<T, P> as(Route<?, P> route) {
			return add(route).as();
		}

		private Builder<T> member(Group.Member<?> member) {
			support = member.support();
			memoryElements.add(new MemoryOffset(member.offset()));
			if (canSync()) valueElements.add(new Member(member));
			return this;
		}

		private RuntimeException wrongType(Class<?> cls) {
			return Exceptions.illegalArg("Route end point must be of type %s: %s",
				Reflect.name(cls), support);
		}

		private boolean canSync() {
			return valueElements != null;
		}

		private void noSync() {
			valueElements = null;
		}

		private Builder<T> parseStep(String step) {
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
	 * Navigation state, tracking memory and values to the end of the route.
	 */
	private static class State<P extends PointerType.Indexable<P, ?, ?>> implements Sync<P> {
		private final Support<?, ?, P, ?> support;
		private final int[] indexes;
		private MemorySegment memory;
		private long offset;
		private ValueElement lastValueElement = null;
		private Object parent = null;
		private Object value;
		private volatile P pointer = null;

		private State(Support<?, ?, P, ?> support, Object value, MemorySegment memory, long offset,
			int[] indexes) {
			this.support = support;
			this.value = value;
			this.memory = memory;
			this.offset = offset;
			this.indexes = indexes;
		}

		@Override
		public boolean read() {
			if (support.mutable()) return support.read(memory, Reflect.unchecked(value));
			if (parent == null || lastValueElement == null) return false;
			lastValueElement.set(this, support.get(memory));
			return true;
		}

		@Override
		public boolean write() {
			if (support.mutable()) return support.write(memory, Reflect.unchecked(value));
			if (parent == null || lastValueElement == null) return false;
			support.write(memory, Reflect.unchecked(lastValueElement.get(this)));
			return true;
		}

		@Override
		public P pointer() {
			var pointer = this.pointer;
			if (pointer == null) {
				pointer = createPointer();
				this.pointer = pointer;
			}
			return pointer;
		}

		private MemorySegment resize() {
			long length = support.layoutSize();
			if (Memory.size(memory) >= offset + length) memory = Memory.slice(memory, offset);
			else memory = Memory.resize(memory, offset, length); // only resize if smaller
			return memory;
		}

		private P createPointer() {
			return support.pointer(memory);
		}

		private MemorySegment memory() {
			return memory;
		}

		private void setArrayElement(int index, Object value) {
			if (value != null) RawArray.set(parent, index, value);
		}

		private Object getArrayElement(int index) {
			return RawArray.get(parent, index);
		}

		private void setArrayIndex(int argIndex, Object value) {
			setArrayElement(index(argIndex), value);
		}

		private Object getArrayIndex(int argIndex) {
			return getArrayElement(index(argIndex));
		}

		private void setMember(Group.Member<?> member, Object value) {
			member.set(Reflect.unchecked(parent), Reflect.unchecked(value));
		}

		private Object getMember(Group.Member<?> member) {
			return member.get(Reflect.unchecked(parent));
		}

		private void applyMemory(List<MemoryElement> elements) {
			for (var element : elements)
				element.accept(this);
		}

		private void applyValue(List<ValueElement> elements) {
			for (var element : elements)
				element.accept(this);
			if (!support.mutable()) lastValueElement = Lists.last(elements);
		}

		private void arrayElement(int index) {
			if (value == null) return;
			boolean isArray = RawArray.isArray(value);
			int len = isArray ? RawArray.length(value) : 1;
			if (!Maths.within(index, 0, len - 1))
				throw Exceptions.illegalArg("No element [%d]: %s", index, Formats.compact(value));
			if (isArray) value(RawArray.get(value, index));
		}

		private void arrayIndex(int argIndex) {
			arrayElement(index(argIndex));
		}

		private void member(Group.Member<?> member) {
			if (value == null) return;
			if (RawArray.isArray(value) && RawArray.length(value) > 0)
				value(RawArray.get(value, 0));
			if (!(value instanceof Group<?, ?> group)) throw Exceptions
				.illegalArg("No group member .%s: %s", member.name(), Formats.compact(value));
			value(member.get(group));
		}

		private Object value(Object value) {
			parent = this.value;
			this.value = value;
			return value;
		}

		private void deref(AddressLayout layout) {
			if (Memory.isNull(memory))
				throw new IllegalArgumentException("Unable to dereference a null pointer");
			memory = memory.get(layout, offset);
			offset = 0L;
		}

		private void index(int argIndex, int max, long size) {
			int index = index(argIndex);
			validateArgIndex(index(argIndex), argIndex, max, indexes);
			offset(index * size);
		}

		private void offset(long offset) {
			this.offset += offset;
		}

		private int index(int argIndex) {
			return Array.INT.at(indexes, argIndex, 0);
		}
	}

	/**
	 * Start building a route with the given type as root.
	 */
	public static <T> Builder<T> builder(Support<T, ?, ?, ?> support) {
		if (support == null) return null;
		return new Builder<>(support);
	}

	private Route(Support<T, ?, ?, ?> root, Support<?, ?, P, ?> support,
		List<MemoryElement> memoryElements, List<ValueElement> valueElements, int indexes) {
		this.root = root;
		this.memoryElements = memoryElements;
		this.valueElements = valueElements;
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
	public Builder<T> sub() {
		return builder(root).add(this);
	}

	/**
	 * Extends the route using the pattern, with unverified end point type.
	 */
	public <R extends PointerType.Indexable<R, ?, ?>> Route<T, R> sub(String pattern) {
		return sub().parse(pattern).as();
	}

	/**
	 * Extends with the given route.
	 */
	public <R extends PointerType.Indexable<R, ?, ?>> Route<T, R> sub(Route<?, R> route) {
		return sub().as(route);
	}

	/**
	 * Returns the memory segment for the end of the route. The route starts at the provided memory
	 * segment offset, with given indexes applied as open indexes in order. Missing indexes are
	 * applied as 0.
	 */
	public MemorySegment memory(MemorySegment memory, long offset, int... indexes) {
		return state(null, memory, offset, indexes).memory();
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
	 * applied as 0. For struct flex member arrays, the pointer type is for the array element.
	 */
	public P pointer(MemorySegment memory, long offset, int... indexes) {
		return state(null, memory, offset, indexes).createPointer();
	}

	/**
	 * Returns a typed pointer for the end of the route. The route starts at the provided pointer,
	 * with given indexes applied as open indexes in order. Missing indexes are applied as 0. For
	 * struct flex member arrays, the pointer type is for the array element.
	 */
	public P pointer(PointerType pointer, int... indexes) {
		return pointer(PointerType.memory(pointer), 0L, indexes);
	}

	/**
	 * Returns a synchronizer for the end of the route, allowing the object value to be read
	 * from/written to memory. The route starts at the provided pointer, with given indexes applied
	 * as open indexes in order. Missing indexes are applied as 0. For struct flex member arrays,
	 * the type is for the array element. Behavior is undefined if the value is modified outside of
	 * the synchronizer.
	 */
	public Sync<P> sync(T value, MemorySegment memory, long offset, int... indexes) {
		checkSync();
		return state(value, memory, offset, indexes);
	}

	/**
	 * Returns a synchronizer for the end of the route, allowing the object value to be read
	 * from/written to memory. The route starts at the provided pointer, with given indexes applied
	 * as open indexes in order. Missing indexes are applied as 0. For struct flex member arrays,
	 * the type is for the array element. Behavior is undefined if the value is modified outside of
	 * the synchronizer.
	 */
	public Sync<P> sync(T value, PointerType pointer, int... indexes) {
		return sync(value, PointerType.memory(pointer), 0L, indexes);
	}

	/**
	 * Returns a synchronizer for the end of the route, allowing the object value to be read
	 * from/written to memory. The route starts at the provided pointer, with given indexes applied
	 * as open indexes in order. Missing indexes are applied as 0. For struct flex member arrays,
	 * the type is for the array element. Behavior is undefined if the value is modified outside of
	 * the synchronizer.
	 */
	public Sync<P> sync(T[] array, MemorySegment memory, long offset, int... indexes) {
		checkSync();
		return state(array, memory, offset, indexes);
	}

	/**
	 * Returns a synchronizer for the end of the route, allowing the object value to be read
	 * from/written to memory. The route starts at the provided pointer, with given indexes applied
	 * as open indexes in order. Missing indexes are applied as 0. For struct flex member arrays,
	 * the type is for the array element. Behavior is undefined if the value is modified outside of
	 * the synchronizer.
	 */
	public Sync<P> sync(T[] array, PointerType pointer, int... indexes) {
		return sync(array, PointerType.memory(pointer), 0L, indexes);
	}

	/**
	 * Returns true if there are no routing elements.
	 */
	public boolean isEmpty() {
		return memoryElements.isEmpty() && valueElements.isEmpty();
	}

	/**
	 * Returns true if this route supports value and memory synchronization.
	 */
	public boolean canSync() {
		return support.mutable() || !valueElements.isEmpty();
	}

	// support

	private void checkSync() {
		if (!canSync()) throw new IllegalArgumentException("Route sync not available: dereference");
	}

	private State<P> state(Object value, MemorySegment memory, long offset, int... indexes) {
		var state = new State<>(support, value, memory, offset, indexes);
		state.applyMemory(memoryElements);
		if (value != null) state.applyValue(valueElements);
		state.resize();
		return state;
	}

	private static void validateArgIndex(int index, int argIndex, int max, int[] indexes) {
		if (index < 0) throw Exceptions.illegalArg("Index %d must be >= 0: %d %s", argIndex, index,
			RawArray.toString(indexes));
		if (max > 0 && index >= max) throw Exceptions.illegalArg("Index %d must be < %d: %d %s",
			argIndex, max, index, RawArray.toString(indexes));
	}

	private static void validateArrayIndex(int index, Support.OfArray<?> array) {
		if (index < 0) throw Exceptions.illegalArg("Index must be >= 0: %d %s", index, array);
		int max = array.elements();
		if (max > 0 && index >= max)
			throw Exceptions.illegalArg("Index must be < %d: %d %s", max, index, array);
	}

	private static List<MemoryElement> reduce(List<MemoryElement> elements) {
		var compact = Lists.<MemoryElement>of();
		long offset = 0L;
		for (var element : elements) {
			switch (element) {
				case MemoryOffset o -> offset += o.offset();
				case MemoryIndex _ -> compact.add(element);
				default -> {
					if (offset > 0L) compact.add(new MemoryOffset(offset));
					offset = 0L;
					compact.add(element);
				}
			}
		}
		if (offset > 0L) compact.add(new MemoryOffset(offset));
		return Immutable.wrap(compact);
	}
}
