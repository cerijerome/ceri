package ceri.ffm.type;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ceri.common.array.Dimensions;
import ceri.common.array.RawArray;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.io.Buffers;
import ceri.common.math.Maths;
import ceri.common.reflect.Generics;
import ceri.common.stream.Streams;
import ceri.common.text.Chars;
import ceri.common.text.Strings;
import ceri.common.text.ToString;
import ceri.common.util.Basics;
import ceri.common.util.Counter;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Segments;
import ceri.ffm.reflect.Refine;
import ceri.ffm.test.FfmTesting;

/**
 * Handler for nul-terminated strings with any encoding. Can be used for byte and wchar arrays by
 * choosing a compatible charset.
 */
public class StringType implements Layouts.Provider<ValueLayout> {
	public static final StringType DEFAULT = init(Charset.defaultCharset());
	public static final StringType ASCII = init(StandardCharsets.US_ASCII);
	public static final StringType LATIN1 = init(StandardCharsets.ISO_8859_1);
	public static final StringType UTF8 = init(Chars.UTF8);
	public static final StringType UTF16 = init(Chars.UTF16);
	public static final StringType UTF32 = init(Chars.UTF32);
	private static final Map<Charset, StringType> MAP = map();
	private final Config config;

	// Wait until use cases:
	// - deep alloc/write min/max/exact (!)nul
	// - deep get/read min/max/exact (!)nul

	public static void main(String[] args) {
		String[] ss = { "abcdef", "g", "", "hijk", null, "lmnop", "" };
		var s = UTF8.support(5, true);
		var m = s.encodeAll(true, ss);
		FfmTesting.bin(m);
		m = s.asArray(5, true).encode(ss);
		FfmTesting.bin(m);
	}

	public static void main0(String[] args) {
		var s = UTF32;
		System.out.println(s);
		System.out.println(s.with(ByteOrder.BIG_ENDIAN));
		System.out.println(s.support(10, false));
		System.out.println(s.support(10, false).with(2, ByteOrder.BIG_ENDIAN));
		System.out.println(s.with(ByteOrder.BIG_ENDIAN).support(6, true));
		System.out.println(
			s.with(ByteOrder.BIG_ENDIAN).support(6, true).with(8, ByteOrder.LITTLE_ENDIAN));
		var m = s.support(8, true).alloc(Segments.auto(), "hello12345");
		FfmTesting.bin(m);
	}

	private static record Config(Charset charset, Chars.Info info, Terminator term,
		ValueLayout layout) {
		public static Config of(Charset charset) {
			var info = Chars.Info.of(charset);
			var term = Terminator.of(info.term().length());
			var layout = Layouts.order(Layouts.ofInt(term.size()), info.order().order);
			return new Config(charset, info, term, layout);
		}
	}

	/**
	 * Operational support with fixed-size layout.
	 */
	public static class Supporter extends Support.Typed<String, SequenceLayout> {
		private final StringType string;
		private final boolean nul;

		private Supporter(StringType string, boolean nul, SequenceLayout layout) {
			super(layout);
			this.string = string;
			this.nul = nul;
		}

		/**
		 * Returns the maximum char count, including nul-terminator if specified.
		 */
		public int count() {
			return (int) string.count(layoutSize());
		}

		/**
		 * Returns the nul-termination directive.
		 */
		public boolean nul() {
			return nul;
		}

		@Override
		public Class<String> type() {
			return String.class;
		}

		@Override
		public boolean partial() {
			return true;
		}

		@Override
		public String val() {
			return "";
		}

