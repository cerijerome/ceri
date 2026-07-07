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
import ceri.common.collect.Immutable;
import ceri.common.io.Buffers;
import ceri.common.io.Direction;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.text.ToString;
import ceri.common.util.Truth;
import ceri.ffm.core.Decoder;
import ceri.ffm.core.Encoder;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;
import ceri.ffm.test.FfmTesting;

/**
 * Operational support for Buffers.
 */
public class BufferType<B extends Buffer, T, A, L extends ValueLayout>
	implements Layouts.Provider<L> {
	public static final BufferType<CharBuffer, Character, char[], ValueLayout.OfChar> CHAR =
		of(Buffers.CHAR, Primitive.CHAR);
	public static final BufferType<ByteBuffer, Byte, byte[], ValueLayout.OfByte> BYTE =
		of(Buffers.BYTE, Primitive.BYTE);
	public static final BufferType<ShortBuffer, Short, short[], ValueLayout.OfShort> SHORT =
		of(Buffers.SHORT, Primitive.SHORT);
	public static final BufferType<IntBuffer, Integer, int[], ValueLayout.OfInt> INT =
		of(Buffers.INT, Primitive.INT);
	public static final BufferType<LongBuffer, Long, long[], ValueLayout.OfLong> LONG =
		of(Buffers.LONG, Primitive.LONG);
	public static final BufferType<FloatBuffer, Float, float[], ValueLayout.OfFloat> FLOAT =
		of(Buffers.FLOAT, Primitive.FLOAT);
	public static final BufferType<DoubleBuffer, Double, double[], ValueLayout.OfDouble> DOUBLE =
		of(Buffers.DOUBLE, Primitive.DOUBLE);
	private static final Map<Class<?>, BufferType<?, ?, ?, ?>> MAP = map();
	private final Config<B, A> config;
	private final Primitive<T, A, L> primitive;

	public static void main(String[] args) {
		var s = INT.support(5, true);
		FfmTesting.bin(s.alloc(Buffers.INT.of(1, 2, 3)));
		var r = s.encodeAll(Direction.duplex, true, Buffers.INT.of(1, 2, 3), Buffers.INT.of(4, 5));
		FfmTesting.bin(r.value());
		var a = s.asArray(4, true).decode(r.value());
		FfmTesting.arg(a);
	}

	private static class Config<B extends Buffer, A> {
		private final Buffers<B, A> buffers;
		private final B emptyL;
		private final B emptyB;

		Config(Buffers<B, A> buffers) {
			this.buffers = buffers;
			emptyL = buffers.from(emptyByte(ByteOrder.LITTLE_ENDIAN));
			emptyB = buffers.from(emptyByte(ByteOrder.BIG_ENDIAN));
		}

		/**
		 * Returns basic buffer support.
		 */
		public Buffers<B, A> buffers() {
			return buffers;
		}

		/**
		 * Provides an empty buffer with given byte order.
		 */
		public B empty(ByteOrder order) {
			return order == ByteOrder.BIG_ENDIAN ? emptyB : emptyL;
		}

		@Override
		public String toString() {
			return buffers.toString();
		}
	}

	/**
	 * Operational support with fixed-size layout.
	 */
	public static class Supporter<B extends Buffer> extends Support.Typed<B, SequenceLayout> {
		private final BufferType<B, ?, ?, ?> buffer;
		private final boolean nul;

		private Supporter(BufferType<B, ?, ?, ?> buffer, boolean nul, SequenceLayout layout) {
			super(layout);
			this.buffer = buffer;
			this.nul = nul;
		}

		@Override
		public Native.Kind kind() {
			return Native.Kind.buffer;
		}

		/**
		 * Returns the maximum char count, including nul-terminator if specified.
		 */
		public int length() {
			return (int) buffer.count(layoutSize());
		}

		/**
		 * Returns the maximum char count, without nul-terminator.
		 */
		public int count() {
			return Math.max(0, length() - (nul ? 1 : 0));
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
		public String typeDesc() {
			return arrayDesc(Reflect.simple(type()), length(), nul());
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
		public Supporter<B> align(long align) {
			return create(buffer.align(align), align);
		}

		@Override
		public Supporter<B> order(ByteOrder order) {
			return create(buffer.order(order), layout().byteAlignment());
		}

		@Override
		public Native.Adapted<MemorySegment> encode(Direction direction, SegmentAllocator allocator,
			B value) {
			if (nul() || !Buffers.isDirect(value)) return super.encode(direction, allocator, value);
			return Native.Adapted.of(ofBuffer(value, count()));
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

		// shared

		@Override
		protected B rawGet(MemorySegment memory, long offset, long length) {
			return buffer.asBuffer(memory, offset, length, nul);
		}

		@Override
		protected void rawRead(MemorySegment memory, long offset, long length, B value) {
			buffer.readAt(memory, offset, length, value, 0, Integer.MAX_VALUE, nul());
		}

		@Override
		protected void rawWrite(MemorySegment memory, long offset, long length, B value) {
			buffer.writeAt(memory, offset, length, value, 0, Integer.MAX_VALUE, nul());
		}

		@Override
		protected void encode(Encoder encoder, B value) {
			int position = value.position();
			long length = Math.min(layoutSize(), buffer.size(value.remaining(), nul()));
			encoder.accept(encoder.in() ? (m, o, l) -> encodeIn(m, o, l, value, position) : null,
				encoder.out() ? (m, o, l) -> encodeOut(m, o, l, value, position) : null, length);
		}

		@Override
		protected B decode(Decoder decoder, long length) {
			length = Math.min(length, layoutSize());
			var value = buffer.asBuffer(decoder.memory(), decoder.offset(), length, nul());
			if (value == null) return decodeNoVal(decoder, length);
			decoder.inc(buffer.size(value.remaining(), nul()));
			return value;
		}

		@Override
		protected void encodeArray(Encoder encoder, B[] array, int index, int count, boolean nul) {
			encodeDynamicArray(encoder, array, index, count, nul);
		}

		@Override
		protected B[] decodeArray(Decoder decoder, long length, int count, boolean nul) {
			return decodeDynamicArray(decoder, length, count, nul);
		}

		@Override
		protected int encodeTermSize() {
			return buffer.layoutSize() * (nul() ? 1 : length());
		}

		// support

		private void encodeIn(MemorySegment memory, long offset, long length, B value,
			int position) {
			write(memory, offset, length, value);
			value.position(position);
		}

		private void encodeOut(MemorySegment memory, long offset, long length, B value,
			int position) {
			read(memory, offset, length, value);
			value.position(position);
		}

		private Supporter<B> create(BufferType<B, ?, ?, ?> buffer, long align) {
			var layout = buffer == this.buffer ? layout() : buffer.layout(length());
			layout = Layouts.align(layout, align);
			if (buffer == this.buffer && layout == layout()) return this;
			return new Supporter<>(buffer, nul, layout);
		}
	}

	/**
	 * Creates fixed-layout operational support.
	 */
	static <B extends Buffer> Supporter<B> supportFor(Class<? extends B> cls, int count,
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
	 * Wraps the segment as a byte buffer with native byte order.
	 */
	public static ByteBuffer asByte(MemorySegment memory) {
		return asByte(ByteOrder.nativeOrder(), memory);
	}

	/**
	 * Wraps the segment as a byte buffer with given byte order.
	 */
	public static ByteBuffer asByte(ByteOrder order, MemorySegment memory) {
		if (Segments.isNull(memory)) return BYTE.nullVal().order(order);
		return memory.asByteBuffer().order(order);
	}

	/**
	 * Wraps the buffer as a memory segment from the current position; buffer must be direct or
	 * backed by an array. The buffer bounds are unchanged.
	 */
	public static <B extends Buffer> MemorySegment ofBuffer(B buffer) {
		return ofBuffer(buffer, Integer.MAX_VALUE);
	}

	/**
	 * Wraps the buffer as a memory segment from the current position; buffer must be direct or
	 * backed by an array. The buffer bounds are unchanged.
	 */
	public static <B extends Buffer> MemorySegment ofBuffer(B buffer, int count) {
		// byte order is handled correctly for direct and array-based buffers
		return Buffers.apply(buffer, count, MemorySegment::ofBuffer);
	}

	/**
	 * Wraps the buffer as a memory segment from the given position; buffer must be direct or backed
	 * by an array. The buffer bounds are unchanged.
	 */
	public static <B extends Buffer> MemorySegment ofBufferAt(B buffer, int position) {
		return ofBufferAt(buffer, position, Integer.MAX_VALUE);
	}

	/**
	 * Wraps the buffer as a memory segment from the given position; buffer must be direct or backed
	 * by an array. The buffer bounds are unchanged.
	 */
	public static <B extends Buffer> MemorySegment ofBufferAt(B buffer, int position, int count) {
		// byte order is handled correctly for direct and array-based buffers
		return Buffers.applyAt(buffer, position, count, MemorySegment::ofBuffer);
	}

	private static <B extends Buffer, T, A, L extends ValueLayout> BufferType<B, T, A, L>
		of(Buffers<B, A> buffers, Primitive<T, A, L> primitive) {
		return new BufferType<>(new Config<>(buffers), primitive);
	}

	private BufferType(Config<B, A> config, Primitive<T, A, L> primitive) {
		this.config = config;
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
	 * Returns an instance with given layout alignment.
	 */
	public BufferType<B, T, A, L> align(long align) {
		align = Layouts.elementAlign(layout(), align);
		var primitive = primitive().align(align);
		return primitive == primitive() ? this : new BufferType<>(config, primitive);
	}

	/**
	 * Returns an instance with given byte order. Order is used for creation of buffers.
	 */
	public BufferType<B, T, A, L> order(ByteOrder order) {
		var primitive = primitive().order(order);
		return primitive == primitive() ? this : new BufferType<>(config, primitive);
	}

	/**
	 * Returns the byte order to be used for buffer creation.
	 */
	public ByteOrder order() {
		return primitive.layout().order();
	}

	/**
	 * Returns the matching buffer support.
	 */
	public Buffers<B, A> buffers() {
		return config.buffers();
	}

	/**
	 * Returns the buffer element class type.
	 */
	public Class<?> type() {
		return buffers().type();
	}

	/**
	 * Returns the buffer class type.
	 */
	public Class<B> bufferType() {
		return buffers().bufferType();
	}

	/**
	 * Returns an empty buffer to use as a null value, with current byte order.
	 */
	public B nullVal() {
		return config.empty(order());
	}

	@Override
	public L layout() {
		return primitive.layout();
	}

	/**
	 * Wraps the bound memory segment with optional nul-termination as a buffer with current byte
	 * order. The segment must be native or backed by a byte array.
	 */
	public B asBuffer(MemorySegment memory, boolean nul) {
		return asBuffer(memory, 0L, nul);
	}

	/**
	 * Wraps the bound memory segment with optional nul-termination as a buffer with current byte
	 * order. The segment must be native or backed by a byte array.
	 */
	public B asBuffer(MemorySegment memory, long offset, boolean nul) {
		return asBuffer(memory, offset, Long.MAX_VALUE, nul);
	}

	/**
	 * Wraps the bound memory segment with optional nul-termination as a buffer with current byte
	 * order. The segment must be native or backed by a byte array.
	 */
	public B asBuffer(MemorySegment memory, long offset, long length, boolean nul) {
		return asBuffer(order(), memory, offset, length, nul);
	}

	/**
	 * Allocates memory with a copy of the buffer from the current position, with optional
	 * nul-termination. The buffer position is updated after copying.
	 */
	public MemorySegment alloc(B buffer, boolean nul) {
		return alloc(buffer, Integer.MAX_VALUE, nul);
	}

	/**
	 * Allocates memory with a copy of the buffer from the current position, with optional
	 * nul-termination. The buffer position is updated after copying.
	 */
	public MemorySegment alloc(B buffer, int count, boolean nul) {
		return alloc(Segments.auto(), buffer, count, nul);
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
	 * Allocates memory with a copy of the buffer from the given position, with optional
	 * nul-termination. The buffer position is updated after copying.
	 */
	public MemorySegment allocAt(B buffer, boolean nul) {
		return allocAt(buffer, 0, nul);
	}

	/**
	 * Allocates memory with a copy of the buffer from the given position, with optional
	 * nul-termination. The buffer position is updated after copying.
	 */
	public MemorySegment allocAt(B buffer, int position, boolean nul) {
		return allocAt(buffer, position, Integer.MAX_VALUE, nul);
	}

	/**
	 * Allocates memory with a copy of the buffer from the given position, with optional
	 * nul-termination. The buffer position is updated after copying.
	 */
	public MemorySegment allocAt(B buffer, int position, int count, boolean nul) {
		return allocAt(Segments.auto(), buffer, position, count, nul);
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
	 * Creates a new buffer from a byte array copy of the bound memory segment, with current byte
	 * order and optional nul-termination. Fails if the length is larger than int.
	 */
	public B get(MemorySegment memory, boolean nul) {
		return get(memory, 0L, nul);
	}

	/**
	 * Creates a new buffer from a byte array copy of the bound memory segment, with current byte
	 * order and optional nul-termination. Fails if the length is larger than int.
	 */
	public B get(MemorySegment memory, long offset, boolean nul) {
		return get(memory, offset, Long.MAX_VALUE, nul);
	}

	/**
	 * Creates a new buffer from a byte array copy of the bound memory segment, with current byte
	 * order and optional nul-termination. Fails if the length is larger than int.
	 */
	public B get(MemorySegment memory, long offset, long length, boolean nul) {
		if (memory == null) return nullVal();
		var array = Primitive.BYTE.getArray(memory, offset, length, nul);
		return buffers().from(Buffers.BYTE.of(array).order(order()));
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
		return ToString.forClass(this, config, Layouts.desc(layout()));
	}

	// support

	private void rawRead(MemorySegment memory, long offset, B buffer, int count) {
		var wrapped = asBuffer(Buffers.order(buffer), memory, offset, size(count), false);
		buffers().copy(wrapped, buffer);
	}

	private void rawWrite(MemorySegment memory, long offset, B buffer, int count) {
		var wrapped = asBuffer(Buffers.order(buffer), memory, offset, size(count), false);
		buffers().copy(buffer, wrapped);
	}

	private B asBuffer(ByteOrder order, MemorySegment memory, long offset, long length,
		boolean nul) {
		var byteBuffer = asByte(order, slice(memory, offset, length, nul));
		return buffers().from(byteBuffer);
	}

	private SequenceLayout layout(int count) {
		return MemoryLayout.sequenceLayout(count, layout());
	}

	private static ByteBuffer emptyByte(ByteOrder order) {
		return Buffers.BYTE.of().order(order).asReadOnlyBuffer();
	}

	private static Map<Class<?>, BufferType<?, ?, ?, ?>> map() {
		return Immutable.convertMapOf(BufferType::bufferType, t -> t, CHAR, BYTE, SHORT, INT, LONG,
			FLOAT, DOUBLE);
	}
}
