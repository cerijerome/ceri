package ceri.ffm.type;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.nio.ByteOrder;
import ceri.common.array.Array;
import ceri.common.array.Dimensions;
import ceri.common.array.RawArray;
import ceri.common.math.Maths;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.common.text.ToString;
import ceri.common.util.Counter;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Memory;

/**
 * Operational support for types and arrays with fixed-size layouts.
 */
public abstract class Support<T, A, L extends MemoryLayout> implements Layouts.Provider<L> {
	private final L layout;

	/**
	 * Support for object types.
	 */
	public static abstract class Typed<T, L extends MemoryLayout> extends Support<T, T[], L> {

		Typed(L layout) {
			super(layout);
		}

		@Override
		public abstract Class<T> type();

		@Override
		public Class<T[]> arrayType() {
			return Array.arrayType(type());
		}

		@Override
		public abstract Typed<T, L> with(String name, long align, ByteOrder order);

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		@SafeVarargs
		public final MemorySegment allocAll(SegmentAllocator allocator, boolean nul, T... array) {
			return allocArray(allocator, array, nul);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		@SafeVarargs
		public final int writeAll(MemorySegment memory, boolean nul, T... array) {
			return writeAll(memory, 0L, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		@SafeVarargs
		public final int writeAll(MemorySegment memory, long offset, boolean nul, T... array) {
			return writeAll(memory, offset, Long.MAX_VALUE, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		@SafeVarargs
		public final int writeAll(MemorySegment memory, long offset, long length, boolean nul,
			T... array) {
			return writeArray(array, 0, Integer.MAX_VALUE, memory, offset, length, nul);
		}

		@Override
		public T[] arrayVal(int count) {
			var array = array(count);
			for (int i = 0; i < count; i++)
				array[i] = val();
			return array;
		}
	}

	/**
	 * Applies name, alignment and byte order.
	 */
	public static <T extends Support<?, ?, ?>> T with(T support, String name, long align,
		ByteOrder order) {
		if (support == null) return null;
		return Reflect.unchecked(support.with(name, align, order));
	}

	Support(L layout) {
		this.layout = layout;
	}

	/**
	 * Returns true if the type is immutable.
	 */
	public boolean immutable() {
		return true;
	}

	/**
	 * Returns the array component class type.
	 */
	public abstract Class<?> type();

	/**
	 * Returns the array class type.
	 */
	public abstract Class<A> arrayType();

	@Override
	public L layout() {
		return layout;
	}

	/**
	 * Returns an instance with modified layout.
	 */
	public abstract Support<T, A, L> with(String name, long align, ByteOrder order);

	/**
	 * Returns a default value for the type.
	 */
	public abstract T val();

	/**
	 * Initializes the given value. Returns a default value if null.
	 */
	public T init(T value) {
		return value != null ? value : val();
	}

	/**
	 * Allocates memory for a value.
	 */
	public MemorySegment alloc(SegmentAllocator allocator) {
		if (allocator == null) return null;
		return allocator.allocate(size(1));
	}

	/**
	 * Allocates memory and writes the value to the memory.
	 */
	public MemorySegment alloc(SegmentAllocator allocator, T value) {
		var memory = alloc(allocator);
		if (memory != null && value != null) rawWrite(value, memory, 0L);
		return memory;
	}

	/**
	 * Creates a new value from memory. Returns null value if the memory segment is smaller than the
	 * layout.
	 */
	public T get(MemorySegment memory) {
		return get(memory, 0L);
	}

	/**
	 * Creates a new value from memory. Returns null value if the memory segment is smaller than the
	 * layout.
	 */
	public T get(MemorySegment memory, long offset) {
		return get(memory, offset, Long.MAX_VALUE);
	}

	/**
	 * Creates a new value from memory. Returns null value if the memory segment is smaller than the
	 * layout.
	 */
	public T get(MemorySegment memory, long offset, long length) {
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		return length < size(1) ? val() : rawGet(memory, offset);
	}

	/**
	 * Updates the value or returns a new value from memory.
	 */
	public T update(MemorySegment memory, T value) {
		return update(memory, 0L, value);
	}

	/**
	 * Updates the value or returns a new value from memory.
	 */
	public T update(MemorySegment memory, long offset, T value) {
		return update(memory, offset, Long.MAX_VALUE, value);
	}

	/**
	 * Updates the value or returns a new value from memory.
	 */
	public T update(MemorySegment memory, long offset, long length, T value) {
		if (immutable() || value == null) return get(memory, offset, length);
		value = val();
		read(memory, offset, length, value);
		return value;
	}

	/**
	 * Updates the value from memory. Returns false if unable to read.
	 */
	public boolean read(MemorySegment memory, T value) {
		return read(memory, 0L, value);
	}

	/**
	 * Updates the value from memory. Returns false if unable to read.
	 */
	public boolean read(MemorySegment memory, long offset, T value) {
		return read(memory, offset, Long.MAX_VALUE, value);
	}

	/**
	 * Updates the value from memory. Returns false if unable to read.
	 */
	public boolean read(MemorySegment memory, long offset, long length, T value) {
		if (immutable()) return false;
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		if (length < size(1)) return false;
		rawRead(memory, offset, value);
		return true;
	}

	/**
	 * Writes the value to memory and returns true. Returns false if the memory segment is smaller
	 * than the layout.
	 */
	public boolean write(T value, MemorySegment memory) {
		return write(value, memory, 0L);
	}

	/**
	 * Writes the value to memory and returns true. Returns false if the memory segment is smaller
	 * than the layout.
	 */
	public boolean write(T value, MemorySegment memory, long offset) {
		return write(value, memory, offset, Long.MAX_VALUE);
	}

	/**
	 * Writes the value to memory and returns true. Returns false if the memory segment is smaller
	 * than the layout.
	 */
	public boolean write(T value, MemorySegment memory, long offset, long length) {
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		if (length < size(1)) return false;
		rawWrite(value, memory, offset);
		return true;
	}

	/**
	 * Creates an empty array instance.
	 */
	public A array(int count) {
		return RawArray.ofType(type(), count);
	}

	/**
	 * Creates an array instance populated with default values.
	 */
	public A arrayVal(int count) {
		return arrayInit(array(count));
	}

	/**
	 * Initializes array values, and replaces nulls with default values.
	 */
	public A arrayInit(A array) {
		deepInit(array);
		return array;
	}

	/**
	 * Allocates memory and copies the values with optional nul-termination.
	 */
	public MemorySegment allocArray(SegmentAllocator allocator, A array, boolean nul) {
		return allocArray(allocator, array, 0, nul);
	}

	/**
	 * Allocates memory and copies the values with optional nul-termination.
	 */
	public MemorySegment allocArray(SegmentAllocator allocator, A array, int index, boolean nul) {
		return allocArray(allocator, array, index, Integer.MAX_VALUE, nul);
	}

	/**
	 * Allocates memory for an array of given size.
	 */
	public MemorySegment allocArray(SegmentAllocator allocator, int count) {
		if (allocator == null) return null;
		return allocator.allocate(size(count));
	}

	/**
	 * Allocates memory and copies the values with optional nul-termination.
	 */
	public MemorySegment allocArray(SegmentAllocator allocator, A array, int index, int count,
		boolean nul) {
		if (allocator == null || array == null) return null;
		index = Maths.limit(index, 0, RawArray.length(array));
		count = Maths.limit(count, 0, RawArray.length(array) - index);
		var memory = allocator.allocate(size(count + (nul ? 1 : 0)));
		rawWriteArray(array, 0, memory, 0L, count);
		if (nul) term().set(memory, size(count));
		return memory;
	}

	/**
	 * Creates an array of values copied from memory up to optional nul-termination, and within
	 * bounds. Returns null if nul-termination is specified but not found.
	 */
	public A getArray(MemorySegment memory, boolean nul) {
		return getArray(memory, 0L, nul);
	}

	/**
	 * Creates an array of values copied from memory up to optional nul-termination, and within
	 * bounds. Returns null if nul-termination is specified but not found.
	 */
	public A getArray(MemorySegment memory, long offset, boolean nul) {
		return getArray(memory, offset, Long.MAX_VALUE, nul);
	}

	/**
	 * Creates an array of values copied from memory up to optional nul-termination, and within
	 * bounds. Returns null if nul-termination is specified but not found.
	 */
	public A getArray(MemorySegment memory, long offset, long length, boolean nul) {
		if (Memory.isNull(memory)) return null;
		if (nul) return getArray(slice(memory, offset, length, nul), false);
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		int count = countInt(length);
		var array = array(count);
		rawReadArray(memory, offset, array, 0, count);
		return array;
	}

	/**
	 * Copies values from memory up to optional nul-termination, to the array within bounds. Returns
	 * the number of values copied. Returns 0 if nul-termination is specified but not found.
	 */
	public int readArray(MemorySegment memory, A array, boolean nul) {
		return readArray(memory, 0L, array, 0, nul);
	}

	/**
	 * Copies values from memory up to optional nul-termination, to the array within bounds. Returns
	 * the number of values copied. Returns 0 if nul-termination is specified but not found.
	 */
	public int readArray(MemorySegment memory, long offset, A array, int index, boolean nul) {
		return readArray(memory, offset, Long.MAX_VALUE, array, index, Integer.MAX_VALUE, nul);
	}

	/**
	 * Copies values from memory up to optional nul-termination, to the array within bounds. Returns
	 * the number of values copied. Returns 0 if nul-termination is specified but not found.
	 */
	public int readArray(MemorySegment memory, long offset, long length, A array, int index,
		int count, boolean nul) {
		if (array == null || Memory.isNull(memory)) return 0;
		if (nul) return readArray(slice(memory, offset, length, nul), 0L, Long.MAX_VALUE, array,
			index, count, false);
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		index = Maths.limit(index, 0, RawArray.length(array));
		count = Maths.limit(count, 0, RawArray.length(array) - index);
		count = (int) Math.min(count, count(length));
		if (count > 0) rawReadArray(memory, offset, array, index, count);
		return count;
	}

	/**
	 * Copies values from the array to memory with optional nul-termination, and within bounds.
	 * Returns the number of values copied, including nul-terminator.
	 */
	public int writeArray(A array, MemorySegment memory, boolean nul) {
		return writeArray(array, 0, memory, 0L, nul);
	}

	/**
	 * Copies values from the array to memory with optional nul-termination, and within bounds.
	 * Returns the number of values copied, including nul-terminator.
	 */
	public int writeArray(A array, int index, MemorySegment memory, long offset, boolean nul) {
		return writeArray(array, index, Integer.MAX_VALUE, memory, offset, Long.MAX_VALUE, nul);
	}

	/**
	 * Copies values from the array to memory with optional nul-termination, and within bounds.
	 * Returns the number of values copied, including nul-terminator.
	 */
	public int writeArray(A array, int index, int count, MemorySegment memory, long offset,
		long length, boolean nul) {
		if (array == null || Memory.isNull(memory)) return 0;
		index = Maths.limit(index, 0, RawArray.length(array));
		count = Maths.limit(count, 0, RawArray.length(array) - index) + (nul ? 1 : 0);
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		count = (int) Math.min(count, count(length));
		if (!nul && count > 0) rawWriteArray(array, index, memory, offset, count);
		if (nul && count > 1) rawWriteArray(array, index, memory, offset, count - 1);
		if (nul && count > 0) term().set(memory, offset + size(count - 1));
		return count;
	}

	/**
	 * Calculates the memory size to store the multi-dimensional array with optional
	 * nul-terminators. Returns 0 if the array type is not supported.
	 */
	public long deepSize(Object t, boolean nul) {
		return rawDeepSize(t, dimsOf(t), nul);
	}

	/**
	 * Creates a multi-dimensional array.
	 */
	public Object deep(Dimensions dims) {
		return deep(dims.array());
	}

	/**
	 * Creates a multi-dimensional array.
	 */
	public Object deep(int... dims) {
		return RawArray.ofType(type(), dims);
	}

	/**
	 * Creates a multi-dimensional array and populates it with default values.
	 */
	public Object deepVal(Dimensions dims) {
		return deepVal(dims.array());
	}

	/**
	 * Creates a multi-dimensional array and populates it with default values.
	 */
	public Object deepVal(int... dims) {
		return deepInit(deep(dims));
	}

	/**
	 * Initializes missing multi-dimensional array leaf values with default values.
	 */
	public Object deepInit(Object t) {
		return RawArray.deepReplace(t, this::init);
	}

	/**
	 * Allocates memory for the multi-dimensional array with optional nul-termination. Returns null
	 * if the array type is not supported.
	 */
	public MemorySegment deepAllocEmpty(SegmentAllocator allocator, Object t, boolean nul) {
		int dims = dimsOf(t);
		if (allocator == null || dims < 0) return null;
		return allocator.allocate(rawDeepSize(t, dims, nul));
	}

	/**
	 * Allocates memory for the multi-dimensional array and copies the values with optional
	 * nul-termination. Returns null if the array type is not supported.
	 */
	public MemorySegment deepAlloc(SegmentAllocator allocator, Object t, boolean nul) {
		int dims = dimsOf(t);
		if (allocator == null || dims < 0) return null;
		var memory = allocator.allocate(rawDeepSize(t, dims, nul));
		rawDeepWrite(t, dims, memory, 0L, Long.MAX_VALUE, nul);
		return memory;
	}

	/**
	 * Creates a multi-dimensional array of values copied from memory up to optional
	 * nul-terminators. A zero-sized array is set if a nul-terminator is specified but not found.
	 */
	public <U> U deepGet(MemorySegment memory, Dimensions dims, boolean nul) {
		return deepGet(memory, 0L, dims, nul);
	}

	/**
	 * Creates a multi-dimensional array of values copied from memory up to optional
	 * nul-terminators. A zero-sized array is set if a nul-terminator is specified but not found.
	 */
	public <U> U deepGet(MemorySegment memory, long offset, Dimensions dims, boolean nul) {
		return deepGet(memory, offset, Long.MAX_VALUE, dims, nul);
	}

	/**
	 * Creates a multi-dimensional array of values copied from memory up to optional
	 * nul-terminators. A zero-sized array is set if a nul-terminator is specified but not found.
	 */
	public <U> U deepGet(MemorySegment memory, long offset, long length, Dimensions dims,
		boolean nul) {
		if (Memory.isNull(memory)) return null;
		if (dims == null) dims = Dimensions.NONE;
		return Reflect.unchecked(rawDeepGet(memory, offset, length, dims, nul));
	}

	/**
	 * Copies multi-dimensional array values from memory up to optional nul-terminators. Returns the
	 * number of values copied. Returns 0 if the array type is not supported.
	 */
	public int deepRead(MemorySegment memory, Object t, boolean nul) {
		return deepRead(memory, 0L, t, nul);
	}

	/**
	 * Copies multi-dimensional array values from memory up to optional nul-terminators. Returns the
	 * number of values copied. Returns 0 if the array type is not supported.
	 */
	public int deepRead(MemorySegment memory, long offset, Object t, boolean nul) {
		return deepRead(memory, offset, Long.MAX_VALUE, t, nul);
	}

	/**
	 * Copies multi-dimensional array values from memory up to optional nul-terminators. Returns the
	 * number of values copied. Returns 0 if the array type is not supported.
	 */
	public int deepRead(MemorySegment memory, long offset, long length, Object t, boolean nul) {
		if (Memory.isNull(memory)) return 0;
		return rawDeepRead(memory, offset, length, t, dimsOf(t), nul);
	}

	/**
	 * Copies values from the multi-dimensional array to memory with optional nul-termination.
	 * Returns the number of values copied, including nul-terminators. Returns 0 if the array type
	 * is not supported.
	 */
	public int deepWrite(Object t, MemorySegment memory, boolean nul) {
		return deepWrite(t, memory, 0L, nul);
	}

	/**
	 * Copies values from the multi-dimensional array to memory with optional nul-termination.
	 * Returns the number of values copied, including nul-terminators. Returns 0 if the array type
	 * is not supported.
	 */
	public int deepWrite(Object t, MemorySegment memory, long offset, boolean nul) {
		return deepWrite(t, memory, offset, Long.MAX_VALUE, nul);
	}

	/**
	 * Copies values from the multi-dimensional array to memory with optional nul-termination.
	 * Returns the number of values copied, including nul-terminators. Returns 0 if the array type
	 * is not supported.
	 */
	public int deepWrite(Object t, MemorySegment memory, long offset, long length, boolean nul) {
		if (Memory.isNull(memory)) return 0;
		return rawDeepWrite(t, dimsOf(t), memory, offset, length, nul);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, Reflect.simple(type()), layout(), term());
	}

	// overrides

	/**
	 * Creates a new value from memory without performing bound checks.
	 */
	abstract T rawGet(MemorySegment memory, long offset);

	/**
	 * Copies memory to the value without performing bound checks. Returns false if immutable.
	 */
	@SuppressWarnings("unused")
	void rawRead(MemorySegment memory, long offset, T value) {
		// does nothing by default
	}

	/**
	 * Writes the value to memory without performing bound checks.
	 */
	abstract void rawWrite(T value, MemorySegment memory, long offset);

	/**
	 * Copies memory to array without performing bound checks.
	 */
	void rawReadArray(MemorySegment memory, long offset, A array, int index, int count) {
		if (immutable()) rawReadArrayImmutable(memory, offset, array, index, count);
		else for (int i = 0; i < count; i++) {
			var value = RawArray.<T>get(array, index);
			if (value == null) RawArray.set(array, index, rawGet(memory, offset));
			else rawRead(memory, offset, value);
			offset += size(1);
			index++;
		}
	}

	/**
	 * Copies memory to array without performing bound checks.
	 */
	void rawReadArrayImmutable(MemorySegment memory, long offset, A array, int index, int count) {
		for (int i = 0; i < count; i++) {
			RawArray.set(array, index + i, rawGet(memory, offset));
			offset += size(1);
		}
	}

	/**
	 * Copies memory to a new array, filling remainder with null-values.
	 */
	int rawReadArrayNew(MemorySegment memory, long offset, A array) {
		int n = readArray(memory, offset, array, 0, false);
		int count = RawArray.length(array);
		for (int i = n; i < count; i++)
			RawArray.set(array, i, val());
		return n;
	}

	/**
	 * Copies array to memory without performing bounds checks.
	 */
	void rawWriteArray(A array, int index, MemorySegment memory, long offset, int count) {
		for (int i = 0; i < count; i++) {
			T t = RawArray.get(array, index++);
			if (t != null) rawWrite(t, memory, offset);
			offset += size(1);
		}
	}

	/**
	 * Returns the array dimension count of the object; 0 if not an array, -1 if not the supported
	 * type.
	 */
	int dimsOf(Object t) {
		var typed = Generics.Typed.from(t).array();
		return type().equals(typed.cls()) ? typed.dimensions() : -1;
	}

	// support

	private long rawDeepSize(Object t, int dims, boolean nul) {
		if (dims < 0) return 0;
		if (dims == 0) return size(nul ? 2 : 1);
		return MultiArray.iterateTwigs(t, null, 0L, 0L,
			(a, _, _) -> size(RawArray.length(a) + (nul ? 1 : 0)));
	}

	private Object rawDeepGet(MemorySegment memory, long offset, long length, Dimensions dims,
		boolean nul) {
		if (dims.count() == 0) return get(slice(memory, offset, length, nul), 0L);
		if (dims.count() == 1) return getArray(memory, offset, length, nul);
		memory = slice(memory, offset, length, false);
		return nul ? rawDeepGetNul(memory, dims) : rawDeepGetFixed(memory, dims);
	}

	private Object rawDeepGetNul(MemorySegment memory, Dimensions dims) {
		var t = MultiArray.twigStubs(type(), dims);
		var maxSize = size(dims.dim(-1));
		var offset = Counter.of(0L);
		MultiArray.replaceTwigs(t, _ -> {
			var slice = slice(memory, offset.get(), maxSize, true);
			if (slice == null) offset.set(memory.byteSize()); // no more slices
			else offset.inc(slice.byteSize() + size(1));
			return getArray(slice, false);
		});
		return t;
	}

	private Object rawDeepGetFixed(MemorySegment memory, Dimensions dims) {
		var t = deep(dims);
		MultiArray.iterateTwigs(t, memory,
			(a, m, o) -> size(rawReadArrayNew(m, o, Reflect.unchecked(a))));
		return t;
	}

	private int rawDeepRead(MemorySegment memory, long offset, long length, Object t, int dims,
		boolean nul) {
		if (dims <= 0) return 0; // can only copy to arrays
		long total = MultiArray.iterateTwigs(t, memory, offset, length,
			(a, m, o) -> size(readArray(m, o, Reflect.unchecked(a), 0, nul)));
		return countInt(total);
	}

	private int rawDeepWrite(Object t, int dims, MemorySegment memory, long offset, long length,
		boolean nul) {
		if (dims < 0) return 0;
		if (dims == 0) return writeArray(singletonArray(t), 0, 1, memory, offset, length, nul);
		long total = MultiArray.iterateTwigs(t, memory, offset, length,
			(a, m, o) -> size(writeArray(Reflect.unchecked(a), 0, m, o, nul)));
		return countInt(total);
	}

	private A singletonArray(Object value) {
		var array = RawArray.<A>ofType(type(), 1);
		RawArray.set(array, 0, value);
		return array;
	}
}
