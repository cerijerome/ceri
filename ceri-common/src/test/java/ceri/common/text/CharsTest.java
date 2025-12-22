package ceri.common.text;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.data.ByteProvider;
import ceri.common.data.Bytes;
import ceri.common.test.Assert;

public class CharsTest {
	private static final char ch0 = 0;
	private static final Charset UTF8 = StandardCharsets.UTF_8;
	private StringBuilder b;

	@After
	public void after() {
		b = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Chars.class);
		Assert.privateConstructor(Chars.Escape.class);
	}

	@Test
	public void testCharset() {
		Assert.same(Chars.charset(null), Charset.defaultCharset());
		Assert.same(Chars.charset(""), Charset.defaultCharset());
		Assert.same(Chars.charset("UTF-8"), StandardCharsets.UTF_8);
	}

	@Test
	public void testCharsetInfo() {
		Assert.equal(Chars.Info.of(StandardCharsets.US_ASCII),
			new Chars.Info(ByteProvider.of(), ByteProvider.of(0), 1.0f));
		Assert.equal(Chars.Info.of(StandardCharsets.UTF_8),
			new Chars.Info(ByteProvider.of(), ByteProvider.of(0), 1.1f));
		Assert.equal(Chars.Info.of(StandardCharsets.UTF_16),
			new Chars.Info(ByteProvider.of(0xfe, 0xff), ByteProvider.of(0, 0), 2.0f));
		Assert.equal(Chars.Info.of(StandardCharsets.UTF_32),
			new Chars.Info(ByteProvider.of(), ByteProvider.of(0, 0, 0, 0), 4.0f));
		Assert.equal(Chars.Info.from(testCharset(1.2f, 2.0f, 0x22, 0x44)), // x-IBM300 encoding
			new Chars.Info(ByteProvider.of(), ByteProvider.of(0), 1.2f));
		Assert.equal(Chars.Info.from(testCharset(1.1f, 4.0f, 0xef, 0xbb, 0xbf, 0)), // UTF BOM
			new Chars.Info(ByteProvider.of(0xef, 0xbb, 0xbf), ByteProvider.of(0), 1.1f));
	}

	@Test
	public void testCompactName() {
		Assert.string(Chars.compactName(null), "");
		Assert.string(Chars.compactName(StandardCharsets.ISO_8859_1), "iso88591");
	}

	@Test
	public void testSafeCharset() {
		Assert.same(Chars.safe((Charset) null), Charset.defaultCharset());
		Assert.same(Chars.safe(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
	}

	@Test
	public void testSafeCharSequence() {
		Assert.string(Chars.safe((String) null), "");
		Assert.same(Chars.safe(b("")), b);
	}

	@Test
	public void testAt() {
		Assert.equal(Chars.at(null, 0), null);
		Assert.equal(Chars.at("", 0), null);
		Assert.equal(Chars.at("test", -1), null);
		Assert.equal(Chars.at("test", 4), null);
		Assert.equal(Chars.at("test", 0), 't');
		Assert.equal(Chars.at(null, 0, 'x'), 'x');
		Assert.equal(Chars.at("", 0, 'x'), 'x');
		Assert.equal(Chars.at("test", -1, 'x'), 'x');
		Assert.equal(Chars.at("test", 4, 'x'), 'x');
		Assert.equal(Chars.at("test", 2, 'x'), 's');
	}

	@Test
	public void testLast() {
		Assert.equal(Chars.last(null), null);
		Assert.equal(Chars.last(""), null);
		Assert.equal(Chars.last("test\0"), '\0');
		Assert.equal(Chars.last(null, 'a'), 'a');
		Assert.equal(Chars.last("", 'a'), 'a');
		Assert.equal(Chars.last("test\0", 'a'), '\0');
	}

	@Test
	public void testLower() {
		Assert.string(Chars.lower(null), "");
		Assert.string(Chars.lower(""), "");
		Assert.string(Chars.lower("_Aa\u03a9\u03c9"), "_aa\u03c9\u03c9");
	}

	@Test
	public void testUpper() {
		Assert.string(Chars.upper(null), "");
		Assert.string(Chars.upper(""), "");
		Assert.string(Chars.upper("_Aa\u03a9\u03c9"), "_AA\u03a9\u03a9");
	}

	@Test
	public void testEquals() {
		Assert.equal(Chars.equals(null, 0, null, 0), false);
		Assert.equal(Chars.equals(null, 0, "", 0), false);
		Assert.equal(Chars.equals("", 0, null, 0), false);
		Assert.equal(Chars.equals("", 0, "", 0), false);
		Assert.equal(Chars.equals("a", -1, "a", 0), false);
		Assert.equal(Chars.equals("a", 1, "a", 0), false);
		Assert.equal(Chars.equals("a", 0, "a", -1), false);
		Assert.equal(Chars.equals("a", 0, "a", 1), false);
		Assert.equal(Chars.equals("abc", 1, "abc", 1), true);
		Assert.equal(Chars.equals("abc", 1, "aBc", 1), false);
	}

	@Test
	public void testIsPrintable() {
		Assert.equal(Chars.isPrintable(null, 0), false);
		Assert.equal(Chars.isPrintable("A", 0), true);
		Assert.equal(Chars.isPrintable("A", 1), false);
		Assert.equal(Chars.isPrintable(Chars.NUL), false);
		Assert.equal(Chars.isPrintable('A'), true);
		Assert.equal(Chars.isPrintable('\u1c00'), true);
		Assert.equal(Chars.isPrintable(Character.MAX_HIGH_SURROGATE), true);
		Assert.equal(Chars.isPrintable('\uffff'), false);
	}

	@Test
	public void testIsNameBoundary() {
		Assert.equal(Chars.isNameBoundary(ch0, ch0), false);
		Assert.equal(Chars.isNameBoundary(ch0, 'a'), true);
		Assert.equal(Chars.isNameBoundary(ch0, 'A'), true);
		Assert.equal(Chars.isNameBoundary(ch0, '1'), true);
		Assert.equal(Chars.isNameBoundary(ch0, '_'), false);
		Assert.equal(Chars.isNameBoundary('a', ch0), true);
		Assert.equal(Chars.isNameBoundary('a', 'a'), false);
		Assert.equal(Chars.isNameBoundary('a', 'A'), true);
		Assert.equal(Chars.isNameBoundary('a', '1'), true);
		Assert.equal(Chars.isNameBoundary('a', '_'), true);
		Assert.equal(Chars.isNameBoundary('A', ch0), true);
		Assert.equal(Chars.isNameBoundary('A', 'a'), false);
		Assert.equal(Chars.isNameBoundary('A', 'A'), false);
		Assert.equal(Chars.isNameBoundary('A', '1'), true);
		Assert.equal(Chars.isNameBoundary('A', '_'), true);
		Assert.equal(Chars.isNameBoundary('1', ch0), true);
		Assert.equal(Chars.isNameBoundary('1', 'a'), true);
		Assert.equal(Chars.isNameBoundary('1', 'A'), true);
		Assert.equal(Chars.isNameBoundary('1', '1'), false);
		Assert.equal(Chars.isNameBoundary('1', '_'), true);
		Assert.equal(Chars.isNameBoundary('_', ch0), false);
		Assert.equal(Chars.isNameBoundary('_', 'a'), true);
		Assert.equal(Chars.isNameBoundary('_', 'A'), true);
		Assert.equal(Chars.isNameBoundary('_', '1'), true);
		Assert.equal(Chars.isNameBoundary('_', '_'), false);
	}

	@Test
	public void testEscape() {
		Assert.string(Chars.escape(ch0), "\\0");
		Assert.string(Chars.escape('\\'), "\\\\");
		Assert.string(Chars.escape('\177'), "\\u007f");
		Assert.string(Chars.escape('a'), "a");
		Assert.string(Chars.escape(null), "");
		Assert.string(Chars.escape("\\ \b \u001b \f \r \n \t \0 \1 \177 \377 a"),
			"\\\\ \\b \\e \\f \\r \\n \\t \\0 \\u0001 \\u007f \377 a");
		var s = "abc ";
		Assert.same(Chars.escape(s), s);
	}

	@Test
	public void testUnescape() {
		Assert.string(Chars.unescape(null), "");
		Assert.string(Chars.unescape( //
			"\\\\ \\b \\e \\f \\r \\n \\t \\0 \\000 \\u0001 \\u007f \377 a \\x00 \\xff"),
			"\\ \b \u001b \f \r \n \t \0 \0 \1 \177 \377 a \0 \377");
		Assert.string(Chars.unescape("\\\\\\b\\e\\f\\r\\n\\t\\0\\000\\u0001\\u007f\377a\\x00\\xff"),
			"\\\b\u001b\f\r\n\t\0\0\1\177\377a\0\377");
		Assert.string(Chars.unescape("\\x\\x0"), "\\x\\x0");
		Assert.string(Chars.unescape("\\8\\18\\378\\477"), "\\8\u00018\u001f8\u00277");
		Assert.string(Chars.unescape("\\u\\u0\\u00\\u000"), "\\u\\u0\\u00\\u000");
		var s = "abc ";
		Assert.same(Chars.unescape(s), s);
	}

	@Test
	public void testEncodeCharset() {
		var s = "\0A\u00a9\u2103\ud835\udc00";
		Assert.equals(Chars.encode(s, UTF8, null, 3), 0);
		var bytes = new byte[12];
		Assert.equals(Chars.encode(s, UTF8, bytes, 12, 5), 0);
		Assert.equals(Chars.encode(s, UTF8, bytes), 11);
		Assert.array(bytes, 0, 'A', 0xc2, 0xa9, 0xe2, 0x84, 0x83, 0xf0, 0x9d, 0x90, 0x80, 0);
		Array.bytes.fill(bytes, 0);
		Assert.equals(Chars.encode(s, UTF8, bytes, 2), 7);
		Assert.array(bytes, 0, 0, 0, 'A', 0xc2, 0xa9, 0xe2, 0x84, 0x83, 0, 0, 0);
	}

	@Test
	public void testEncodeCharsetToBuffer() {
		var s = "\0A\u00a9\u2103\ud835\udc00";
		Assert.equals(Chars.encode(s, UTF8, (ByteBuffer) null), 0);
		Assert.equals(Chars.encode(s, UTF8, ByteBuffer.wrap(new byte[0])), 0);
	}

	@Test
	public void testDecodeCharset() {
		var bytes = Array.bytes.of(0, 'A', 0xc2, 0xa9, 0xe2, 0x84, 0x83, 0xf0, 0x9d, 0x90, 0x80, 0);
		Assert.string(Chars.decode(UTF8, (byte[]) null), "");
		Assert.string(Chars.decode(UTF8, bytes, 13, 5), "");
		Assert.string(Chars.decode(UTF8, bytes), "\0A\u00a9\u2103\ud835\udc00\0");
		Assert.string(Chars.decode(UTF8, bytes, 3), "\ufffd\u2103\ud835\udc00\0");
		Assert.string(Chars.decode(UTF8, bytes, 5), "\ufffd\ufffd\ud835\udc00\0");
		Assert.string(Chars.decode(UTF8, bytes, 7), "\ud835\udc00\0");
		Assert.string(Chars.decode(UTF8, bytes, 7, 3), "\ufffd");
	}

	private StringBuilder b(String s) {
		b = new StringBuilder(s);
		return b;
	}

	private static Charset testCharset(float averageBytes, float maxBytes, int... nulEnc) {
		var nulBytes = Array.bytes.of(nulEnc);
		return new Charset("test", new String[0]) {
			@Override
			public boolean contains(Charset cs) {
				return false;
			}

			@Override
			public CharsetDecoder newDecoder() {
				return new Decoder(this);
			}

			@Override
			public CharsetEncoder newEncoder() {
				return new Encoder(this);
			}

			class Decoder extends CharsetDecoder {
				public Decoder(Charset cs) {
					super(cs, averageBytes, maxBytes);
				}

				@Override
				protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
					var s = new String(Bytes.bytes(in));
					out.put(s.toCharArray());
					return CoderResult.UNDERFLOW;
				}
			}

			class Encoder extends CharsetEncoder {
				public Encoder(Charset cs) {
					super(cs, averageBytes, maxBytes);
				}

				@Override
				protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
					var s = new String(Chars.chars(in));
					if (s.equals("\0")) out.put(nulBytes);
					else out.put(s.getBytes());
					return CoderResult.UNDERFLOW;
				}
			}
		};
	}
}