		@Override
		public Supporter with(long align, ByteOrder order) {
			var string = this.string.with(order);
			var layout = string == this.string ? layout() : string.layout(count());
			layout = Layouts.align(layout, align);
			if (string == this.string && layout == layout()) return this;
			return new Supporter(string, nul, layout);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), string, nul);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			return (obj instanceof Supporter s) && nul == s.nul && Objects.equals(string, s.string)
				&& equalTo(s);
		}

		@Override
		public String toString() {
			return ToString.forClass(this, string.charset(), count(), nul, Layouts.desc(layout()));
		}

		@Override
		String rawGet(MemorySegment memory, long offset, long length) {
			return string.get(memory, offset, length, nul);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, String value) {
			string.write(memory, offset, length, value, 0, Integer.MAX_VALUE, nul);
		}

		@Override
		void encode(Segments.Encoder encoder, String value) {
			var buffer = buffer(value);
			encoder.accept((m, o, _) -> write(m, o, buffer), length(buffer));
		}

		@Override
		void encodeArray(Segments.Encoder encoder, String[] array, int index, int count,
			boolean nul) {
			var buffers = buffers(array, index, count);
			long size = Streams.of(buffers).mapToLong(this::length).sum();
			encoder.accept((m, o, _) -> write(m, o, buffers, nul), size + string.termSize(nul));
		}

		private ByteBuffer buffer(CharSequence value) {
			int count = Math.min(Strings.length(value), count() - (nul ? 1 : 0));
			return string.buffer(value, 0, count);
		}

		private ByteBuffer[] buffers(CharSequence[] array, int index, int count) {
			var buffers = new ByteBuffer[count];
			for (int i = 0; i < count; i++)
				buffers[i] = buffer(array[index + i]);
			return buffers;
		}

		private long length(ByteBuffer buffer) {
			return string.length(buffer, nul());
		}

		private long write(MemorySegment memory, long offset, ByteBuffer buffer) {
			return string.write(memory, offset, buffer, nul());
		}

		private long write(MemorySegment memory, long offset, ByteBuffer[] buffers, boolean nul) {
			for (var buffer : buffers)
				offset = write(memory, offset, buffer);
			if (nul) offset += string.term().set(memory, offset);
			return offset;
		}
	}

	private long length(ByteBuffer buffer, boolean nul) {
		return Buffers.limit(buffer) + termSize(nul);
	}

	private long write(MemorySegment memory, long offset, ByteBuffer buffer, boolean nul) {
		int n = BufferType.BYTE.write(memory, offset, buffer, false);
		if (nul) term().set(memory, offset + n);
		return offset + length(buffer, nul);
	}

	private ByteBuffer buffer(CharSequence value, int index, int count) {
		return Chars.encode(charset(), Buffers.CHAR.of(value, index, count));
	}

	/**
	 * Returns fixed-layout operational support.
	 */
	public static Supporter support(Refine.Context context) {
		return support(context.chars(), context.size(), context.nul());
	}

	/**
	 * Returns fixed-layout operational support.
	 */
	public static Supporter support(Charset charset, int count, boolean nul) {
		var string = StringType.of(charset);
		return string == null ? null : string.support(count, nul);
	}

	/**
	 * Returns an instance for the charset.
	 */
	public static StringType of(Charset charset) {
		charset = Chars.safe(charset);
		var string = MAP.get(charset);
		return string != null ? string : new StringType(Config.of(charset));
	}

	private StringType(Config config) {
		this.config = config;
	}

	/**
	 * Returns the charset used for encoding and decoding strings.
	 */
	public Charset charset() {
		return config.charset();
	}

	/**
	 * Returns fixed-layout operational support.
	 */
	public Supporter support(int count, boolean nul) {
		return new Supporter(this, nul, layout(count));
	}

	/**
	 * Returns an instance with specified charset byte order, if applicable.
	 */
	public StringType with(ByteOrder order) {
		var charset = Chars.apply(charset(), order);
		if (charset().equals(charset)) return this;
		return of(charset);
	}

	/**
	 * Returns the layout matching nul-terminator size.
	 */
	@Override
	public ValueLayout layout() {
		return config.layout();
	}

	/**
	 * Returns the nul-terminator.
	 */
	@Override
	public Terminator term() {
		return config.term();
	}

	/**
	 * Allocates memory with the encoded chars and optional nul-termination.
	 */
	public MemorySegment alloc(SegmentAllocator allocator, CharSequence s, boolean nul) {
		return alloc(allocator, s, 0, nul);
	}

	/**
	 * Allocates memory with the encoded chars and optional nul-termination.
	 */
	public MemorySegment alloc(SegmentAllocator allocator, CharSequence s, int index, boolean nul) {
		return alloc(allocator, s, index, Integer.MAX_VALUE, nul);
	}

	/**
	 * Allocates memory with the encoded chars and optional nul-termination.
	 */
	public MemorySegment alloc(SegmentAllocator allocator, CharSequence s, int index, int count,
		boolean nul) {
		if (allocator == null || s == null) return null;
		var buffer = Chars.encode(config.charset(), Buffers.CHAR.of(s, index, count));
		var memory = allocator.allocate(buffer.limit() + termSize(nul));
		int n = BufferType.BYTE.write(memory, buffer, false);
		if (nul) term().set(memory, n);
		return memory;
	}

	/**
	 * Decodes a string from the bound memory segment with optional nul-termination. Fails if the
	 * length is larger than int.
	 */
	public String get(MemorySegment memory, boolean nul) {
		return get(memory, 0L, nul);
	}

	/**
	 * Decodes a string from the bound memory segment with optional nul-termination. Fails if the
	 * length is larger than int.
	 */
	public String get(MemorySegment memory, long offset, boolean nul) {
		return get(memory, offset, Integer.MAX_VALUE, nul);
	}

	/**
	 * Decodes a string from the bound memory segment with optional nul-termination. Fails if the
	 * length is larger than int.
	 */
	public String get(MemorySegment memory, long offset, long length, boolean nul) {
		memory = slice(memory, offset, length, nul);
		if (Segments.isNull(memory)) return null;
		return Chars.decode(config.charset(), memory.asByteBuffer());
	}

	/**
	 * Copies encoded chars to memory within bounds and optional nul-termination. Returns the number
	 * of bytes written, including nul-terminator.
	 */
	public int write(MemorySegment memory, CharSequence s, boolean nul) {
		return write(memory, 0L, s, 0, nul);
	}

	/**
	 * Copies encoded chars to memory within bounds and optional nul-termination. Returns the number
	 * of bytes written, including nul-terminator.
	 */
	public int write(MemorySegment memory, long offset, CharSequence s, int index, boolean nul) {
		return write(memory, offset, Integer.MAX_VALUE, s, index, Integer.MAX_VALUE, nul);
	}

	/**
	 * Copies encoded chars to memory within bounds and optional nul-termination. Returns the number
	 * of bytes written, including nul-terminator.
	 */
	public int write(MemorySegment memory, long offset, long length, CharSequence s, int index,
		int count, boolean nul) {
		if (s == null || Segments.isNull(memory)) return 0;
		var chars = Buffers.CHAR.of(s, index, count);
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		return rawEncode(memory, offset, length, chars, nul);
	}

	/**
	 * Allocates memory and encodes leaves of a multi-dimensional string array, with optional
	 * nul-termination. Returns null if the array type is not supported.
	 */
	public MemorySegment deepAlloc(SegmentAllocator allocator, Object t, boolean nul) {
		int dims = dimsOf(t);
		if (allocator == null || dims < 0) return null;
		var buffers = Lists.<ByteBuffer>of();
		RawArray.<CharSequence>deepForEach(t, s -> buffers.add(Chars.encode(config.charset(), s)));
		return rawDeepAlloc(allocator, buffers, nul);
	}

	/**
	 * Creates a multi-dimensional string array by decoding memory into strings up to optional
	 * nul-terminators. Dimensions specify the array dimensions, count specifies the fixed or max
	 * size to decode, based on nul-termination.
	 */
	public <U> U deepGet(MemorySegment memory, Dimensions dims, int count, boolean nul) {
		return deepGet(memory, 0L, dims, count, nul);
	}

	/**
	 * Creates a multi-dimensional string array by decoding memory into strings up to optional
	 * nul-terminators. Dimensions specify the array dimensions, count specifies the fixed or max
	 * size to decode, based on nul-termination.
	 */
	public <U> U deepGet(MemorySegment memory, long offset, Dimensions dims, int count,
		boolean nul) {
		return deepGet(memory, offset, Long.MAX_VALUE, dims, count, nul);
	}

	/**
	 * Creates a multi-dimensional string array by decoding memory into strings up to optional
	 * nul-terminators. Dimensions specify the array dimensions, count specifies the fixed or max
	 * size to decode, based on nul-termination.
	 */
	public <U> U deepGet(MemorySegment memory, long offset, long length, Dimensions dims, int count,
		boolean nul) {
		memory = slice(memory, offset, length, false);
		if (memory == null) return null;
		U array = newArray(dims);
		rawDeepRead(memory, array, sizeInt(count), nul);
		return array;
	}

	/**
	 * Replaces leaves of a multi-dimensional string array by decoding memory up to optional
	 * nul-terminators. Returns the number of bytes processed. Returns 0 if the array type is not
	 * supported, or the object is not an array.
	 */
	public int deepRead(MemorySegment memory, Object t, int count, boolean nul) {
		return deepRead(memory, 0L, t, count, nul);
	}

	/**
	 * Replaces leaves of a multi-dimensional string array by decoding memory up to optional
	 * nul-terminators. Returns the number of bytes processed. Returns 0 if the array type is not
	 * supported, or the object is not an array.
	 */
	public int deepRead(MemorySegment memory, long offset, Object t, int count, boolean nul) {
		return deepRead(memory, offset, Long.MAX_VALUE, t, count, nul);
	}

	/**
	 * Replaces leaves of a multi-dimensional string array by decoding memory up to optional
	 * nul-terminators. Returns the number of bytes processed. Returns 0 if the array type is not
	 * supported, or the object is not an array.
	 */
	public int deepRead(MemorySegment memory, long offset, long length, Object t, int count,
		boolean nul) {
		memory = slice(memory, offset, length, false);
		if (memory == null || dimsOf(t) < 0) return 0;
		return rawDeepRead(memory, t, sizeInt(count), nul);
	}

	/**
	 * Encodes leaves of a multi-dimensional string array to memory with optional nul-terminators.
	 * Returns the number of bytes processed. Returns 0 if the array type is not supported, or the
	 * object is not an array.
	 */
	public int deepWrite(MemorySegment memory, Object t, boolean nul) {
		return deepWrite(memory, 0L, t, nul);
	}

	/**
	 * Encodes leaves of a multi-dimensional string array to memory with optional nul-terminators.
	 * Returns the number of bytes processed. Returns 0 if the array type is not supported, or the
	 * object is not an array.
	 */
	public int deepWrite(MemorySegment memory, long offset, Object t, boolean nul) {
		return deepWrite(memory, offset, Long.MAX_VALUE, t, nul);
	}

	/**
	 * Encodes leaves of a multi-dimensional string array to memory with optional nul-terminators.
	 * Returns the number of bytes processed. Returns 0 if the array type is not supported, or the
	 * object is not an array.
	 */
	public int deepWrite(MemorySegment memory, long offset, long length, Object t, boolean nul) {
		memory = slice(memory, offset, length, false);
		if (memory == null || dimsOf(t) < 0) return 0;
		return rawDeepWrite(memory, t, nul);
	}

	@Override
	public int hashCode() {
		return Objects.hash(charset());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return obj instanceof StringType s && Objects.equals(charset(), s.charset());
	}

	@Override
	public String toString() {
		return ToString.forClass(this, charset(), Layouts.desc(layout()));
	}

	// support

	private MemorySegment rawDeepAlloc(SegmentAllocator allocator, List<ByteBuffer> buffers,
		boolean nul) {
		long length = Streams.from(buffers).mapToLong(b -> Buffers.limit(b) + termSize(nul)).sum();
		var memory = allocator.allocate(length);
		var offset = 0L;
		for (var buffer : buffers)
			offset += rawWrite(memory, offset, buffer, nul);
		return memory;
	}

	private int rawEncode(MemorySegment memory, long offset, long length, CharBuffer chars,
		boolean nul) {
		var bytes = BufferType.BYTE.asBuffer(memory, offset, length - termSize(nul), false);
		int n = Chars.encode(config.charset(), chars, bytes);
		if (nul) n += config.term().set(memory, offset + n);
		return n;
	}

	private int rawWrite(MemorySegment memory, long offset, ByteBuffer buffer, boolean nul) {
		int n = BufferType.BYTE.write(memory, offset, buffer, false);
		if (nul) n += config.term().set(memory, offset + n);
		return n;
	}

	private int rawDeepRead(MemorySegment memory, Object t, int size, boolean nul) {
		var offset = Counter.of(0L);
		RawArray.<CharSequence, Object>deepReplace(t, _ -> {
			if (offset.get() >= memory.byteSize()) return "";
			var buffer = BufferType.asByte(slice(memory, offset.get(), size, nul));
			if (buffer == null) offset.set(memory.byteSize());
			else offset.inc(buffer.limit() + termSize(nul));
			return Basics.def(Chars.decode(config.charset(), buffer), "");
		});
		return Math.toIntExact(offset.get());
	}

	private int rawDeepWrite(MemorySegment memory, Object t, boolean nul) {
		var offset = Counter.of(0L);
		RawArray.<CharSequence>deepForEach(t, s -> {
			long length = memory.byteSize() - offset.get();
			if (length <= 0L) return;
			int n = rawEncode(memory, offset.get(), length, Buffers.CHAR.of(s), nul);
			offset.inc(n);
		});
		return Math.toIntExact(offset.get());
	}

	private int dimsOf(Object t) {
		var typed = Generics.typedClass(t).array();
		return CharSequence.class.isAssignableFrom(typed.cls()) ? typed.dimensions() : -1;
	}

	private <U> U newArray(Dimensions dims) {
		return Dimensions.create(dims, String.class);
	}

	private SequenceLayout layout(int count) {
		var layout = MemoryLayout.sequenceLayout(count * layoutSize(),
			Layouts.order(Layouts.BYTE, config.info().order().order));
		return Layouts.align(layout, layoutSize());
	}

	private static StringType init(Charset charset) {
		return new StringType(Config.of(charset));
	}

	private static Map<Charset, StringType> map() {
		return Immutable.convertMapOf(StringType::charset, t -> t, DEFAULT, ASCII, LATIN1, UTF8,
			UTF16, UTF32);
	}
}
