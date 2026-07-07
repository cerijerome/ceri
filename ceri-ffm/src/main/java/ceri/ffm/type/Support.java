package ceri.ffm.type;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.util.Objects;
import ceri.common.array.DynamicArray;
import ceri.common.array.RawArray;
import ceri.common.io.Direction;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.text.ToString;
import ceri.ffm.core.Decoder;
import ceri.ffm.core.Encoder;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;
import ceri.ffm.test.FfmTesting;

/**
 * Operational support for types and arrays with fixed-size layouts.
 */
public abstract class Support<T, A, L extends MemoryLayout> implements Layouts.Provider<L> {
	public static final OfVoid VOID = new OfVoid(Layouts.BYTE);
	private final L layout;
	private static final String t = "a";
	private static final String tt = "bb";
	private static final String ttt = "ccc";

	public static void main(String[] args) {
		var F = StringType.UTF8.support(3, false);
		var N = StringType.UTF8.support(4, true);
		var FF = F.asArray(2);
		var FN = N.asArray(2);
		var NF = F.asArray(3, true);
		var NN = N.asArray(3, true);
		var FFF = FF.asArray(3);
		var FFN = FN.asArray(3);
		var FNF = NF.asArray(3);
		var FNN = NN.asArray(3);
		var NFF = FF.asArray(4, true);
		var NFN = FN.asArray(4, true);
		var NNF = NF.asArray(4, true);
		var NNN = NN.asArray(4, true);
		// FFF: |ttt|ttt||ttt|ttt||ttt|ttt| <=> {{ttt,ttt},{ttt,ttt},{ttt,ttt}}
		encode(FFF, new String[][] { { ttt, ttt }, { ttt, ttt }, { ttt, ttt } });
		// FFN: |tttn|tn||n|ttn||ttn|tttn| <=> {{ttt,t},{,tt},{tt,ttt}}
		encode(FFN, new String[][] { { ttt, t }, { "", tt }, { tt, ttt } });
		// FNF: |ttt|ttt|nnn||ttt|nnn||nnn| <=> {{ttt,ttt},{ttt},{}}
		encode(FNF, new String[][] { { ttt, ttt }, { ttt }, {} });
		// NFF: |ttt|ttt||ttt|ttt||nnnnnn| <=> {{ttt,ttt},{ttt,ttt}}
		encode(NFF, new String[][] { { ttt, ttt }, { ttt, ttt } });
		// FNN: |tttn|tn|n||ttn|n||tn|n| <=> {{ttt,t},{tt},{t}}
		encode(FNN, new String[][] { { ttt, t }, { tt }, { t } });
		// NNF: |ttt|ttt|nnn||ttt|nnn||nnn| <=> {{ttt,ttt},{ttt}}
		encode(NNF, new String[][] { { ttt, ttt }, { ttt } });
		// NFN: |tttn|tn||ttn|n||n|n| <=> {{ttt,t},{tt,}}
		encode(NFN, new String[][] { { ttt, t }, { tt, "" } });
		// NNN: |tttn|tn|n||tn|n||ttn|n||n| <=> {{ttt,t},{t},{tt}}
		encode(NNN, new String[][] { { ttt, t }, { t }, { tt } });
	}

	private static <T> MemorySegment encode(OfArray<T> support, T value) {
		var r = support.encode(Direction.in, value);
		System.out.println(support);
		FfmTesting.bin(r.value());
		System.out.println("Encode: " + RawArray.toString(value));
		var decoded = support.decode(r.value());
		System.out.println("Decode: " + RawArray.toString(decoded));
		System.out.println();
		return r.value();
	}

	/**
	 * Adapts an instance for arrays of the support type.
	 */
	public static class OfArray<T> extends Typed<T, SequenceLayout> {
		private final Config<T> config;
		private final Class<T> type;

		/**
		 * Support array configuration; length includes the nul-terminator if specified.
		 */
		private record Config<T>(Support<?, T, ?> support, int count, boolean nul) {
			/**
			 * Returns a new sequence layout for support type arrays.
			 */
			public SequenceLayout layout() {
				return MemoryLayout.sequenceLayout(count(), support().layout());
			}

