package ceri.common.text;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.PrimitiveIterator;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.stream.IntStream;
import ceri.common.stream.Streams;
import ceri.common.test.Assert;

public class StringBuildersTest {
	private static final String nullString = null;
	private static final IntStream<RuntimeException> nullIntStream = null;
	private static final PrimitiveIterator.OfInt nullIntIterator = null;
	private static final int _1B = 'A';
	private static final int _2B = 0xa9; // copyright: UTF16=00a9, UTF8=c2+a9
	private static final int _3B = 0x2103; // degree celsius: UTF16=2103, UTF8=e2+84+83
	private static final int _4B = 0x1d400; // bold A: UTF16=d835+dc00, UTF8=f0+9d+90+80
	private static final String S = "\0A\u00a9\u2103\ud835\udc00";
	private StringBuilder b;
	private PrintStream p;
	private OutputStream o;

	@After
	public void after() {
		Closeables.close(p, o);
		b = null;
		p = null;
		o = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(StringBuilders.class);
	}

	@Test
	public void testStateUnchanged() {
		var s = "abc";
		Assert.same(state(s, null).toString(), "");
		Assert.same(state(s, null).append(2, 'c').toString(), s);
		Assert.same(state(s, null).append(3, "").toString(), s);
		Assert.same(state(s, null).append(0, "abc").toString(), s);
		Assert.same(state(s, null).append(1, "bc").toString(), s);
		Assert.same(state(s, null).append(2, 2).toString(), s);
		Assert.same(state(s, null).append(2, 2).append(2, 'c').toString(), s);
	}

	@Test
	public void testStateAppend() {
		var s = "abc";
		Assert.string(state(s, null).append(1, 'B'), "aB");
		Assert.string(state(s, null).append(3, "d"), "abcd");
		Assert.string(state(s, null).append(1, "Bc"), "aBc");
	}

	@Test
	public void testStateWrapped() {
		var s = "abc";
		Assert.string(state(s, ""), "");
		Assert.string(state(s, "").append(0, "abc"), "abc");
	}

	@Test
	public void testSafe() {
		Assert.string(StringBuilders.safe(null), "");
		Assert.string(StringBuilders.safe(new StringBuilder("abc")), "abc");
	}

	@Test
	public void testClear() {
		Assert.equal(StringBuilders.clear(null), null);
		Assert.string(StringBuilders.clear(b(S)), "");
	}

	@Test
	public void testAppendCodePointStream() {
		Assert.equal(StringBuilders.append(null, Streams.ints(0)), null);
		Assert.string(StringBuilders.append(b(), nullIntStream), "");
		Assert.string(StringBuilders.append(b(), Streams.ints(0, _1B, _2B, _3B, _4B)),
			"\0A\u00a9\u2103\ud835\udc00");
	}

	@Test
	public void testAppendCodePointIterator() {
		Assert.equal(StringBuilders.append(null, Streams.ints(0).iterator()), null);
		Assert.string(StringBuilders.append(b(), nullIntIterator), "");
		Assert.string(StringBuilders.append(b(), Streams.ints(0, _1B, _2B, _3B, _4B).iterator()),
			"\0A\u00a9\u2103\ud835\udc00");
	}

	@Test
	public void testAppendCharSequence() {
		Assert.equal(StringBuilders.append(null, ""), null);
		Assert.string(StringBuilders.append(b(), nullString), "");
		Assert.string(StringBuilders.append(b(), "abc"), "abc");
		Assert.string(StringBuilders.append(b(), "abc", 2), "c");
	}

	@Test
	public void testFormat() {
		b = StringBuilders.format(null);
		b = StringBuilders.format("", (Object[]) null);
		b = StringBuilders.format("%.2f", 3.3333);
		b = StringBuilders.format(b, " %.2f"); // no args, string not processed
		Assert.string(b, "3.33 %.2f");
	}

	@Test
	public void testRepeat() {
		Assert.equal(StringBuilders.repeat(null, '\0', 1), null);
		Assert.string(StringBuilders.repeat(b(), '\0', -1), "");
		Assert.string(StringBuilders.repeat(b(), '\0', 1), "\0");
		Assert.equal(StringBuilders.repeat(null, "abc", 1), null);
		Assert.string(StringBuilders.repeat(b(), null, 1), "");
		Assert.string(StringBuilders.repeat(b(), "abc", -1), "");
		Assert.string(StringBuilders.repeat(b(), "abc", 1), "abc");
		Assert.string(StringBuilders.repeat(b(), "abc", 3), "abcabcabc");
	}

	@Test
	public void testTrim() {
		Assert.equal(StringBuilders.trim(null), null);
		Assert.string(StringBuilders.trim(b()), "");
		Assert.string(StringBuilders.trim(b("\0\n\r \t")), "");
		Assert.string(StringBuilders.trim(b("\0\na b\nc")), "a b\nc");
		Assert.string(StringBuilders.trim(b("a b\nc\r \t")), "a b\nc");
		Assert.string(StringBuilders.trim(b("\0\na b\nc\r \t")), "a b\nc");
	}

	@Test
	public void testSub() {
		Assert.string(StringBuilders.sub(null, 1), "");
		Assert.string(StringBuilders.sub(null, 1, 1), "");
		Assert.string(StringBuilders.sub(b(), 1), "");
		Assert.string(StringBuilders.sub(b(S), 2, 2), "\u00a9\u2103");
		Assert.string(StringBuilders.sub(b(S), 5), "\udc00");
	}

	@Test
	public void testPrintStream() {
		p = StringBuilders.printStream(null);
		p.append(S);
		p = StringBuilders.printStream(b(), StandardCharsets.UTF_8);
		p.write(0);
		p.print(S);
		p.println("\b\t\f\'\"");
		p.close();
		Assert.string(b, "\0" + S + "\b\t\f\'\"" + Strings.EOL);
	}

	@Test
	public void testOutputStream() throws IOException {
		o = StringBuilders.outputStream(null);
		o.write(0);
		o = StringBuilders.outputStream(b());
		o.write(0);
		o.write(S.getBytes(StandardCharsets.UTF_8));
		o.close();
		Assert.string(b, "\0" + S);
	}

	private StringBuilder b(String s) {
		return b().append(s);
	}

	private StringBuilder b() {
		b = new StringBuilder();
		return b;
	}

	private StringBuilders.State state(String s, String b) {
		return StringBuilders.State.wrap(s, b == null ? null : b(b));
	}
}
