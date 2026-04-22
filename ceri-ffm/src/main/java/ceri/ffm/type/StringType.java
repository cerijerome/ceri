package ceri.ffm.type;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import ceri.common.array.Dimensions;
import ceri.common.array.RawArray;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.io.Buffers;
import ceri.common.math.Maths;
import ceri.common.reflect.Generics;
import ceri.common.stream.Streams;
import ceri.common.text.Chars;
import ceri.common.util.Basics;
import ceri.common.util.Counter;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Segments;
import ceri.ffm.test.FfmTesting;

/**
 * Handler for nul-terminated strings with any encoding. Can be used for byte and wchar arrays by
 * choosing a compatible charset.
 */
public class StringType implements Layouts.Provider<ValueLayout> {
	public static final StringType DEFAULT = new StringType(Charset.defaultCharset());
	public static final StringType ASCII = new StringType(StandardCharsets.US_ASCII);
	public static final StringType ISO88591 = new StringType(StandardCharsets.ISO_8859_1);
	public static final StringType UTF8 = new StringType(Chars.UTF8);
	public static final StringType UTF16 = new StringType(Chars.UTF16);
	public static final StringType UTF32 = new StringType(Chars.UTF32);
	private static final Map<Charset, StringType> MAP = map();
	private final Charset charset;
	private final Terminator term;

	// Wait until use cases:
	// - deep alloc/write min/max/exact (!)nul
	// - deep get/read min/max/exact (!)nul

	public static void main(String[] args) {
		var bb = ByteBuffer.allocate(16);
		Chars.encode(Chars.UTF16, "test", bb);
		System.out.println(bb.position());
		System.out.println(bb.limit());

		StringType ss = StringType.of(Chars.UTF16);
		var a = new String[][] { { "abcde", "fgh", "" }, { "ij" }, {}, { "k", null } };
		System.out.println(RawArray.toString(a));
		var m = ss.deepAlloc(Segments.auto(), a, true);
		FfmTesting.bin(m);
		a = ss.deepGet(m, Dimensions.from(a), 8, true);
		System.out.println(RawArray.toString(a));
	}

	/**
	 * Returns an instance for the charset.
	 */
	public static StringType of(Charset charset) {
		charset = Chars.safe(charset);
		var string = MAP.get(charset);
		return string != null ? string : new StringType(charset);
	}

	private StringType(Charset charset) {
		this.charset = Chars.safe(charset);
		var info = Chars.Info.of(this.charset);
		term = Terminator.of(info.term().length());
	}

	/**
	 * Returns the charset used for encoding and decoding strings.
	 */
	public Charset charset() {
		return charset;
	}

	/**
	 * Returns the layout matching nul-terminator size.
	 */
	@Override
	public ValueLayout layout() {
		return Layouts.ofInt(layoutSize());
	}

	@Override
	public int layoutSize() {
		return term.size();
	}

	/**
	 * Returns the nul-terminator.
	 */
	@Override
	public Terminator term() {
		return term;
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
		var buffer = Chars.encode(charset, Buffers.CHAR.of(s, index, count));
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
		if (memory == null) return null;
		return Chars.decode(charset, memory.asByteBuffer());
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
		RawArray.<CharSequence>deepForEach(t, s -> buffers.add(Chars.encode(charset, s)));
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
		int n = Chars.encode(charset, chars, bytes);
		if (nul) n += term.set(memory, offset + n);
		return n;
	}

	private int rawWrite(MemorySegment memory, long offset, ByteBuffer buffer, boolean nul) {
		int n = BufferType.BYTE.write(memory, offset, buffer, false);
		if (nul) n += term.set(memory, offset + n);
		return n;
	}

	private int rawDeepRead(MemorySegment memory, Object t, int size, boolean nul) {
		var offset = Counter.of(0L);
		RawArray.<CharSequence, Object>deepReplace(t, _ -> {
			if (offset.get() >= memory.byteSize()) return "";
			var buffer = BufferType.asByte(slice(memory, offset.get(), size, nul));
			if (buffer == null) offset.set(memory.byteSize());
			else offset.inc(buffer.limit() + termSize(nul));
			return Basics.def(Chars.decode(charset, buffer), "");
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
		var typed = Generics.Typed.from(t).array();
		return CharSequence.class.isAssignableFrom(typed.cls()) ? typed.dimensions() : -1;
	}

	private <U> U newArray(Dimensions dims) {
		return Dimensions.create(dims, String.class);
	}

	private static Map<Charset, StringType> map() {
		return Immutable.convertMapOf(StringType::charset, t -> t, DEFAULT, ASCII, ISO88591, UTF8,
			UTF16, UTF32);
	}
}
