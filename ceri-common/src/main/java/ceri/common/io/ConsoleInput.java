package ceri.common.io;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import ceri.common.array.ArrayUtil;
import ceri.common.collection.Immutable;
import ceri.common.collection.Lists;
import ceri.common.collection.Maps;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.Excepts;
import ceri.common.math.Maths;
import ceri.common.text.Chars;
import ceri.common.text.Regex;
import ceri.common.text.Strings;

/**
 * A console input and display utility for char-based input. Attempts to copy functionality of
 * terminal line-based input, and provides line history recall/editing.
 */
public class ConsoleInput implements LineReader {
	private static final Pattern SPACE_REGEX = Pattern.compile("^ ");
	private static final int ESC_END_MIN = 0x40;
	private static final int ESC_END_MAX = 0x7e;
	private static final int ESC_LEN_MIN = 3;
	private static final int ESC_LEN_MAX = 4;
	private static final String DEL = Chars.ESC + "[3~";
	private static final String UP = Chars.ESC + "[A";
	private static final String DOWN = Chars.ESC + "[B";
	private static final String RIGHT = Chars.ESC + "[C";
	private static final String LEFT = Chars.ESC + "[D";
	private static final char START = 0x01;
	private static final char END = 0x05;
	private final Config config;
	private final List<String> history = Lists.of();
	private final Reader in;
	private final PrintStream out;

	/**
	 * Internal state.
	 */
	private class State {
		private final Map<Integer, String> historyMods = Maps.of();
		private int historyIndex = history.size();
		private final StringBuilder esc = new StringBuilder();
		private final StringBuilder line = new StringBuilder();
		private int pos = 0;
		private boolean complete = false;
	}

	/**
	 * Input configuration. History size determines when older entries are purged. History edit
	 * allows edits to be saved. Historical predicate determines if a line should be save to
	 * history. Poll delay determines how long to wait before checking for new input; a null value
	 * blocks until input is available.
	 */
	public record Config(int historySize, boolean historyEdit,
		Excepts.Predicate<RuntimeException, String> historical, Integer pollDelayMs) {
		public static Excepts.Predicate<RuntimeException, String> NO_SPACE =
			Regex.Filter.matching(SPACE_REGEX, Regex.Filter.NON_FIND);
		public static Config POLL = new Config(100, true, NO_SPACE, 20);
		public static Config BLOCK = new Config(100, true, NO_SPACE, null);
		public static Config NO_EDIT = new Config(100, false, NO_SPACE, null);
	}

	/**
	 * Creates an instance for input and output streams with blocking configuration.
	 */
	public static ConsoleInput of(Reader in, PrintStream out) {
		return of(in, out, Config.BLOCK);
	}

	/**
	 * Creates an instance for input and output streams with given configuration and pre-set
	 * history.
	 */
	public static ConsoleInput of(Reader in, PrintStream out, Config config, String... history) {
		return new ConsoleInput(in, out, config, Arrays.asList(history));
	}

	/**
	 * Creates an instance for input and output streams with given configuration and pre-set
	 * history.
	 */
	public static ConsoleInput of(Reader in, PrintStream out, Config config,
		Iterable<String> history) {
		return new ConsoleInput(in, out, config, history);
	}

	private ConsoleInput(Reader in, PrintStream out, Config config, Iterable<String> history) {
		this.in = in;
		this.out = out;
		this.config = config;
		for (var line : history) {
			this.history.add(line);
		}
	}

	@Override
	public String readLine() throws IOException {
		var state = new State();
		while (!state.complete) {
			int c = read();
			if (c < 0) break;
			process(state, (char) c);
		}
		moveTo(state, state.line.length());
		out.println();
		return updateHistory(state);
	}

	@Override
	public boolean ready() throws IOException {
		return in.ready();
	}

	/**
	 * Provide a view onto current history.
	 */
	public List<String> history() {
		return Immutable.wrap(history);
	}

	private int read() throws IOException {
		while (true) {
			if (config.pollDelayMs() == null || in.ready()) return in.read();
			ConcurrentUtil.delay(config.pollDelayMs()); // poll delay if no input
		}
	}