			/**
			 * Modifies alignment, returning a new config only if changed.
			 */
			public Config<T> align(long align) {
				align = Layouts.elementAlign(support().layout(), align);
				var support = support().align(align);
				return support == support() ? this : new Config<>(support, count(), nul());
			}

			/**
			 * Modifies alignment and byte order, returning a new config only if changed.
			 */
			public Config<T> order(ByteOrder order) {
				var support = support().order(order);
				return support == support() ? this : new Config<>(support, count(), nul());
			}

			/**
			 * Returns an initialized array; empty if nul-termination is configured.
			 */
			public T val() {
				return support().initArray(nul() ? 0 : count());
			}

			@Override
			public String toString() {
				return arrayDesc(Native.wrap(support().typeDesc()), count(), nul());
			}
		}

		private OfArray(Config<T> config, SequenceLayout layout) {
			super(layout);
			this.config = config;
			type = elementSupport().arrayType();
		}

		@Override
		public Native.Kind kind() {
			return elementSupport().kind();
		}

		/**
		 * Returns the maximum element count, including nul-terminator if specified.
		 */
		public int count() {
			return config.count();
		}

		/**
		 * Returns the maximum element count, without nul-terminator.
		 */
		public int elements() {
			return nul() ? Math.max(0, count() - 1) : count();
		}

		/**
		 * Returns the nul-termination directive.
		 */
		public boolean nul() {
			return config.nul();
		}

		@Override
		public boolean isArray() {
			return true;
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
			return elementSupport().initArray(value);
		}

		@Override
		public OfArray<T> align(long align) {
			return create(config.align(align), align);
		}

		@Override
		public OfArray<T> order(ByteOrder order) {
			return create(config.order(order), layout().byteAlignment());
		}

