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
import java.util.Map;
import java.util.Objects;
import ceri.common.collect.Immutable;
import ceri.common.io.Buffers;
import ceri.common.io.Direction;
import ceri.common.math.Maths;
import ceri.common.text.Chars;
import ceri.common.text.Strings;
import ceri.common.text.ToString;
import ceri.ffm.core.Decoder;
import ceri.ffm.core.Encoder;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;
import ceri.ffm.core.Terminator;
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

	public static void main(String[] args) {
		String[] ss = { "abcdef", "g", "", "hijk", null, "lmnop", "" };
		var s = UTF16.support(10, true);
		var r = s.encodeAll(Direction.duplex, true, ss);
		var m = r.value();
		FfmTesting.bin(r.value());
		FfmTesting.arg(ss);
		Primitive.CHAR.writeAll(m, 4, false, 'C', 'D', 'E');
		r.resolve();
		FfmTesting.arg(ss);
		// FfmTesting.arg(s.decodeArray(m, 10, false));
	}

	/**
	 * Charset and layout configuration.
	 */
	private static record Config(Charset charset, Chars.Info info, Terminator term,
		ValueLayout layout) {
		private static Config of(Charset charset) {
			var info = Chars.Info.of(charset);
			var term = Terminator.of(info.term().length());
			var layout = Layouts.order(Layouts.ofInt(term.size()), info.order().order);
			return new Config(charset, info, term, layout);
		}

		private Config align(long align) {
			align = Layouts.elementAlign(layout(), align);
			var layout = Layouts.align(layout(), align);
			return layout == layout() ? this : new Config(charset(), info(), term(), layout());
		}

		private Config order(ByteOrder order) {
			var charset = Chars.apply(charset(), order);
			if (charset().equals(charset)) return this;
			return of(charset).align(layout().byteAlignment());
		}
	}

	/**
	 * Operational string support with bounded size and optional nul-termination.
	 */
	public static class Supporter extends Support.Typed<String, SequenceLayout> {
		private final StringType string;
		private final boolean nul;

		private Supporter(StringType string, boolean nul, SequenceLayout layout) {
			super(layout);
			this.string = string;
			this.nul = nul;
		}

		@Override
		public Native.Kind kind() {
			return Native.Kind.string;
		}

		/**
		 * Returns the maximum char count, including nul-terminator if specified.
		 */
		public int length() {
			return (int) string.count(layoutSize());
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
		public Class<String> type() {
			return String.class;
		}

		@Override
		public String typeDesc() {
			return arrayDesc(string.charset().name(), length(), nul());
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
		public Supporter align(long align) {
			return create(string.align(align), align);
		}

		@Override
		public Supporter order(ByteOrder order) {
			return create(string.order(order), layout().byteAlignment());
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

		// shared

		@Override
		protected String rawGet(MemorySegment memory, long offset, long length) {
			return string.get(memory, offset, length, nul);
		}

		@Override
		protected void rawWrite(MemorySegment memory, long offset, long length, String value) {
			string.write(memory, offset, length, value, 0, Integer.MAX_VALUE, nul);
		}

		@Override
		protected void encode(Encoder encoder, String value) {
			var buffer = buffer(value);
			encoder.accept(encoder.in() ? (m, o, _) -> write(m, o, buffer) : null, null,
				length(buffer));
		}

		@Override
		protected String decode(Decoder decoder, long length) {
			length = Math.min(length, layoutSize());
			var memory = string.slice(decoder.memory(), decoder.offset(), length, nul);
			if (Segments.isNull(memory)) return decodeNoVal(decoder, length);
			var buffer = memory.asByteBuffer();
			var value = string.decode(buffer);
			decoder.inc(buffer.position() + string.termSize(nul));
			return value;
		}

		@Override
		protected void encodeArray(Encoder encoder, String[] array, int index, int count,
			boolean nul) {
			encodeDynamicArray(encoder, array, index, count, nul);
		}

		@Override
		protected String[] decodeArray(Decoder decoder, long length, int count, boolean nul) {
			return decodeDynamicArray(decoder, length, count, nul);
		}

		@Override
		protected int encodeTermSize() {
			return string.layoutSize() * (nul() ? 1 : length());
		}

		// support

		private Supporter create(StringType string, long align) {
			var layout = string == this.string ? layout() : string.layout(length());
			layout = Layouts.align(layout, align);
			if (string == this.string && layout == layout()) return this;
			return new Supporter(string, nul, layout);
		}

		private ByteBuffer buffer(CharSequence value) {
			int count = Math.min(Strings.length(value), count());
			return string.encode(value, 0, count);
		}

		private long length(ByteBuffer buffer) {
			return string.length(buffer, nul());
		}

		private long write(MemorySegment memory, long offset, ByteBuffer buffer) {
			return string.write(memory, offset, buffer, nul());
		}
	}

	/**
	 * Returns fixed-layout operational support.
	 */
	static Supporter supportFor(Charset charset, int count, boolean nul) {
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
	 * Returns fixed-layout operational support. Count includes the nul-terminator if specified.
	 */
	public Supporter support(int count, boolean nul) {
		return new Supporter(this, nul, layout(count));
	}

	/**
	 * Returns an instance with specified layout alignment.
	 */
	public StringType align(long align) {
		var config = this.config.align(align); // config aligns as an element
		return config == this.config ? this : new StringType(config);
	}

	/**
	 * Returns an instance with specified charset byte order, if applicable.
	 */
	public StringType order(ByteOrder order) {
		var config = this.config.order(order);
		return config == this.config ? this : new StringType(config);
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
	 * Encodes the string to a new buffer.
	 */
	public ByteBuffer encode(CharSequence s) {
		return encode(s, 0);
	}

	/**
	 * Encodes the string to a new buffer.
	 */
	public ByteBuffer encode(CharSequence value, int index) {
		return encode(value, index, Integer.MAX_VALUE);
	}

	/**
	 * Encodes the string to a new buffer.
	 */
	public ByteBuffer encode(CharSequence value, int index, int count) {
		return Chars.encode(charset(), Buffers.CHAR.of(value, index, count));
	}

	/**
	 * Encodes the string to the buffer, and returns the number of bytes written.
	 */
	public int encode(CharSequence s, ByteBuffer buffer) {
		return encode(s, 0, buffer);
	}

	/**
	 * Encodes the string to the buffer within bounds, and returns the number of bytes written.
	 */
	public int encode(CharSequence s, int index, ByteBuffer buffer) {
		return encode(s, index, Integer.MAX_VALUE, buffer);
	}

	/**
	 * Encodes the string to the buffer within bounds, and returns the number of bytes written.
	 */
	public int encode(CharSequence s, int index, int count, ByteBuffer buffer) {
		return Chars.encode(config.charset(), s, index, count, buffer);
	}

	/**
	 * Decodes the buffer to a string.
	 */
	public String decode(ByteBuffer buffer) {
		return Chars.decode(config.charset(), buffer);
	}

	/**
	 * Allocates memory with the encoded chars and optional nul-termination.
	 */
	public MemorySegment alloc(CharSequence s, boolean nul) {
		return alloc(s, 0, nul);
	}

	/**
	 * Allocates memory with the encoded chars and optional nul-termination.
	 */
	public MemorySegment alloc(CharSequence s, int index, boolean nul) {
		return alloc(s, index, Integer.MAX_VALUE, nul);
	}

	/**
	 * Allocates memory with the encoded chars and optional nul-termination.
	 */
	public MemorySegment alloc(CharSequence s, int index, int count, boolean nul) {
		return alloc(Segments.auto(), s, index, count, nul);
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
		var buffer = encode(Buffers.CHAR.of(s, index, count));
		var memory = allocate(allocator, buffer.limit() + termSize(nul));
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
		return decode(memory.asByteBuffer());
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

	private int rawEncode(MemorySegment memory, long offset, long length, CharBuffer chars,
		boolean nul) {
		var bytes = BufferType.BYTE.asBuffer(memory, offset, length - termSize(nul), false);
		int n = encode(chars, bytes);
		if (nul) n += config.term().set(memory, offset + n);
		return n;
	}

	private SequenceLayout layout(int count) {
		var layout = MemoryLayout.sequenceLayout(count * layoutSize(),
			Layouts.order(Layouts.BYTE, config.info().order().order));
		return Layouts.align(layout, layout().byteAlignment());
	}

	private long length(ByteBuffer buffer, boolean nul) {
		return Buffers.limit(buffer) + termSize(nul);
	}

	private long write(MemorySegment memory, long offset, ByteBuffer buffer, boolean nul) {
		int n = BufferType.BYTE.write(memory, offset, buffer, false);
		if (nul) term().set(memory, offset + n);
		return offset + length(buffer, nul);
	}

	private static StringType init(Charset charset) {
		return new StringType(Config.of(charset));
	}

	private static Map<Charset, StringType> map() {
		return Immutable.convertMapOf(StringType::charset, t -> t, DEFAULT, ASCII, LATIN1, UTF8,
			UTF16, UTF32);
	}
}