	private void process(State state, char c) {
		if (processEsc(state, c)) return;
		if (processCtrl(state, c)) return;
		insert(state, String.valueOf(c));
	}

	private boolean processEsc(State state, char c) {
		if (state.esc.isEmpty() && c != Chars.ESC) return false;
		if (c == Chars.ESC) state.esc.setLength(0);
		state.esc.append(c);
		int len = state.esc.length();
		if (len >= ESC_LEN_MAX
			|| (len >= ESC_LEN_MIN && Maths.within(c, ESC_END_MIN, ESC_END_MAX))) {
			var esc = state.esc.substring(0);
			switch (esc) {
				case DEL -> deleteRight(state, 1);
				case UP -> history(state, state.historyIndex - 1);
				case DOWN -> history(state, state.historyIndex + 1);
				case RIGHT -> moveTo(state, state.pos + 1);
				case LEFT -> moveTo(state, state.pos - 1);
				default -> {}
			}
			state.esc.setLength(0);
		}
		return true;
	}

	private boolean processCtrl(State state, char c) {
		boolean result = true;
		switch (c) {
			case START -> moveTo(state, 0);
			case END -> moveTo(state, state.line.length());
			case Chars.BS -> moveTo(state, state.pos - 1);
			case Chars.DEL -> deleteLeft(state, 1);
			case Chars.NL, Chars.CR -> state.complete = true;
			default -> result = !Chars.isPrintable(c); // drop if non-printable
		}
		return result;
	}

	private String updateHistory(State state) {
		var line = storeHistory(state);
		state.historyMods.remove(state.historyIndex);
		state.historyMods.remove(history.size());
		if (config.historyEdit()) state.historyMods.forEach((i, s) -> {
			if (historical(s)) history.set(i, s);
		});
		addHistory(line);
		return line;
	}

	private void addHistory(String line) {
		if (config.historySize() == 0 || !historical(line)) return;
		while (history.size() >= config.historySize())
			history.remove(0);
		history.add(line);
	}

	private boolean historical(String line) {
		if (Strings.isBlank(line)) return false;
		return config.historical() == null || config.historical().test(line);
	}

	private void history(State state, int index) {
		index = Maths.limit(index, 0, history.size());
		var current = storeHistory(state);
		if (index == state.historyIndex) return;
		recallHistory(state, index);
		redrawHistory(state, current.length());
	}

	private String storeHistory(State state) {
		String current = state.line.toString();
		state.historyMods.put(state.historyIndex, current);
		return current;
	}

	private void recallHistory(State state, int index) {
		state.historyIndex = index;
		state.line.setLength(0);
		state.line.append(historyAt(state, index));
	}

	private void redrawHistory(State state, int max) {
		moveTo(state, 0);
		moveTo(state, state.line.length());
		redraw(state, max);
	}

	private String historyAt(State state, int index) {
		var s = state.historyMods.get(index);
		if (s != null) return s;
		return Lists.at(history, index, "");
	}

	private void insert(State state, String s) {
		state.line.insert(state.pos, s);
		moveTo(state, state.pos + s.length());
		redraw(state, 0);
	}

	private void deleteLeft(State state, int n) {
		n = Math.min(state.pos, n);
		moveTo(state, state.pos - n);
		deleteRight(state, n);
	}

	private void deleteRight(State state, int n) {
		n = Math.min(state.line.length() - state.pos, n);
		int max = state.line.length();
		state.line.delete(state.pos, state.pos + n);
		redraw(state, max);
	}

	private void moveTo(State state, int pos) {
		pos = Maths.limit(pos, 0, state.line.length());
		if (pos == state.pos) return;
		if (pos < state.pos) out.print(repeat(Chars.BS, state.pos - pos));
		else out.print(state.line.substring(state.pos, pos));
		state.pos = pos;
	}

	private void redraw(State state, int max) {
		max = Math.max(state.line.length(), max);
		out.print(state.line.substring(state.pos));
		out.print(repeat(' ', max - state.line.length()));
		out.print(repeat(Chars.BS, max - state.pos));
	}

	private static char[] repeat(char c, int n) {
		if (n <= 0) return ArrayUtil.chars.empty;
		var chars = new char[n];
		Arrays.fill(chars, c);
		return chars;
	}
}
