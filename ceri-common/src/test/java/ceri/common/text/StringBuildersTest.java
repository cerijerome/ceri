package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertString;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.PrimitiveIterator;
import org.junit.After;
import org.junit.Test;
import ceri.common.stream.IntStream;
import ceri.common.stream.Streams;
import ceri.common.util.CloseableUtil;

public class StringBuildersTest {
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
		CloseableUtil.close(p, o);
		b = null;
		p = null;
		o = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(StringBuilders.class);
	}

	@Test
	public void testClear() {
		assertEquals(StringBuilders.clear(null), null);
		assertString(StringBuilders.clear(b(S)), "");
	}

	@Test
	public void testAppendCodePointStream() {
		assertEquals(StringBuilders.append(null, Streams.ints(0)), null);
		assertString(StringBuilders.append(b(), nullIntStream), "");
		assertString(StringBuilders.append(b(), Streams.ints(0, _1B, _2B, _3B, _4B)),
			"\0A\u00a9\u2103\ud835\udc00");
	}

	@Test
	public void testAppendCodePointIterator() {
		assertEquals(StringBuilders.append(null, Streams.ints(0).iterator()), null);
		assertString(StringBuilders.append(b(), nullIntIterator), "");
		assertString(StringBuilders.append(b(), Streams.ints(0, _1B, _2B, _3B, _4B).iterator()),
			"\0A\u00a9\u2103\ud835\udc00");
	}

	@Test
	public void testFormat() {
		b = StringBuilders.format(null);
		b = StringBuilders.format("", (Object[]) null);
		b = StringBuilders.format("%.2f", 3.3333);
		b = StringBuilders.format(b, " %.2f"); // no args, string not processed
		assertString(b, "3.33 %.2f");
	}

	@Test
	public void testRepeat() {
		assertEquals(StringBuilders.repeat(null, '\0', 1), null);
		assertString(StringBuilders.repeat(b(), '\0', -1), "");
		assertString(StringBuilders.repeat(b(), '\0', 1), "\0");
		assertEquals(StringBuilders.repeat(null, "abc", 1), null);
		assertString(StringBuilders.repeat(b(), null, 1), "");
		assertString(StringBuilders.repeat(b(), "abc", -1), "");
		assertString(StringBuilders.repeat(b(), "abc", 1), "abc");
		assertString(StringBuilders.repeat(b(), "abc", 3), "abcabcabc");
	}

	@Test
	public void testSub() {
		assertString(StringBuilders.sub(null, 1), "");
		assertString(StringBuilders.sub(null, 1, 1), "");
		assertString(StringBuilders.sub(b(), 1), "");
		assertString(StringBuilders.sub(b(S), 2, 2), "\u00a9\u2103");
		assertString(StringBuilders.sub(b(S), 5), "\udc00");
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
		assertString(b, "\0" + S + "\b\t\f\'\"" + Strings.EOL);
	}

	@Test
	public void testOutputStream() throws IOException {
		o = StringBuilders.outputStream(null);
		o.write(0);
		o = StringBuilders.outputStream(b());
		o.write(0);
		o.write(S.getBytes(StandardCharsets.UTF_8));
		o.close();
		assertString(b, "\0" + S);
	}

	private StringBuilder b(String s) {
		return b().append(s);
	}

	private StringBuilder b() {
		b = new StringBuilder();
		return b;
	}
}
