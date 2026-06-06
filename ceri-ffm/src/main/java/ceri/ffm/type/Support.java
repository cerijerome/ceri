package ceri.ffm.type;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.util.Objects;
import ceri.common.array.RawArray;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.text.ToString;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;
import ceri.ffm.core.Segments.Encoder;
import ceri.ffm.reflect.Refine;

/**
 * Operational support for types and arrays with fixed-size layouts.
 */
public abstract class Support<T, A, L extends MemoryLayout> implements Layouts.Provider<L> {
	public static final OfVoid VOID = new OfVoid(Layouts.BYTE);
	private final L layout;

	/**
	 * Adapts an instance for arrays of the support type.
	 */
	public static class OfArray<T> extends Typed<T, SequenceLayout> {
		private final Config<T> config;
		private final Class<T> type;

		/**
		 * Support array configuration; size includes the nul-terminator if specified.
		 */
		private record Config<T>(Support<?, T, ?> support, int size, boolean nul) {
			/**
			 * Returns a new sequence layout for support type arrays.
			 */
			public SequenceLayout layout() {
				return MemoryLayout.sequenceLayout(size(), support().layout());
			}

			/**
			 * Modifies alignment and byte order, returning a new config only if changed.
			 */
			public Config<T> with(long align, ByteOrder order) {
				align = Layouts.elementAlign(support().layout(), align);
				var support = support().with(align, order);
				if (support == support()) return this;
				return new Config<>(support, size(), nul());
			}

			/**
			 * Returns an initialized array; empty if nul-termination is configured.
			 */
			public T val() {
				return support().initArray(nul() ? 0 : count());
			}

			/**
			 * Returns the maximum array element count, not including nul-terminator.
			 */
			public int count() {
				return Math.max(0, size() - (nul() ? 1 : 0));
			}

			@Override
			public String toString() {
				return Native.wrap(support().typeDesc()) + "[" + size() + (nul() ? "\\0" : "")
					+ "]";
			}
		}

		private OfArray(Config<T> config, SequenceLayout layout) {
			super(layout);
			this.config = config;
			type = config.support().arrayType();
		}

		@Override
		public boolean immutable() {
			return false;
		}

		@Override
		public boolean partial() {
			return config.nul(); // or true?
		}

		@Override
		public Class<T> type() {
			return type;
		}

		@Override
		public T val() {
			return config.val();
		}

		@Override
		public T init(T value) {
			return config.support.initArray(value);
		}
		
		@Override
		public OfArray<T> with(long align, ByteOrder order) {
			var config = this.config.with(align, order);
			var layout = config == this.config ? layout() : config.layout();
			layout = Layouts.align(layout, align);
			if (config == this.config && layout == layout()) return this;
			return new OfArray<>(config, layout);
		}

		@Override
		public MemorySegment alloc(SegmentAllocator allocator, T value) {
			return config.support().allocArray(allocator, value, 0, config.count(), config.nul());
		}

		@Override
		public String typeDesc() {
			return config.toString();
		}

		@Override
		public int hashCode() {
			return Objects.hash(config, layout().byteAlignment());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			return (obj instanceof OfArray d) && Objects.equals(config, d.config)
				&& layout().byteAlignment() == d.layout().byteAlignment();
		}

		@Override
		T rawGet(MemorySegment memory, long offset, long length) {
			return config.support().getArray(memory, offset, length, config.nul());
		}

		@Override
		void rawRead(MemorySegment memory, long offset, long length, T value) {
			config.support().readArray(memory, offset, length, value, 0, config.count(),
				config.nul());
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, T value) {
			config.support().writeArray(memory, offset, length, value, 0, config.count(),
				config.nul());
		}

		@Override
		void encode(Encoder encoder, T value) {
			int count = Maths.min(RawArray.length(value), config.count());
			config.support().encodeArray(encoder, value, 0, count, config.nul());
		}
	}

	/**
	 * A void implementation.
	 */
	public static class OfVoid extends Typed<Void, ValueLayout.OfByte> {
		private OfVoid(ValueLayout.OfByte layout) {
			super(layout);
		}

		@Override
		public Class<Void> type() {
			return Void.class;
		}

		@Override
		public String typeDesc() {
			return "void";
		}

