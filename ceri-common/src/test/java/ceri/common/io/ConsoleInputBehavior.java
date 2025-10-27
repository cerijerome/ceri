package ceri.common.io;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.TestInputStream;
import ceri.common.text.StringBuilders;

public class ConsoleInputBehavior {
	private static final ConsoleInput.Config CONF = new ConsoleInput.Config(0, false, null, null);
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
		Closeables.close(ps, in);
		in = null;
		ps = null;
		con = null;
	}

	@Test
	public void shouldCreateWithDefaultConfig() throws IOException {
		init(CONF, List.of(), "test\n");
		con = ConsoleInput.of(in, ps);
		Assert.equal(con.ready(), true);
		Assert.equal(con.readLine(), "test");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldPollForInput() throws IOException {
		var tin = TestInputStream.of();
		tin.available.autoResponses(0, 0, 1);
		tin.to.writeString("abc\n");
		var out = IoStream.nullPrint();
		in = new InputStreamReader(tin);
		con = ConsoleInput.of(in, out, new ConsoleInput.Config(0, false, null, 0));
		Assert.equal(con.readLine(), "abc");
	}

	@Test
	public void shouldInsertChars() throws IOException {
		init(CONF, List.of(), "abc", L, "\b", "12", R, "3\n");
		Assert.equal(con.readLine(), "a12b3c");
	}

	@Test
	public void shouldDeleteChars() throws IOException {
		init(CONF, List.of(), "abcde", DEL_L, L, L, L, DEL_L, R, DEL_R, "\n");
		Assert.equal(con.readLine(), "bd");
	}

	@Test
	public void shouldJumpToEndsOfLine() throws IOException {
		init(CONF, List.of(), "abc", ctrlA, "12", ctrlE, "3\n");
		Assert.equal(con.readLine(), "12abc3");
	}

	@Test
	public void shouldIgnoreNonPrintableChars() throws IOException {
		init(CONF, List.of(), "ab\0c\n");
		Assert.equal(con.readLine(), "abc");
	}

	@Test
	public void shouldNavigateHistory() throws IOException {
		init(CONF, List.of("abc", "d"), U, U, U, D, "\n");
		Assert.equal(con.readLine(), "d");
		Assert.string(out, "d\babc\b\b\bd  \b\b\n");
	}

	@Test
	public void shouldEditHistory() throws IOException {
		init(ConsoleInput.Config.BLOCK, List.of("abc", "d"), U, U, "d", D, "e\n");
		Assert.equal(con.readLine(), "de");
		Assert.ordered(con.history(), "abcd", "d", "de");
	}

	@Test
	public void shouldAddToHistory() throws IOException {
		init(ConsoleInput.Config.BLOCK, List.of(), " abc\n\nabc\n");
		Assert.equal(con.readLine(), " abc"); // not added
		Assert.equal(con.readLine(), ""); // not added
		Assert.equal(con.readLine(), "abc");
		Assert.ordered(con.history(), "abc");
	}

	@Test
	public void shouldLimitHistorySize() throws IOException {
		init(new ConsoleInput.Config(3, false, null, null), List.of(), "a\nb\nc\nd\n");
		Assert.equal(con.readLine(), "a");
		Assert.equal(con.readLine(), "b");
		Assert.equal(con.readLine(), "c");
		Assert.equal(con.readLine(), "d");
		Assert.ordered(con.history(), "b", "c", "d");
	}

	@Test
	public void shouldStopOnEof() throws IOException {
		init(CONF, List.of(), "abc");
		Assert.equal(con.readLine(), "abc");
	}

	private void init(ConsoleInput.Config config, List<String> history, String... inputs) {
		in = new StringReader(String.join("", inputs));
		out = new StringBuilder();
		ps = StringBuilders.printStream(out);
		con = ConsoleInput.of(in, ps, config, history);
	}
}