		@Override
		public MemorySegment alloc(SegmentAllocator allocator, T value) {
			return elementSupport().allocArray(allocator, value, 0, count(), nul());
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
		protected T rawGet(MemorySegment memory, long offset, long length) {
			return elementSupport().getArray(memory, offset, length, nul());
		}

		@Override
		protected void rawRead(MemorySegment memory, long offset, long length, T value) {
			elementSupport().readArray(memory, offset, length, value, 0, count(), nul());
		}

		@Override
		protected void rawWrite(MemorySegment memory, long offset, long length, T value) {
			elementSupport().writeArray(memory, offset, length, value, 0, count(), nul());
		}

		@Override
		protected void encode(Encoder encoder, T value) {
			int count = Maths.min(RawArray.length(value), elements());
			elementSupport().encodeArray(encoder, value, 0, count, nul());
		}

		@Override
		protected T decode(Decoder decoder, long length) {
			return elementSupport().decodeArray(decoder, length, count(), nul());
		}

		@Override
		protected void encodeArray(Encoder encoder, T[] array, int index, int count, boolean nul) {
			encodeDynamicArray(encoder, array, index, count, nul);
		}

		@Override
		protected T[] decodeArray(Decoder decoder, long length, int count, boolean nul) {
			return decodeDynamicArray(decoder, length, count, nul);
		}

		@Override
		protected int encodeTermSize() {
			return elementSupport().encodeTermSize() * (nul() ? 1 : count());
		}

		private OfArray<T> create(Config<T> config, long align) {
			var layout = config == this.config ? layout() : config.layout();
			layout = Layouts.align(layout, align);
			if (config == this.config && layout == layout()) return this;
			return new OfArray<>(config, layout);
		}

		private Support<?, T, ?> elementSupport() {
			return config.support();
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
		public Native.Kind kind() {
			return Native.Kind.none;
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
		public OfVoid align(long align) {
			var layout = Layouts.align(layout(), align);
			return layout == layout() ? this : new OfVoid(layout);
		}

		@Override
		public OfVoid order(ByteOrder order) {
			var layout = Layouts.order(layout(), order);
			return layout == layout() ? this : new OfVoid(layout);
		}

		@Override
		public Void val() {
			return null;
		}

		@Override
		protected Void rawGet(MemorySegment memory, long offset, long length) {
			return val();
		}

		@Override
		protected void rawWrite(MemorySegment memory, long offset, long length, Void value) {}
	}

	/**
	 * Support for object types.
	 */
	public static abstract class Typed<T, L extends MemoryLayout> extends Support<T, T[], L> {

		protected Typed(L layout) {
			super(layout);
		}

		@Override
		public abstract Class<T> type();

		@Override
		public abstract Typed<T, L> align(long align);

		@Override
		public abstract Typed<T, L> order(ByteOrder order);

		/**
		 * Provides support for pointers of this type.
		 */
		public RawPointer.Supporter<Pointer<T>> asPointer() {
			return asPointer(false);
		}

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
		public final Native.Adapted<MemorySegment> encodeAll(Direction direction, boolean nul,
			T... array) {
			return encodeAll(direction, Segments.auto(), nul, array);
		}

		/**
		 * Encodes the array to allocated memory with optional nul-termination, and no padding.
		 */
		@SafeVarargs
		public final Native.Adapted<MemorySegment> encodeAll(Direction direction,
			SegmentAllocator allocator, boolean nul, T... array) {
			return encodeArray(direction, allocator, array, nul);
		}

		/**
		 * Encodes an array with non-fixed element sizes. Count does not include terminator.
		 */
		protected void encodeDynamicArray(Encoder encoder, T[] array, int index, int count,
			boolean nul) {
			for (int i = 0; i < count; i++)
				encode(encoder, array[index + i]);
			if (nul) encoder.acceptNul(encodeTermSize());
		}

		/**
		 * Decodes an array with non-fixed element sizes. Count includes terminator if specified.
		 */
		protected T[] decodeDynamicArray(Decoder decoder, long length, int count, boolean nul) {
			if (nul) count--;
			var termSize = nul ? encodeTermSize() : 0;
			long end = decoder.offset() + length - termSize;
			var array = DynamicArray.of(type());
			while (true) {
				if (nul && decoder.nul(termSize)) return array.truncate();
				if (decoder.offset() >= end || array.index() >= count) break;
				var value = decode(decoder, end - decoder.offset());
				if (value == null) break;
				array.accept(value);
			}
			return nul ? decodeNoVals(decoder, end + termSize - decoder.offset()) :
				array.truncate();
		}
	}

	protected Support(L layout) {
		this.layout = layout;
	}

	/**
	 * Returns the supported native kind.
	 */
	public abstract Native.Kind kind();

	/**
	 * Returns true if this is a void placeholder.
	 */
	public boolean isVoid() {
		return kind() == Native.Kind.none;
	}

	/**
	 * Returns true if this supports an array type.
	 */
	public boolean isArray() {
		return false;
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

	/**
	 * Returns an instance with modified layout.
	 */
	public abstract Support<T, A, L> align(long align);

	/**
	 * Returns an instance with modified layout.
	 */
	public abstract Support<T, A, L> order(ByteOrder order);

	@Override
	public long count(long size) {
		return Layouts.Provider.super.count(partial() ? size + layoutSize() - 1 : size);
	}

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
		offset = Maths.limit(offset, 0L, memory.byteSize());
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
		offset = Maths.limit(offset, 0L, memory.byteSize());
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
		offset = Maths.limit(offset, 0L, memory.byteSize());
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
		offset = Maths.limit(offset, 0L, memory.byteSize());
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
		offset = Maths.limit(offset, 0L, memory.byteSize());
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
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		count = (int) Math.min(count, count(length));
		if (!nul && count > 0) rawWriteArray(memory, offset, array, index, count);
		if (nul && count > 1) rawWriteArray(memory, offset, array, index, count - 1);
		if (nul && count > 0) term().set(memory, offset + size(count - 1));
		return count;
	}

	/**
	 * Encodes the value to allocated memory without padding.
	 */
	public Native.Adapted<MemorySegment> encode(Direction direction, T value) {
		return encode(direction, Segments.auto(), value);
	}

	/**
	 * Encodes the value to allocated memory without padding.
	 */
	public Native.Adapted<MemorySegment> encode(Direction direction, SegmentAllocator allocator,
		T value) {
		var encoder = Encoder.of(direction, layout().byteAlignment());
		encode(encoder, value);
		return encoder.alloc(allocator);
	}

	/**
	 * Decodes the value from memory without padding.
	 */
	public T decode(MemorySegment memory) {
		return decode(memory, 0L);
	}

	/**
	 * Decodes the value from memory without padding.
	 */
	public T decode(MemorySegment memory, long offset) {
		return decode(memory, offset, Long.MAX_VALUE);
	}

	/**
	 * Decodes the value from memory without padding.
	 */
	public T decode(MemorySegment memory, long offset, long length) {
		var decoder = Decoder.of(memory, offset, length, layout().byteAlignment());
		if (decoder == null) return null;
		return decode(decoder, decoder.length());
	}

	/**
	 * Encodes the array to allocated memory with optional nul-termination, and no padding.
	 */
	public Native.Adapted<MemorySegment> encodeArray(Direction direction, A array, boolean nul) {
		return encodeArray(direction, array, 0, nul);
	}

	/**
	 * Encodes the array to allocated memory with optional nul-termination, and no padding.
	 */
	public Native.Adapted<MemorySegment> encodeArray(Direction direction, A array, int index,
		boolean nul) {
		return encodeArray(direction, array, index, Integer.MAX_VALUE, nul);
	}

	/**
	 * Encodes the array to allocated memory with optional nul-termination, and no padding. The
	 * count does not include the nul-terminator if specified.
	 */
	public Native.Adapted<MemorySegment> encodeArray(Direction direction, A array, int index,
		int count, boolean nul) {
		return encodeArray(direction, Segments.auto(), array, index, count, nul);
	}

	/**
	 * Encodes the array to allocated memory with optional nul-termination, and no padding.
	 */
	public Native.Adapted<MemorySegment> encodeArray(Direction direction,
		SegmentAllocator allocator, A array, boolean nul) {
		return encodeArray(direction, allocator, array, 0, nul);
	}

	/**
	 * Encodes the array to allocated memory with optional nul-termination, and no padding.
	 */
	public Native.Adapted<MemorySegment> encodeArray(Direction direction,
		SegmentAllocator allocator, A array, int index, boolean nul) {
		return encodeArray(direction, allocator, array, index, Integer.MAX_VALUE, nul);
	}

	/**
	 * Encodes the array to allocated memory with optional nul-termination, and no padding. The
	 * count does not include the nul-terminator if specified.
	 */
	public Native.Adapted<MemorySegment> encodeArray(Direction direction,
		SegmentAllocator allocator, A array, int index, int count, boolean nul) {
		if (array == null) return null;
		var encoder = Encoder.of(direction, layout().byteAlignment());
		index = Maths.limit(index, 0, RawArray.length(array));
		count = Maths.limit(count, 0, RawArray.length(array) - index);
		encodeArray(encoder, array, index, count, nul);
		return encoder.alloc(allocator);
	}

	/**
	 * Decodes an array up to count elements from memory, with optional nul-termination, and no
	 * padding.
	 */
	public A decodeArray(MemorySegment memory, int count, boolean nul) {
		return decodeArray(memory, 0L, count, nul);
	}

	/**
	 * Decodes an array up to count elements from memory, with optional nul-termination, and no
	 * padding.
	 */
	public A decodeArray(MemorySegment memory, long offset, int count, boolean nul) {
		return decodeArray(memory, offset, Long.MAX_VALUE, count, nul);
	}

	/**
	 * Decodes an array up to count elements from memory, with optional nul-termination, and no
	 * padding.
	 */
	public A decodeArray(MemorySegment memory, long offset, long length, int count, boolean nul) {
		var decoder = Decoder.of(memory, offset, length, layout().byteAlignment());
		if (decoder == null) return null;
		return decodeArray(decoder, decoder.length(), count, nul);
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

	protected boolean equalTo(Support<?, ?, ?> support) {
		return type().equals(support.type()) && layout().equals(support.layout());
	}

	/**
	 * Creates a new value from memory without performing bound checks.
	 */
	protected abstract T rawGet(MemorySegment memory, long offset, long length);

	/**
	 * Copies memory to the value without performing bound checks. Returns false if immutable.
	 */
	@SuppressWarnings("unused")
	protected void rawRead(MemorySegment memory, long offset, long length, T value) {
		// does nothing by default
	}

	/**
	 * Writes the value to memory without performing bound checks.
	 */
	protected abstract void rawWrite(MemorySegment memory, long offset, long length, T value);

	/**
	 * Copies memory to array without performing bound checks.
	 */
	protected void rawReadArray(MemorySegment memory, long offset, A array, int index, int count) {
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
	protected int rawReadArrayNew(MemorySegment memory, long offset, A array) {
		int n = readArray(memory, offset, array, 0, false);
		int count = RawArray.length(array);
		for (int i = n; i < count; i++)
			RawArray.set(array, i, init());
		return n;
	}

	/**
	 * Copies array to memory without performing bounds checks.
	 */
	protected void rawWriteArray(MemorySegment memory, long offset, A array, int index, int count) {
		for (int i = 0; i < count; i++) {
			T t = RawArray.get(array, index++);
			if (t != null) rawWrite(memory, offset, length(memory, offset), t);
			offset += layoutSize();
		}
	}

	/**
	 * Provides sequential encoding for the type without padding.
	 */
	protected void encode(Encoder encoder, T value) {
		encoder.accept(encoder.in() ? (m, o, l) -> write(m, o, l, value) : null,
			encoder.out() && !immutable() ? (m, o, l) -> read(m, o, l, value) : null, layoutSize());
	}

	/**
	 * Provides sequential decoding for the type without padding.
	 */
	protected T decode(Decoder decoder, long length) {
		var value = get(decoder.memory(), decoder.offset(), length);
		decoder.inc(layoutSize());
		return value;
	}

	/**
	 * Provides sequential encoding for the array without padding, after bound checks. The count
	 * does not include the nul-terminator if specified.
	 */
	protected void encodeArray(Encoder encoder, A array, int index, int count, boolean nul) {
		encoder.accept(
			encoder.in() ? (m, o, l) -> writeArray(m, o, l, array, index, count, nul) : null,
			encoder.out() ? (m, o, l) -> readArray(m, o, l, array, index, count, nul) : null,
			size(count, nul));
	}

	/**
	 * Provides sequential decoding for an array from memory with max count and optional
	 * nul-termination, after bound checks. Count includes nul-terminator if specified.
	 */
	protected A decodeArray(Decoder decoder, long length, int count, boolean nul) {
		length = Math.min(length, size(count));
		var array = getArray(decoder.memory(), decoder.offset(), length, nul);
		if (array == null) return decodeNoVals(decoder, length);
		decoder.inc(size(RawArray.length(array), nul));
		return array;
	}

	/**
	 * Failed to find value; position the decoder and return an empty value.
	 */
	protected T decodeNoVal(Decoder decoder, long length) {
		decoder.inc(length);
		return val();
	}

	/**
	 * Failed to find values; position the decoder and return an empty array.
	 */
	protected A decodeNoVals(Decoder decoder, long length) {
		decoder.inc(length);
		return valArray(0);
	}

	/**
	 * Returns the nul-terminator size for encoding and decoding.
	 */
	protected int encodeTermSize() {
		return layoutSize();
	}

	protected static String arrayDesc(String type, int length, boolean nul) {
		int i = type.indexOf('[');
		if (i == -1) i = type.length();
		return String.format("%s[%d%s]%s", type.substring(0, i), length, nul ? "!" : "",
			type.substring(i));
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

	private long length(MemorySegment memory, long offset) {
		return Maths.min(Math.min(memory.byteSize() - offset, layoutSize()));
	}

	private long length(MemorySegment memory, long offset, long length) {
		return Maths.limit(length, 0L, length(memory, offset));
	}
}
