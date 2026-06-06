package ceri.ffm.type;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Map;
import java.util.Objects;
import ceri.common.array.Dimensions;
import ceri.common.array.RawArray;
import ceri.common.collect.Immutable;
import ceri.common.io.Buffers;
import ceri.common.math.Maths;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.common.text.ToString;
import ceri.common.util.Counter;
import ceri.common.util.Truth;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Segments;
import ceri.ffm.reflect.Refine;
import ceri.ffm.test.FfmTesting;

/**
 * Operational support for Buffers.
 */
public class BufferType<B extends Buffer, T, A, L extends ValueLayout>
	implements Layouts.Provider<L> {
	public static final BufferType<CharBuffer, Character, char[], ValueLayout.OfChar> CHAR =
		new BufferType<>(Buffers.CHAR, Primitive.CHAR);
	public static final BufferType<ByteBuffer, Byte, byte[], ValueLayout.OfByte> BYTE =
		new BufferType<>(Buffers.BYTE, Primitive.BYTE);
	public static final BufferType<ShortBuffer, Short, short[], ValueLayout.OfShort> SHORT =
		new BufferType<>(Buffers.SHORT, Primitive.SHORT);
	public static final BufferType<IntBuffer, Integer, int[], ValueLayout.OfInt> INT =
		new BufferType<>(Buffers.INT, Primitive.INT);
	public static final BufferType<LongBuffer, Long, long[], ValueLayout.OfLong> LONG =
		new BufferType<>(Buffers.LONG, Primitive.LONG);
	public static final BufferType<FloatBuffer, Float, float[], ValueLayout.OfFloat> FLOAT =
		new BufferType<>(Buffers.FLOAT, Primitive.FLOAT);
	public static final BufferType<DoubleBuffer, Double, double[], ValueLayout.OfDouble> DOUBLE =
		new BufferType<>(Buffers.DOUBLE, Primitive.DOUBLE);
	private static final Map<Class<?>, BufferType<?, ?, ?, ? extends ValueLayout>> MAP = map();
	private final Buffers<B, A> buffers;
	private final Primitive<T, A, L> primitive;

	public static void main(String[] args) {
		var s = INT;
		long a = 1;
		System.out.println(s);
		System.out.println(s.with(a, ByteOrder.BIG_ENDIAN));
		System.out.println(s.support(10, false));
		System.out.println(s.support(10, false).with(a, ByteOrder.BIG_ENDIAN));
		System.out.println(s.with(a, ByteOrder.BIG_ENDIAN).support(6, true));
		System.out.println(
			s.with(a, ByteOrder.BIG_ENDIAN).support(6, true).with(a, ByteOrder.LITTLE_ENDIAN));
		var m = s.support(8, true).alloc(Segments.auto(), Buffers.INT.of(1, 2, 3, 4, 5));
		FfmTesting.bin(m);
	}

	public static void main0(String[] args) {
		var sp = support(IntBuffer.class, 8, false);
		var m = Primitive.INT.allocAll(Segments.auto(), false, 1, 2, 3, 4, 5, 0, 6, 7, 8, 9, 0, 10,
			11, 12, 13, 14, 15, 16);
		// var m = sp.alloc(Segments.auto(), Buffers.INT.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
		FfmTesting.bin(m);
		var bb = sp.getArray(m, false);
		for (var b : bb)
			FfmTesting.arg(Buffers.INT.get(b));
		Buffers.INT.putAt(bb[1], 3, -1, -2, -3);
		FfmTesting.bin(m);
	}

	/**
	 * Operational support with fixed-size layout.
	 */
	public static class Supporter<B extends Buffer> extends Support.Typed<B, SequenceLayout> {
		private final BufferType<B, ?, ?, ? extends ValueLayout> buffer;
		private final boolean nul;

		private Supporter(BufferType<B, ?, ?, ?> buffer, boolean nul, SequenceLayout layout) {
			super(layout);
			this.buffer = buffer;
			this.nul = nul;
		}

		/**
		 * Returns the maximum buffer length, including nul-terminator if specified.
		 */
		public int count() {
			return (int) layout().elementCount();
		}

		/**
		 * Returns the nul-termination directive.
		 */
		public boolean nul() {
			return nul;
		}

		@Override
		public Class<B> type() {
			return buffer.bufferType();
		}

		@Override
		public boolean immutable() {
			return false;
		}

		@Override
		public boolean partial() {
			return true;
		}

		@Override
		public B val() {
			return buffer.nullVal();
		}

		@Override
		public Supporter<B> with(long align, ByteOrder order) {
			var buffer = this.buffer.with(align, order);
			var layout = buffer == this.buffer ? layout() : buffer.layout(count());
			layout = Layouts.align(layout, align);
			if (buffer == this.buffer && layout == layout()) return this;
			return new Supporter<>(buffer, nul, layout);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), nul);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			return (obj instanceof Supporter<?> s) && nul == s.nul && equalTo(s);
		}

		@Override
		public String toString() {
			return ToString.forClass(this, Reflect.simple(type()), count(), nul,
				Layouts.desc(layout()));
		}

		@Override
		B rawGet(MemorySegment memory, long offset, long length) {
			return buffer.asBuffer(memory, offset, length, nul);
		}

		@Override
		void rawRead(MemorySegment memory, long offset, long length, B value) {
			buffer.readAt(memory, offset, length, value, 0, Integer.MAX_VALUE, nul);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, B value) {
			buffer.writeAt(memory, offset, length, value, 0, Integer.MAX_VALUE, nul);
		}
	}

	/**
	 * Interface to provide a buffer from a memory segment, such as wrapping or copying.
	 */
	private interface Instantiator<B extends Buffer> {
		/** Provide a buffer from the memory segment with optional nul-termination. */
		B apply(MemorySegment memory, long offset, long length, boolean nul);
	}

	/**
	 * Returns fixed-layout operational support.
	 */
	public static <B extends Buffer> Supporter<B> support(Class<? extends B> cls, Refine.Context context) {
		return support(cls, context.size(), context.nul());
	}
	
	/**
	 * Returns fixed-layout operational support.
	 */
	public static <B extends Buffer> Supporter<B> support(Class<? extends B> cls, int count,
		boolean nul) {
		var buffer = BufferType.<B>of(cls);
		return buffer == null ? null : buffer.support(count, nul);
	}

	/**
	 * Provides buffer support based on buffer type.
	 */
	public static <B extends Buffer> BufferType<B, ?, ?, ?> from(Buffer buffer) {
		return Reflect.unchecked(MAP.get(Buffers.baseType(buffer)));
	}

	/**
	 * Provides buffer support based on buffer type.
	 */
	public static <B extends Buffer> BufferType<B, ?, ?, ?> of(Class<? extends B> cls) {
		return Reflect.unchecked(MAP.get(Buffers.baseType(cls)));
	}

	/**
	 * Returns the layout for a buffer, with byte order matching the buffer.
	 */
	public static ValueLayout layout(Buffer buffer) {
		var b = from(buffer);
		if (b == null) return null;
		return Layouts.order(b.layout(), Buffers.order(buffer));
	}

	/**
	 * Determines if the buffer can be used with {@code MemorySegment.ofBuffer(buffer)}.
	 */
	public static boolean canWrap(Buffer buffer) {
		if (buffer == null) return false;
		return buffer.isDirect() || buffer.hasArray();
	}

	/**
	 * Determines if the segment can be used with {@code MemorySegment.asByteBuffer()}. Not possible
	 * to determine read-only buffers.
	 */
	public static Truth canWrap(MemorySegment memory) {
		if (Segments.isNull(memory)) return Truth.no;
		if (memory.isNative()) return Truth.yes;
		var array = memory.heapBase().orElse(null);
		if (array == null && memory.isReadOnly()) return Truth.maybe; // read-only returns null
		return (array instanceof byte[]) ? Truth.yes : Truth.no;
	}

	/**
	 * Wraps the segment as a byte buffer with natural byte order.
	 */
	public static ByteBuffer asByte(MemorySegment memory) {
		if (Segments.isNull(memory)) return BYTE.nullVal();
		return memory.asByteBuffer().order(ByteOrder.nativeOrder());
	}

	private BufferType(Buffers<B, A> buffers, Primitive<T, A, L> primitive) {
		this.buffers = buffers;
		this.primitive = primitive;
	}

	/**
	 * Returns the matching primitive handler.
	 */
	public Primitive<T, A, L> primitive() {
		return primitive;
	}

	/**
	 * Returns fixed-layout operational support.
	 */
	public Supporter<B> support(int count, boolean nul) {
		return new Supporter<>(this, nul, layout(count));
	}

	/**
	 * Returns an instance with primitive layout modifications.
	 */
	public BufferType<B, T, A, L> with(long align, ByteOrder order) {
		align = Layouts.elementAlign(layout(), align);
		var primitive = primitive().with(align, order);
		return primitive == primitive() ? this : new BufferType<>(buffers, primitive);
	}

	/**
	 * Returns the matching buffer support.
	 */
	public Buffers<B, A> buffers() {
		return buffers;
	}

	/**
	 * Returns the buffer element class type.
	 */
	public Class<?> type() {
		return buffers.type();
	}

	/**
	 * Returns the buffer class type.
	 */
	public Class<B> bufferType() {
		return buffers.bufferType();
	}

	/**
	 * Returns an empty buffer to use as a null value.
	 */
	public B nullVal() {
		return buffers.empty();
	}

	@Override
	public L layout() {
		return primitive.layout();
	}

	/**
	 * Wraps the buffer as a memory segment from the current position; buffer must be direct or
	 * backed by an array. The buffer bounds are unchanged.
	 */
	public MemorySegment ofBuffer(B buffer) {
		return ofBuffer(buffer, Integer.MAX_VALUE);
	}

	/**
	 * Wraps the buffer as a memory segment from the current position; buffer must be direct or
	 * backed by an array. The buffer bounds are unchanged.
	 */
	public MemorySegment ofBuffer(B buffer, int count) {
		return buffers.apply(buffer, count, MemorySegment::ofBuffer);
	}

	/**
	 * Wraps the buffer as a memory segment from the given position; buffer must be direct or backed
	 * by an array. The buffer bounds are unchanged.
	 */
	public MemorySegment ofBufferAt(B buffer, int position) {
		return ofBufferAt(buffer, position, Integer.MAX_VALUE);
	}

	/**
	 * Wraps the buffer as a memory segment from the given position; buffer must be direct or backed
	 * by an array. The buffer bounds are unchanged.
	 */
	public MemorySegment ofBufferAt(B buffer, int position, int count) {
		return buffers.applyAt(buffer, position, count, MemorySegment::ofBuffer);
	}

	/**
	 * Wraps the memory segment with optional nul-termination as a buffer; the segment must be
	 * native or backed by a byte array. Byte order is native.
	 */
	public B asBuffer(MemorySegment memory, boolean nul) {
		return asBuffer(memory, 0L, nul);
	}

	/**
	 * Wraps the bound memory segment with optional nul-termination as a buffer; the segment must be
	 * native or backed by a byte array. Byte order is native.
	 */
	public B asBuffer(MemorySegment memory, long offset, boolean nul) {
		return asBuffer(memory, offset, Long.MAX_VALUE, nul);
	}

	/**
	 * Wraps the bound memory segment with optional nul-termination as a buffer; the segment must be
	 * native or backed by a byte array. Byte order is native.
	 */
	public B asBuffer(MemorySegment memory, long offset, long length, boolean nul) {
		var byteBuffer = asByte(slice(memory, offset, length, nul));
		return buffers.from(byteBuffer);
	}

	/**
	 * Allocates memory with a copy of the buffer from the current position, with optional
	 * nul-termination. The buffer position is updated after copying.
	 */
	public MemorySegment alloc(SegmentAllocator allocator, B buffer, boolean nul) {
		return alloc(allocator, buffer, Integer.MAX_VALUE, nul);
	}

	/**
	 * Allocates memory with a copy of the buffer from the current position, with optional
	 * nul-termination. The buffer position is updated after copying.
	 */
	public MemorySegment alloc(SegmentAllocator allocator, B buffer, int count, boolean nul) {
		if (allocator == null || buffer == null) return MemorySegment.NULL;
		count = Maths.limit(count, 0, buffer.remaining());
		var memory = allocator.allocate(size(count + (nul ? 1 : 0)));
		rawWrite(memory, 0L, buffer, count);
		if (nul) term().set(memory, size(count));
		return memory;
	}

	/**
	 * Allocates memory with a copy of the buffer, with optional nul-termination. The buffer
	 * position is updated after copying.
	 */
	public MemorySegment allocAt(SegmentAllocator allocator, B buffer, boolean nul) {
		return allocAt(allocator, buffer, 0, nul);
	}

	/**
	 * Allocates memory with a copy of the buffer from the given position, with optional
	 * nul-termination. The buffer position is updated after copying.
	 */
	public MemorySegment allocAt(SegmentAllocator allocator, B buffer, int position, boolean nul) {
		return allocAt(allocator, buffer, position, Integer.MAX_VALUE, nul);
	}

	/**
	 * Allocates memory with a copy of the buffer from the given position, with optional
	 * nul-termination. The buffer position is updated after copying.
	 */
	public MemorySegment allocAt(SegmentAllocator allocator, B buffer, int position, int count,
		boolean nul) {
		Buffers.position(buffer, position);
		return alloc(allocator, buffer, count, nul);
	}

	/**
	 * Creates a new buffer from an array copy of the bound memory segment with optional
	 * nul-termination. Fails if the length is larger than int.
	 */
	public B get(MemorySegment memory, boolean nul) {
		return get(memory, 0L, nul);
	}

	/**
	 * Creates a new buffer from an array copy of the bound memory segment with optional
	 * nul-termination. Fails if the length is larger than int.
	 */
	public B get(MemorySegment memory, long offset, boolean nul) {
		return get(memory, offset, Long.MAX_VALUE, nul);
	}

	/**
	 * Creates a new buffer from an array copy of the bound memory segment with optional
	 * nul-termination. Fails if the length is larger than int.
	 */
	public B get(MemorySegment memory, long offset, long length, boolean nul) {
		if (memory == null) return nullVal();
		var array = primitive.getArray(memory, offset, length, nul);
		return buffers.of(array, 0);
	}

	/**
	 * Copies values from the memory segment up to optional nul-termination, to the buffer at
	 * current position, within bounds. Returns the number of values copied. Returns 0 if
	 * nul-termination is specified but not found. The buffer position is updated after copying.
	 */
	public int read(MemorySegment memory, B buffer, boolean nul) {
		return read(memory, 0L, buffer, nul);
	}

	/**
	 * Copies values from the memory segment up to optional nul-termination, to the buffer at
	 * current position, within bounds. Returns the number of values copied. Returns 0 if
	 * nul-termination is specified but not found. The buffer position is updated after copying.
	 */
	public int read(MemorySegment memory, long offset, B buffer, boolean nul) {
		return read(memory, offset, Long.MAX_VALUE, buffer, Integer.MAX_VALUE, nul);
	}

	/**
	 * Copies values from the memory segment up to optional nul-termination, to the buffer at
	 * current position, within bounds. Returns the number of values copied. Returns 0 if
	 * nul-termination is specified but not found. The buffer position is updated after copying.
	 */
	public int read(MemorySegment memory, long offset, long length, B buffer, int count,
		boolean nul) {
		if (buffer == null || Segments.isNull(memory)) return 0;
		if (nul) return read(slice(memory, offset, length, nul), 0L, Long.MAX_VALUE, buffer, count,
			false);
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		count = Maths.limit(count, 0, buffer.remaining());
		count = (int) Math.min(count, count(length));
		if (count > 0) rawRead(memory, offset, buffer, count);
		return count;
	}

	/**
	 * Copies values from the memory segment up to optional nul-termination, to the buffer at given
	 * position, within bounds. Returns the number of values copied. Returns 0 if nul-termination is
	 * specified but not found. The buffer position is updated after copying.
	 */
	public int readAt(MemorySegment memory, B buffer, boolean nul) {
		return readAt(memory, 0L, buffer, 0, nul);
	}

	/**
	 * Copies values from the memory segment up to optional nul-termination, to the buffer at given
	 * position, within bounds. Returns the number of values copied. Returns 0 if nul-termination is
	 * specified but not found. The buffer position is updated after copying.
	 */
	public int readAt(MemorySegment memory, long offset, B buffer, int position, boolean nul) {
		return readAt(memory, offset, Long.MAX_VALUE, buffer, position, Integer.MAX_VALUE, nul);
	}

	/**
	 * Copies values from the memory segment up to optional nul-termination, to the buffer at given
	 * position, within bounds. Returns the number of values copied. Returns 0 if nul-termination is
	 * specified but not found. The buffer position is updated after copying.
	 */
	public int readAt(MemorySegment memory, long offset, long length, B buffer, int position,
		int count, boolean nul) {
		Buffers.position(buffer, position);
		return read(memory, offset, length, buffer, count, nul);
	}

	/**
	 * Copies values from the buffer at current position to memory with optional nul-termination,
	 * and within bounds. Returns the number of values copied, including nul-terminator. The buffer
	 * position is updated after copying.
	 */
	public int write(MemorySegment memory, B buffer, boolean nul) {
		return write(memory, 0L, buffer, nul);
	}

	/**
	 * Copies values from the buffer at current position to memory with optional nul-termination,
	 * and within bounds. Returns the number of values copied, including nul-terminator. The buffer
	 * position is updated after copying.
	 */
	public int write(MemorySegment memory, long offset, B buffer, boolean nul) {
		return write(memory, offset, Long.MAX_VALUE, buffer, Integer.MAX_VALUE, nul);
	}

	/**
	 * Copies values from the buffer at current position to memory with optional nul-termination,
	 * and within bounds. Returns the number of values copied, including nul-terminator. The buffer
	 * position is updated after copying.
	 */
	public int write(MemorySegment memory, long offset, long length, B buffer, int count,
		boolean nul) {
		if (buffer == null || Segments.isNull(memory)) return 0;
		count = Maths.limit(count, 0, buffer.remaining()) + (nul ? 1 : 0);
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		count = (int) Math.min(count, count(length));
		if (!nul && count > 0) rawWrite(memory, offset, buffer, count);
		if (nul && count > 1) rawWrite(memory, offset, buffer, count - 1);
		if (nul && count > 0) term().set(memory, offset + size(count - 1));
		return count;
	}

	/**
	 * Copies values from the buffer at given position to memory with optional nul-termination, and
	 * within bounds. Returns the number of values copied, including nul-terminator. The buffer
	 * position is updated after copying.
	 */
	public int writeAt(MemorySegment memory, B buffer, boolean nul) {
		return writeAt(memory, 0L, buffer, 0, nul);
	}

	/**
	 * Copies values from the buffer at given position to memory with optional nul-termination, and
	 * within bounds. Returns the number of values copied, including nul-terminator. The buffer
	 * position is updated after copying.
	 */
	public int writeAt(MemorySegment memory, long offset, B buffer, int position, boolean nul) {
		return writeAt(memory, offset, Long.MAX_VALUE, buffer, position, Integer.MAX_VALUE, nul);
	}

	/**
	 * Copies values from the buffer at given position to memory with optional nul-termination, and
	 * within bounds. Returns the number of values copied, including nul-terminator. The buffer
	 * position is updated after copying.
	 */
	public int writeAt(MemorySegment memory, long offset, long length, B buffer, int position,
		int count, boolean nul) {
		Buffers.position(buffer, position);
		return write(memory, offset, length, buffer, count, nul);
	}

	/**
	 * Calculates the memory size to store the remaining values of a multi-dimensional array of
	 * buffers with optional nul-terminators. Returns 0 if the array type is not supported.
	 */
	public long deepSize(Object t, boolean nul) {
		return rawDeepSize(t, dimsOf(t), nul);
	}

	/**
	 * Creates a multi-dimensional array of memory segments that match and wrap the leaves of a
	 * multi-dimensional buffer array.
	 */
	public <U> U deepOfBuffers(Object array) {
		if (dimsOf(array) < 0) return null;
		return RawArray.<B, MemorySegment, U>deepAdapt(array, MemorySegment.class, this::ofBuffer,
			false);
	}

	/**
	 * Creates a multi-dimensional array of buffers that wrap memory up to optional nul-terminators.
	 * Dimensions specify the array dimensions, count specifies the fixed or max buffer sizes,
	 * depending on nul-termination.
	 */
	public <U> U deepAsBuffers(MemorySegment memory, Dimensions dims, int count, boolean nul) {
		return deepAsBuffers(memory, 0L, dims, count, nul);
	}

	/**
	 * Creates a multi-dimensional array of buffers that wrap memory up to optional nul-terminators.
	 * Dimensions specify the array dimensions, count specifies the fixed or max buffer sizes,
	 * depending on nul-termination.
	 */
	public <U> U deepAsBuffers(MemorySegment memory, long offset, Dimensions dims, int count,
		boolean nul) {
		return deepAsBuffers(memory, offset, Long.MAX_VALUE, dims, count, nul);
	}

	/**
	 * Creates a multi-dimensional array of buffers that wrap memory up to optional nul-terminators.
	 * Dimensions specify the array dimensions, count specifies the fixed or max buffer sizes,
	 * depending on nul-termination.
	 */
	public <U> U deepAsBuffers(MemorySegment memory, long offset, long length, Dimensions dims,
		int count, boolean nul) {
		return deepInstantiate(this::asBuffer, memory, offset, length, dims, count, nul);
	}

	/**
	 * Allocates memory for the remaining values of a multi-dimensional array of buffers with
	 * optional nul-termination. Returns null if the array type is not supported.
	 */
	public MemorySegment deepAllocEmpty(SegmentAllocator allocator, Object t, boolean nul) {
		int dims = dimsOf(t);
		if (allocator == null || dims < 0) return null;
		return allocator.allocate(rawDeepSize(t, dims, nul));
	}

	/**
	 * Allocates memory for the remaining values of a multi-dimensional array of buffers and copies
	 * the values with optional nul-termination. Returns null if the array type is not supported.
	 */
	public MemorySegment deepAlloc(SegmentAllocator allocator, Object t, boolean nul) {
		int dims = dimsOf(t);
		if (allocator == null || dims < 0) return null;
		var memory = allocator.allocate(rawDeepSize(t, dims, nul));
		rawDeepWrite(memory, 0L, Long.MAX_VALUE, t, nul);
		return memory;
	}

	/**
	 * Creates a multi-dimensional array of buffers with values copied from memory up to optional
	 * nul-terminators. Dimensions specify the array dimensions, count specifies the fixed or max
	 * buffer sizes, depending on nul-termination.
	 */
	public <U> U deepGet(MemorySegment memory, Dimensions dims, int count, boolean nul) {
		return deepGet(memory, 0L, dims, count, nul);
	}

	/**
	 * Creates a multi-dimensional array of buffers with values copied from memory up to optional
	 * nul-terminators. Dimensions specify the array dimensions, count specifies the fixed or max
	 * buffer sizes, depending on nul-termination.
	 */
	public <U> U deepGet(MemorySegment memory, long offset, Dimensions dims, int count,
		boolean nul) {
		return deepGet(memory, offset, Long.MAX_VALUE, dims, count, nul);
	}

	/**
	 * Creates a multi-dimensional array of buffers with values copied from memory up to optional
	 * nul-terminators. Dimensions specify the array dimensions, count specifies the fixed or max
	 * buffer sizes, depending on nul-termination.
	 */
	public <U> U deepGet(MemorySegment memory, long offset, long length, Dimensions dims, int count,
		boolean nul) {
		return deepInstantiate(this::get, memory, offset, length, dims, count, nul);
	}

	/**
	 * Copies memory to a multi-dimensional array of buffers up to optional nul-terminators. Returns
	 * the number of values copied. Returns 0 if the array type is not supported.
	 */
	public int deepRead(MemorySegment memory, Object t, int count, boolean nul) {
		return deepRead(memory, 0L, t, count, nul);
	}

	/**
	 * Copies memory to a multi-dimensional array of buffers up to optional nul-terminators. Returns
	 * the number of values copied. Returns 0 if the array type is not supported.
	 */
	public int deepRead(MemorySegment memory, long offset, Object t, int count, boolean nul) {
		return deepRead(memory, offset, Long.MAX_VALUE, t, count, nul);
	}

	/**
	 * Copies memory to a multi-dimensional array of buffers up to optional nul-terminators. Returns
	 * the number of values copied. Returns 0 if the array type is not supported.
	 */
	public int deepRead(MemorySegment memory, long offset, long length, Object t, int count,
		boolean nul) {
		if (Segments.isNull(memory) || dimsOf(t) < 0) return 0;
		return rawDeepRead(memory, offset, length, t, count, nul);
	}

	/**
	 * Copies remaining values of a multi-dimensional array of buffers to memory with optional
	 * nul-terminators. Returns the number of values copied, including nul-terminators. Returns 0 if
	 * the array type is not supported.
	 */
	public int deepWrite(MemorySegment memory, Object t, boolean nul) {
		return deepWrite(memory, 0L, t, nul);
	}

	/**
	 * Copies remaining values of a multi-dimensional array of buffers to memory with optional
	 * nul-terminators. Returns the number of values copied, including nul-terminators. Returns 0 if
	 * the array type is not supported.
	 */
	public int deepWrite(MemorySegment memory, long offset, Object t, boolean nul) {
		return deepWrite(memory, offset, Long.MAX_VALUE, t, nul);
	}

	/**
	 * Copies remaining values of a multi-dimensional array of buffers to memory with optional
	 * nul-terminators. Returns the number of values copied, including nul-terminators. Returns 0 if
	 * the array type is not supported.
	 */
	public int deepWrite(MemorySegment memory, long offset, long length, Object t, boolean nul) {
		if (Segments.isNull(memory) || dimsOf(t) < 0) return 0;
		return rawDeepWrite(memory, offset, length, t, nul);
	}

	@Override
	public int hashCode() {
		return Objects.hash(primitive);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return (obj instanceof BufferType b) && Objects.equals(primitive, b.primitive);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, buffers, Layouts.desc(layout()));
	}

	// support

	private <U> U deepInstantiate(Instantiator<B> instantiator, MemorySegment memory, long offset,
		long length, Dimensions dims, int count, boolean nul) {
		memory = slice(memory, offset, length, false);
		if (memory == null) return null;
		count = Math.max(count, 0);
		return rawDeepInstantiate(instantiator, memory, dims, count, nul);
	}

	private void rawRead(MemorySegment memory, long offset, B buffer, int count) {
		var wrapped = asBuffer(memory, offset, size(count), false);
		buffers.copy(wrapped, buffer);
	}

	private void rawWrite(MemorySegment memory, long offset, B buffer, int count) {
		var wrapped = asBuffer(memory, offset, size(count), false);
		buffers.copy(buffer, wrapped);
	}

	private long rawDeepSize(Object t, int dims, boolean nul) {
		if (dims < 0) return 0;
		return MultiArray.<Buffer>iterate(t, null, 0L, 0L,
			(b, _, _) -> size(Buffers.remaining(b) + (nul ? 1 : 0)));
	}

	private <U> U rawDeepInstantiate(Instantiator<B> instantiator, MemorySegment memory,
		Dimensions dims, int count, boolean nul) {
		var length = size(count);
		var offset = Counter.of(0L);
		return RawArray.<B, U>deepReplace(newArray(dims), _ -> {
			var b = instantiator.apply(memory, offset.get(), length, nul);
			if (b == null) offset.set(memory.byteSize()); // no more buffers
			else offset.inc(size(b.remaining() + (nul ? 1 : 0)));
			return b == null ? nullVal() : b;
		});
	}

	private int rawDeepRead(MemorySegment memory, long offset, long length, Object t, int count,
		boolean nul) {
		long total = MultiArray.<B>iterate(t, memory, offset, length,
			(b, m, o) -> size(read(m, o, m.byteSize() - o, b, count, nul)));
		return countInt(total);
	}

	private int rawDeepWrite(MemorySegment memory, long offset, long length, Object t,
		boolean nul) {
		long total = MultiArray.<B>iterate(t, memory, offset, length,
			(b, m, o) -> size(write(m, o, b, nul)));
		return countInt(total);
	}

	private int dimsOf(Object t) {
		var typed = Generics.typedClass(t).array();
		return Reflect.assignable(bufferType(), typed.cls()) ? typed.dimensions() : -1;
	}

	private <U> U newArray(Dimensions dims) {
		return Dimensions.create(dims, bufferType());
	}

	private SequenceLayout layout(int count) {
		return MemoryLayout.sequenceLayout(count, layout());
	}

	private static Map<Class<?>, BufferType<?, ?, ?, ? extends ValueLayout>> map() {
		return Immutable.convertMapOf(BufferType::bufferType, t -> t, CHAR, BYTE, SHORT, INT, LONG,
			FLOAT, DOUBLE);
	}
}
