package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertString;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import ceri.common.io.ConsoleInput.Config;
import ceri.common.test.TestInputStream;
import ceri.common.text.StringUtil;
import ceri.common.util.CloseableUtil;

public class ConsoleInputBehavior {
	private static final Config CONF = new Config(0, false, null, null);
	private static final String U = "\u001b[A";
	private static final String D = "\u001b[B";
	private static final String R = "\u001b[C";
	private static final String L = "\u001b[D";
	private static final String ctrlA = "\u0001";
	private static final String ctrlE = "\u0005";
	private static final String DEL_L = "\u007f";
	private static final String DEL_R = "\u001b[3~";
	private Reader in;
	private PrintStream ps;
	private StringBuilder out;
	private ConsoleInput con;

	@After
	public void after() {
		CloseableUtil.close(ps, in);
		in = null;
		ps = null;
		con = null;
	}

	@Test
	public void shouldCreateWithDefaultConfig() throws IOException {
		init(CONF, List.of(), "test\n");
		con = ConsoleInput.of(in, ps);
		assertEquals(con.ready(), true);
		assertEquals(con.readLine(), "test");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldPollForInput() throws IOException {
		var tin = TestInputStream.of();
		tin.available.autoResponses(0, 0, 1);
		tin.to.writeString("abc\n");
		var out = IoUtil.nullPrintStream();
		in = new InputStreamReader(tin);
		con = ConsoleInput.of(in, out, new Config(0, false, null, 0));
		assertEquals(con.readLine(), "abc");
	}

	@Test
	public void shouldInsertChars() throws IOException {
		init(CONF, List.of(), "abc", L, "\b", "12", R, "3\n");
		assertEquals(con.readLine(), "a12b3c");
	}

	@Test
	public void shouldDeleteChars() throws IOException {
		init(CONF, List.of(), "abcde", DEL_L, L, L, L, DEL_L, R, DEL_R, "\n");
		assertEquals(con.readLine(), "bd");
	}

	@Test
	public void shouldJumpToEndsOfLine() throws IOException {
		init(CONF, List.of(), "abc", ctrlA, "12", ctrlE, "3\n");
		assertEquals(con.readLine(), "12abc3");
	}

	@Test
	public void shouldIgnoreNonPrintableChars() throws IOException {
		init(CONF, List.of(), "ab\0c\n");
		assertEquals(con.readLine(), "abc");
	}

	@Test
	public void shouldNavigateHistory() throws IOException {
		init(CONF, List.of("abc", "d"), U, U, U, D, "\n");
		assertEquals(con.readLine(), "d");
		assertString(out, "d\babc\b\b\bd  \b\b\n");
	}

	@Test
	public void shouldEditHistory() throws IOException {
		init(Config.BLOCK, List.of("abc", "d"), U, U, "d", D, "e\n");
		assertEquals(con.readLine(), "de");
		assertIterable(con.history(), "abcd", "d", "de");
	}

	@Test
	public void shouldAddToHistory() throws IOException {
		init(Config.BLOCK, List.of(), " abc\n\nabc\n");
		assertEquals(con.readLine(), " abc"); // not added
		assertEquals(con.readLine(), ""); // not added
		assertEquals(con.readLine(), "abc");
		assertIterable(con.history(), "abc");
	}

	@Test
	public void shouldLimitHistorySize() throws IOException {
		init(new Config(3, false, null, null), List.of(), "a\nb\nc\nd\n");
		assertEquals(con.readLine(), "a");
		assertEquals(con.readLine(), "b");
		assertEquals(con.readLine(), "c");
		assertEquals(con.readLine(), "d");
		assertIterable(con.history(), "b", "c", "d");
	}

	@Test
	public void shouldStopOnEof() throws IOException {
		init(CONF, List.of(), "abc");
		assertEquals(con.readLine(), "abc");
	}

	private void init(ConsoleInput.Config config, List<String> history, Object... inputs) {
		in = new StringReader(in(inputs));
		out = new StringBuilder();
		ps = StringUtil.asPrintStream(out);
		con = ConsoleInput.of(in, ps, config, history);
	}

	private String in(Object... args) {
		return StringUtil.append(new StringBuilder(), "", args).toString();
	}
}