		@Override
		public OfVoid with(long align, ByteOrder order) {
			return Layouts.with(this, OfVoid::new, layout(), null, align, order);
		}

		@Override
		public Void val() {
			return null;
		}

		@Override
		Void rawGet(MemorySegment memory, long offset, long length) {
			return val();
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, Void value) {}
	}

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
		public abstract Typed<T, L> with(long align, ByteOrder order);

		/**
		 * Provides support for pointers of this type.
		 */
		public RawPointer.Supporter<Pointer<T>> asPointer(boolean constant) {
			return Pointer.support(this, constant);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		@SafeVarargs
		public final MemorySegment allocAll(boolean nul, T... array) {
			return allocAll(Segments.auto(), nul, array);
		}

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
			return writeArray(memory, offset, length, array, 0, Integer.MAX_VALUE, nul);
		}

		/**
		 * Encodes the array to allocated memory with optional nul-termination, and no padding.
		 */
		@SafeVarargs
		public final MemorySegment encodeAll(boolean nul, T... array) {
			return encodeAll(Segments.auto(), nul, array);
		}

		/**
		 * Encodes the array to allocated memory with optional nul-termination, and no padding.
		 */
		@SafeVarargs
		public final MemorySegment encodeAll(SegmentAllocator allocator, boolean nul, T... array) {
			return encodeArray(allocator, array, nul);
		}
	}

	/**
	 * Applies name and alignment, byte order from context.
	 */
	public static <T extends Support<?, ?, ?>> T with(T support, Refine.Context context) {
		if (support == null) return null;
		return Reflect.unchecked(support.with(context == null ? 0L : context.align(),
			context == null ? null : context.order()));
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
	 * Returns true if the type can be read from a partial layout.
	 */
	public boolean partial() {
		return false;
	}

	/**
	 * Returns the class type.
	 */
	public abstract Class<?> type();

	/**
	 * Returns a simple descriptor for the type.
	 */
	public String typeDesc() {
		return Reflect.simple(type());
	}

	/**
	 * Returns the array class type.
	 */
	public Class<A> arrayType() {
		return RawArray.arrayType(type());
	}

	@Override
	public L layout() {
		return layout;
	}

	@Override
	public long count(long size) {
		return Layouts.Provider.super.count(partial() ? size + layoutSize() - 1 : size);
	}

	/**
	 * Returns true if this is a void placeholder.
	 */
	public boolean isVoid() {
		return VOID.equals(this);
	}

	/**
	 * Returns an instance with modified layout.
	 */
	public abstract Support<T, A, L> with(long align, ByteOrder order);

	/**
	 * Returns operational support for arrays of this type.
	 */
	public OfArray<A> asArray(int size) {
		return asArray(size, false);
	}

	/**
	 * Returns operational support for arrays of this type, with optional nul-termination.
	 */
	public OfArray<A> asArray(int size, boolean nul) {
		var config = new OfArray.Config<>(this, Math.max(0, size), nul);
		return new OfArray<>(config, config.layout());
	}

	/**
	 * Returns a default value for the type. Sub-fields are not initialized.
	 */
	public abstract T val();

	/**
	 * Initializes a default instance and any sub-fields.
	 */
	public T init() {
		return init(null);
	}

	/**
	 * Initializes the given value and any sub-fields. Returns a default value if null.
	 */
	public T init(T value) {
		return value != null ? value : val();
	}

	/**
	 * Allocates memory for a value.
	 */
	public MemorySegment alloc() {
		return alloc(Segments.auto());
	}

	/**
	 * Allocates memory and writes the value to the memory.
	 */
	public MemorySegment alloc(T value) {
		return alloc(Segments.auto(), value);
	}

	/**
	 * Allocates memory for a value.
	 */
	public MemorySegment alloc(SegmentAllocator allocator) {
		if (allocator == null) return null;
		return allocator.allocate(layout());
	}

	/**
	 * Allocates memory and writes the value to the memory.
	 */
	public MemorySegment alloc(SegmentAllocator allocator, T value) {
		var memory = alloc(allocator);
		if (memory != null && value != null) rawWrite(memory, 0L, layoutSize(), value);
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
		if (Segments.isNull(memory)) return null;
		offset = offset(memory, offset);
		length = length(memory, offset, length);
		return count(length) < 1 ? init() : rawGet(memory, offset, length);
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
		if (immutable() || memory == null || value == null) return false;
		offset = offset(memory, offset);
		length = length(memory, offset, length);
		if (count(length) < 1) return false;
		rawRead(memory, offset, length, value);
		return true;
	}

	/**
	 * Writes the value to memory and returns true. Returns false if the memory segment is smaller
	 * than the layout.
	 */
	public boolean write(MemorySegment memory, T value) {
		return write(memory, 0L, value);
	}

	/**
	 * Writes the value to memory and returns true. Returns false if the memory segment is smaller
	 * than the layout.
	 */
	public boolean write(MemorySegment memory, long offset, T value) {
		return write(memory, offset, Long.MAX_VALUE, value);
	}

	/**
	 * Writes the value to memory and returns true. Returns false if the memory segment is smaller
	 * than the layout.
	 */
	public boolean write(MemorySegment memory, long offset, long length, T value) {
		if (value == null || memory == null) return false;
		offset = offset(memory, offset);
		length = length(memory, offset, length);
		if (count(length) < 1) return false;
		rawWrite(memory, offset, length, value);
		return true;
	}

	/**
	 * Creates an empty array instance.
	 */
	public A valArray(int count) {
		return RawArray.ofType(type(), count);
	}

	/**
	 * Creates an array instance populated with default values.
	 */
	public A initArray(int count) {
		return initArray(valArray(count));
	}

	/**
	 * Initializes array values, and replaces nulls with default values.
	 */
	public A initArray(A array) {
		if (array == null) return array;
		int length = RawArray.length(array);
		for (int i = 0; i < length; i++) {
			var value = RawArray.get(array, i);
			if (value == null) RawArray.set(array, i, init());
		}
		return array;
	}

	/**
	 * Initializes the array if not null, otherwise returns a new initialized array of given size.
	 */
	public A initArray(A array, int count) {
		return array == null ? initArray(count) : initArray(array);
	}

	/**
	 * Allocates memory and copies the values with optional nul-termination.
	 */
	public MemorySegment allocArray(A array, boolean nul) {
		return allocArray(array, 0, nul);
	}

	/**
	 * Allocates memory and copies the values with optional nul-termination.
	 */
	public MemorySegment allocArray(A array, int index, boolean nul) {
		return allocArray(array, index, Integer.MAX_VALUE, nul);
	}

	/**
	 * Allocates memory for an array of given size.
	 */
	public MemorySegment allocArray(int count) {
		return allocArray(Segments.auto(), count);
	}

	/**
	 * Allocates memory and copies the values with optional nul-termination.
	 */
	public MemorySegment allocArray(A array, int index, int count, boolean nul) {
		return allocArray(Segments.auto(), array, index, count, nul);
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
		return allocate(allocator, size(count));
	}

	/**
	 * Allocates memory and copies the values with optional nul-termination.
	 */
	public MemorySegment allocArray(SegmentAllocator allocator, A array, int index, int count,
		boolean nul) {
		if (allocator == null || array == null) return null;
		index = Maths.limit(index, 0, RawArray.length(array));
		count = Maths.limit(count, 0, RawArray.length(array) - index);
		var memory = allocate(allocator, size(count + (nul ? 1 : 0)));
		rawWriteArray(memory, 0L, array, 0, count);
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
		if (memory == null) return valArray(0);
		if (nul) return getArray(slice(memory, offset, length, nul), false);
		offset = offset(memory, offset);
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		int count = countInt(length);
		var array = valArray(count);
		rawReadArray(memory, offset, array, 0, count);
		return array;
	}

	/**
	 * Reads array and returns the updated array, or gets a new array if null.
	 */
	public A updateArray(MemorySegment memory, A array, boolean nul) {
		return updateArray(memory, 0L, array, nul);
	}

	/**
	 * Reads array and returns the updated array, or gets a new array if null.
	 */
	public A updateArray(MemorySegment memory, long offset, A array, boolean nul) {
		return updateArray(memory, offset, Long.MAX_VALUE, array, nul);
	}

	/**
	 * Reads array and returns the updated array, or gets a new array if null.
	 */
	public A updateArray(MemorySegment memory, long offset, long length, A array, boolean nul) {
		if (array == null) return getArray(memory, offset, length, nul);
		readArray(memory, offset, length, array, 0, Integer.MAX_VALUE, nul);
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
		if (array == null || Segments.isNull(memory)) return 0;
		index = Maths.limit(index, 0, RawArray.length(array));
		count = Maths.limit(count, 0, RawArray.length(array) - index);
		if (nul) return readArray(slice(memory, offset, Math.min(length, size(count, nul)), nul),
			0L, Long.MAX_VALUE, array, index, count, false);
		offset = offset(memory, offset);
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		count = (int) Math.min(count, count(length));
		if (count > 0) rawReadArray(memory, offset, array, index, count);
		return count;
	}

	/**
	 * Copies values from the array to memory with optional nul-termination, and within bounds.
	 * Returns the number of values copied, including nul-terminator.
	 */
	public int writeArray(MemorySegment memory, A array, boolean nul) {
		return writeArray(memory, 0L, array, 0, nul);
	}

	/**
	 * Copies values from the array to memory with optional nul-termination, and within bounds.
	 * Returns the number of values copied, including nul-terminator.
	 */
	public int writeArray(MemorySegment memory, long offset, A array, int index, boolean nul) {
		return writeArray(memory, offset, Long.MAX_VALUE, array, index, Integer.MAX_VALUE, nul);
	}

	/**
	 * Copies values from the array to memory with optional nul-termination, and within bounds. The
	 * count does not include the nul-terminator if specified. Returns the number of values copied,
	 * including nul-terminator.
	 */
	public int writeArray(MemorySegment memory, long offset, long length, A array, int index,
		int count, boolean nul) {
		if (array == null || Segments.isNull(memory)) return 0;
		index = Maths.limit(index, 0, RawArray.length(array));
		count = Maths.limit(count, 0, RawArray.length(array) - index) + (nul ? 1 : 0);
		offset = offset(memory, offset);
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		count = (int) Math.min(count, count(length));
		if (!nul && count > 0) rawWriteArray(memory, offset, array, index, count);
		if (nul && count > 1) rawWriteArray(memory, offset, array, index, count - 1);
		if (nul && count > 0) term().set(memory, offset + size(count - 1));
		return count;
	}

	/**
	 * Encodes the array to allocated memory without padding.
	 */
	public MemorySegment encode(T value) {
		return encode(Segments.auto(), value);
	}

	/**
	 * Encodes the array to allocated memory without padding.
	 */
	public MemorySegment encode(SegmentAllocator allocator, T value) {
		var encoder = Segments.encoder(layout.byteAlignment());
		encode(encoder, value);
		return encoder.alloc(allocator);
	}

	/**
	 * Encodes the array to allocated memory with optional nul-termination, and no padding.
	 */
	public MemorySegment encodeArray(A array, boolean nul) {
		return encodeArray(array, 0, nul);
	}

	/**
	 * Encodes the array to allocated memory with optional nul-termination, and no padding.
	 */
	public MemorySegment encodeArray(A array, int index, boolean nul) {
		return encodeArray(array, index, Integer.MAX_VALUE, nul);
	}

	/**
	 * Encodes the array to allocated memory with optional nul-termination, and no padding. The
	 * count does not include the nul-terminator if specified.
	 */
	public MemorySegment encodeArray(A array, int index, int count, boolean nul) {
		return encodeArray(Segments.auto(), array, index, count, nul);
	}

	/**
	 * Encodes the array to allocated memory with optional nul-termination, and no padding.
	 */
	public MemorySegment encodeArray(SegmentAllocator allocator, A array, boolean nul) {
		return encodeArray(allocator, array, 0, nul);
	}

	/**
	 * Encodes the array to allocated memory with optional nul-termination, and no padding.
	 */
	public MemorySegment encodeArray(SegmentAllocator allocator, A array, int index, boolean nul) {
		return encodeArray(allocator, array, index, Integer.MAX_VALUE, nul);
	}

	/**
	 * Encodes the array to allocated memory with optional nul-termination, and no padding. The
	 * count does not include the nul-terminator if specified.
	 */
	public MemorySegment encodeArray(SegmentAllocator allocator, A array, int index, int count,
		boolean nul) {
		if (array == null) return null;
		var encoder = Segments.encoder(layout.byteAlignment());
		index = index(array, index);
		count = count(array, index, count);
		encodeArray(encoder, array, index, count, nul);
		return encoder.alloc(allocator);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type(), layout());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return (obj instanceof Support s) && equalTo(s);
	}

	@Override
	public String toString() {
		return ToString.forName("$", typeDesc(), Layouts.desc(layout()));
	}

	// overrides

	boolean equalTo(Support<?, ?, ?> support) {
		return type().equals(support.type()) && layout().equals(support.layout());
	}

	/**
	 * Creates a new value from memory without performing bound checks.
	 */
	abstract T rawGet(MemorySegment memory, long offset, long length);

	/**
	 * Copies memory to the value without performing bound checks. Returns false if immutable.
	 */
	@SuppressWarnings("unused")
	void rawRead(MemorySegment memory, long offset, long length, T value) {
		// does nothing by default
	}

	/**
	 * Writes the value to memory without performing bound checks.
	 */
	abstract void rawWrite(MemorySegment memory, long offset, long length, T value);

	/**
	 * Copies memory to array without performing bound checks.
	 */
	void rawReadArray(MemorySegment memory, long offset, A array, int index, int count) {
		if (immutable()) rawReadArrayImmutable(memory, offset, array, index, count);
		else for (int i = 0; i < count; i++) {
			var value = RawArray.<T>get(array, index);
			long length = length(memory, offset);
			if (value == null) RawArray.set(array, index, rawGet(memory, offset, length));
			else rawRead(memory, offset, length, value);
			offset += layoutSize();
			index++;
		}
	}

	/**
	 * Copies memory to a new array, filling remainder with null-values.
	 */
	int rawReadArrayNew(MemorySegment memory, long offset, A array) {
		int n = readArray(memory, offset, array, 0, false);
		int count = RawArray.length(array);
		for (int i = n; i < count; i++)
			RawArray.set(array, i, init());
		return n;
	}

	/**
	 * Copies array to memory without performing bounds checks.
	 */
	void rawWriteArray(MemorySegment memory, long offset, A array, int index, int count) {
		for (int i = 0; i < count; i++) {
			T t = RawArray.get(array, index++);
			if (t != null) rawWrite(memory, offset, length(memory, offset), t);
			offset += layoutSize();
		}
	}

	/**
	 * Provides sequential encoding for the type without padding.
	 */
	void encode(Segments.Encoder encoder, T value) {
		encoder.accept((m, o, l) -> rawWrite(m, o, l, value), layoutSize());
	}

	/**
	 * Provides sequential encoding for the array without padding, after bound checks. The count
	 * does not include the nul-terminator if specified.
	 */
	void encodeArray(Segments.Encoder encoder, A array, int index, int count, boolean nul) {
		encoder.accept((m, o, _) -> rawWriteArray(m, o, array, index, count, nul),
			size(count, nul));
	}

	/**
	 * Allocates size in bytes with layout alignment.
	 */
	MemorySegment allocate(SegmentAllocator allocator, long size) {
		if (allocator == null) return null;
		return allocator.allocate(size, layout().byteAlignment());
	}
	
	// support

	private void rawReadArrayImmutable(MemorySegment memory, long offset, A array, int index,
		int count) {
		for (int i = 0; i < count; i++) {
			long length = length(memory, offset);
			RawArray.set(array, index + i, rawGet(memory, offset, length));
			offset += layoutSize();
		}
	}

	private void rawWriteArray(MemorySegment memory, long offset, A array, int index, int count,
		boolean nul) {
		if (!nul && count > 0) rawWriteArray(memory, offset, array, index, count);
		if (nul && count > 1) rawWriteArray(memory, offset, array, index, count - 1);
		if (nul && count > 0) term().set(memory, offset + size(count - 1));
	}

	private static long offset(MemorySegment memory, long offset) {
		return Maths.limit(offset, 0L, memory.byteSize());
	}

	private long length(MemorySegment memory, long offset) {
		return Maths.min(Math.min(memory.byteSize() - offset, layoutSize()));
	}

	private long length(MemorySegment memory, long offset, long length) {
		return Maths.limit(length, 0L, length(memory, offset));
	}

	private static int index(Object array, int index) {
		return Maths.limit(index, 0, RawArray.length(array));
	}

	private static int count(Object array, int index, int count) {
		return Maths.limit(count, 0, RawArray.length(array) - index);
	}
}
