package ceri.common.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import ceri.common.collection.CollectionUtil;
import ceri.common.collection.ImmutableUtil;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.Lazy;
import ceri.common.concurrent.Locker;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.event.Listenable;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.function.Functions.ObjIntFunction;
import ceri.common.io.IoUtil;
import ceri.common.io.LineReader;
import ceri.common.math.MathUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.AnsiEscape;
import ceri.common.text.AnsiEscape.Sgr;
import ceri.common.text.AnsiEscape.Sgr.BasicColor;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;
import ceri.common.text.ToString;
import ceri.common.util.BasicUtil;
import ceri.common.util.CloseableUtil;

/**
 * A tool for parsing keyboard input, and running commands against 1 or more subjects. Allows
 * pre-processing actions before waiting for input, and is able to notify a listener when the
 * subject index changes.
 */
public class ManualTester implements Functions.Closeable {
	private static final Lazy.Value<RuntimeException, Boolean> fastMode = Lazy.Value.of(false);
	private static final Pattern COMMAND_SPLIT_REGEX = Pattern.compile("\\s*;\\s*");
	private final Function<Object, String> stringFn;
	private final List<SubjectConsumer<Object>> preProcessors;
	private final List<Command<?>> commands;
	private final StringBuilder binText = new StringBuilder();
	private final BinaryPrinter bin;
	private final LineReader in;
	private final PrintStream out;
	private final PrintStream err;
	private final List<Object> subjects;
	private final Predicate<String> historical;
	private final int historySize;
	private final int historyBlock;
	private final String indent;
	private final Sgr promptSgr;
	private final int delayMs;
	private final int errorDelayMs;
	private final Locker locker = Locker.of();
	private final List<String> history = new ArrayList<>();
	private CycleRunner cycleRunner = null;
	private int index = 0;
	private boolean exit = false;

	/**
	 * Utilities for parsing commands.
	 */
	public static class Parse {
		private static final Set<Character> TRUE = Set.of('1', 'T', 't', 'Y', 'y');

		private Parse() {}

		/**
		 * Parses first char of group 1 as a boolean.
		 */
		public static Boolean b(Matcher m) {
			return b(m, 1);
		}

		/**
		 * Parses first char of group as a boolean.
		 */
		public static Boolean b(Matcher m, int group) {
			String s = m.group(group);
			if (StringUtil.empty(s)) return null;
			return TRUE.contains(s.charAt(0));
		}

		/**
		 * Returns first char of group 1.
		 */
		public static Character c(Matcher m) {
			return c(m, 1);
		}

		/**
		 * Returns first char of group.
		 */
		public static Character c(Matcher m, int group) {
			String s = m.group(group);
			if (StringUtil.empty(s)) return null;
			return s.charAt(0);
		}

		/**
		 * Parses group 1 as an integer.
		 */
		public static Integer i(Matcher m) {
			return i(m, 1);
		}

		/**
		 * Parses group as an integer.
		 */
		public static Integer i(Matcher m, int group) {
			String s = m.group(group);
			if (StringUtil.empty(s)) return null;
			return RegexUtil.Common.decodeInt(s);
		}

		/**
		 * Parses group 1 as a long.
		 */
		public static Long l(Matcher m) {
			return l(m, 1);
		}

		/**
		 * Parses group as a long.
		 */
		public static Long l(Matcher m, int group) {
			String s = m.group(group);
			if (StringUtil.empty(s)) return null;
			return RegexUtil.Common.decodeLong(s);
		}

		/**
		 * Parses group 1 as a double.
		 */
		public static Double d(Matcher m) {
			return d(m, 1);
		}

		/**
		 * Parses group as a double.
		 */
		public static Double d(Matcher m, int group) {
			String s = m.group(group);
			if (s.isEmpty()) return null;
			return Double.parseDouble(s);
		}

		/**
		 * Returns group length; 0 if null.
		 */
		public static int len(Matcher m) {
			return len(m, 1);
		}

		/**
		 * Returns group length; 0 if null.
		 */
		public static int len(Matcher m, int group) {
			return StringUtil.len(m.group(group));
		}

		/**
		 * Consumes and returns the first non-null group. Returns null if no match.
		 */
		public static String consumeFirst(Matcher m, ObjIntConsumer<String> consumer) {
			return consumeFirst(m, 1, consumer);
		}

		/**
		 * Consumes and returns the first non-null group starting at the given group index. Returns
		 * null if no match.
		 */
		public static String consumeFirst(Matcher m, int start, ObjIntConsumer<String> consumer) {
			return applyFirst(m, start, (s, i) -> {
				consumer.accept(s, i);
				return s;
			});
		}

		/**
		 * Applies the function to the first non-null group, and returns the result. Returns null if
		 * no match.
		 */
		public static <T> T applyFirst(Matcher m, ObjIntFunction<String, T> function) {
			return applyFirst(m, 1, function);
		}

		/**
		 * Applies the function to the first non-null group, starting at the given index, and
		 * returns the result. Returns null if no match.
		 */
		public static <T> T applyFirst(Matcher m, int start, ObjIntFunction<String, T> function) {
			for (int i = start; i <= m.groupCount(); i++) {
				var s = m.group(i);
				if (s == null) continue;
				return function.apply(s, i + 1 - start);
			}
			return null;
		}
	}

	public static interface SubjectConsumer<T> {
		void accept(ManualTester tester, T subject) throws Exception;
	}

	private static record Command<T>(Class<T> cls, Action<T> action, String help) {
		public boolean assignable(Object subject) {
			Class<?> cls = subject == null ? Object.class : subject.getClass();
			return cls().isAssignableFrom(cls);
		}
	}

	public static interface Action<T> {
		boolean execute(Context<T> context) throws Exception;

		record Context<T>(ManualTester tester, List<String> inputs, int index, T subject) {
			public String input() {
				return inputs().get(index());
			}
		}

		interface Input<T> {
			boolean execute(ManualTester tester, String input, T subject) throws Exception;
		}

		interface Match<T> {
			void execute(ManualTester tester, Matcher m, T subject) throws Exception;
		}
	}

	/**
	 * A pre-processor for capturing and processing events.
	 */
	public static class EventCatcher implements Consumer<ManualTester> {
		private final BiConsumer<Object, ManualTester> processor;
		private final Queue<Object> events = new ConcurrentLinkedQueue<>();

		public static EventCatcher of() {
			return of(false);
		}

		public static EventCatcher of(boolean stackTrace) {
			return of((s, tester) -> {
				if (s instanceof Throwable t) tester.err(t, stackTrace);
				else tester.out(s);
			});
		}

		public static EventCatcher of(BiConsumer<Object, ManualTester> processor) {
			return new EventCatcher(processor);
		}

		private EventCatcher(BiConsumer<Object, ManualTester> processor) {
			this.processor = processor;
		}

		public void add(Object event) {
			events.add(event);
		}

		@Override
		public void accept(ManualTester tester) {
			while (true) {
				var event = events.poll();
				if (event == null) break;
				processor.accept(event, tester);
			}
		}

		public void execute(Excepts.Runnable<Exception> runnable) {
			try {
				runnable.run();
			} catch (Exception e) {
				add(e);
			}
		}
	}

	public static class Builder {
		final List<?> subjects;
		Function<Object, String> stringFn = String::valueOf;
		final List<SubjectConsumer<Object>> preProcessors = new ArrayList<>();
		final List<Command<?>> commands = new ArrayList<>();
		int historySize = 100;
		int historyBlock = 5;
		Predicate<String> historical = RegexUtil.nonMatcher("[ ?!:<+\\-@].*");
		String indent = "    ";
		LineReader in = null;
		PrintStream out = System.out;
		PrintStream err = System.err;
		Sgr promptSgr = AnsiEscape.csi.sgr().fgColor(BasicColor.green, false);
		Sgr separatorSgr = AnsiEscape.csi.sgr().fgColor8(3, 3, 3);
		int delayMs = BasicUtil.ternaryInt(fast(), 0, 100);
		int errorDelayMs = BasicUtil.ternaryInt(fast(), 0, 1000);

		@SuppressWarnings("resource")
		protected Builder(List<?> subjects) {
			if (subjects.isEmpty()) throw new IllegalArgumentException("No subjects");
			this.subjects = subjects;
			command("\\?", (t, _, _) -> t.showHelp(), "? = show commands");
			command("\\!", (t, _, _) -> t.exit = true, "! = exit");
			command(":", (t, _, s) -> t.out(ReflectUtil.nameHash(s)), ": = subject type");
			command("(?:(<+)|<(\\d+))", (t, m, _) -> t.history(Parse.len(m), Parse.i(m, 2)),
				"<N = execute Nth previous command");
			command("~(\\d+)", (_, m, _) -> ConcurrentUtil.delay(Parse.i(m)),
				"~N = sleep for N ms");
			command(Object.class, "\\^(\\d+)", (c, m) -> c.tester().repeat(c, Parse.i(m)),
				"^N;... = repeat subsequent commands N times");
			addIndexCommands(subjects.size());
		}

		private void addIndexCommands(int n) {
			if (n <= 1) return;
			command("(\\*)", (t, _, _) -> t.listSubjects(), "* = list all subjects");
			command("(\\-+|\\++)", (t, m, _) -> t.indexDiff(mDiff(m)),
				"-|+ = previous/next subject");
			command("@(\\d+)", (t, m, _) -> t.index(Parse.i(m)),
				"@N = set subject index to N (0.." + (n - 1) + ")");
		}

		public Builder in(InputStream in) {
			return in(LineReader.of(in));
		}

		public Builder in(LineReader in) {
			this.in = in;
			return this;
		}

		public Builder out(PrintStream out) {
			this.out = out;
			return this;
		}

		public Builder err(PrintStream err) {
			this.err = err;
			return this;
		}

		public Builder history(int size, int block) {
			this.historySize = size;
			this.historyBlock = block;
			return this;
		}

		public Builder historical(Predicate<String> historical) {
			this.historical = historical;
			return this;
		}

		public Builder indent(String indent) {
			this.indent = indent;
			return this;
		}

		public Builder stringFn(Function<Object, String> stringFn) {
			this.stringFn = stringFn;
			return this;
		}

		public Builder promptSgr(Sgr sgr) {
			this.promptSgr = sgr;
			return this;
		}

		public Builder separatorSgr(Sgr sgr) {
			this.separatorSgr = sgr;
			return this;
		}

		public Builder delayMs(int delayMs) {
			this.delayMs = delayMs;
			return this;
		}

		public Builder errorDelayMs(int errorDelayMs) {
			this.errorDelayMs = errorDelayMs;
			return this;
		}

		/**
		 * Listen for events and output during pre-processor stage. Uses current string function.
		 */
		public Builder listenTo(Object... subjects) {
			return listen(Arrays.asList(subjects));
		}

		/**
		 * Listen for events and output during pre-processor stage. Uses current string function.
		 */
		public Builder listen(Collection<?> subjects) {
			var events = EventCatcher.of();
			for (var subject : subjects)
				if (subject instanceof Listenable.Indirect<?> listenable) listenable.listeners()
					.listen(e -> events.add(stringFn.apply(listenable) + " => " + e));
			return preProcessor(events);
		}

		public <T> Builder preProcessor(Class<T> cls, SubjectConsumer<T> preProcessor) {
			return preProcessor(typed(cls, preProcessor));
		}

		public Builder preProcessor(Consumer<ManualTester> preProcessor) {
			return preProcessor((t, _) -> preProcessor.accept(t));
		}

		public Builder preProcessor(SubjectConsumer<Object> preProcessor) {
			preProcessors.add(preProcessor);
			return this;
		}

		public Builder command(String pattern, Action.Match<Object> action, String help) {
			return command(Object.class, pattern, action, help);
		}

		public <T> Builder command(Class<T> cls, String pattern, Action.Match<T> action,
			String help) {
			var p = Pattern.compile(pattern);
			return command(cls, c -> ManualTester.matches(p, c.input(),
				m -> action.execute(c.tester(), m, c.subject())), help);
		}

		public <T> Builder command(Class<T> cls, String pattern,
			Excepts.BiConsumer<?, Action.Context<T>, Matcher> action, String help) {
			var p = Pattern.compile(pattern);
			return command(cls, c -> ManualTester.matches(p, c.input(), m -> action.accept(c, m)),
				help);
		}

		public <T> Builder command(Class<T> cls, Action.Input<T> action, String help) {
			return command(cls, c -> action.execute(c.tester(), c.input(), c.subject()), help);
		}

		public <T> Builder command(Class<T> cls, Action<T> action, String help) {
			commands.add(new Command<>(cls, action, help));
			return this;
		}

		public Builder separator(String separator) {
			return separator(Object.class, separator);
		}

		public <T> Builder separator(Class<T> cls, String separator) {
			command(cls, _ -> false, start(separatorSgr) + separator + stop(separatorSgr));
			return this;
		}

		public ManualTester build() {
			return new ManualTester(this);
		}
	}

	public static <T> Builder builder(T subject) {
		return builderList(Arrays.asList(subject));
	}

	public static <T> Builder builder(T subject, Function<T, String> stringFn) {
		return builderList(Arrays.asList(subject), stringFn);
	}

	@SafeVarargs
	public static <T> Builder builderArray(T... subjects) {
		return builderList(Arrays.asList(subjects));
	}

	public static Builder builderList(List<? extends Object> subjects) {
		return new Builder(subjects);
	}

	public static <T> Builder builderList(List<T> subjects, Function<T, String> stringFn) {
		return builderList(subjects).stringFn(s -> {
			return stringFn.apply(BasicUtil.<T>unchecked(s));
		});
	}

	/**
	 * Reduce timings to zero when building. Close to return to default timing.
	 */
	public static Functions.Closeable fastMode() {
		return fastMode.override(true);
	}

	protected ManualTester(Builder builder) {
		in = BasicUtil.def(builder.in, () -> LineReader.of(System.in));
		out = builder.out;
		err = builder.err;
		historical = builder.historical;
		historySize = builder.historySize;
		historyBlock = builder.historyBlock;
		indent = builder.indent;
		promptSgr = builder.promptSgr;
		delayMs = builder.delayMs;
		errorDelayMs = builder.errorDelayMs;
		stringFn = builder.stringFn;
		preProcessors = ImmutableUtil.copyAsList(builder.preProcessors);
		commands = ImmutableUtil.copyAsList(builder.commands);
		bin = binaryPrinter();
		subjects = ImmutableUtil.copyAsList(builder.subjects);
	}

	/**
	 * Process user input commands, until exit.
	 */
	public void run() {
		exit = false;
		showHelp();
		while (!exit) {
			ConcurrentUtil.delay(delayMs); // try to avoid err/out print conflict
			preProcess();
			print(start(promptSgr) + prompt());
			execute(() -> {
				String input = readInput();
				print(stop(promptSgr));
				executeInput(input);
			}, true);
		}
	}

	/**
	 * A locker in case synchronization is required, such as with cycles.
	 */
	public Locker locker() {
		return locker;
	}

	/**
	 * Start a cycle in a separate thread. Starts the thread on first call.
	 */
	public void startCycle(CycleRunner.Cycle cycle) {
		stopCycle();
		if (cycleRunner == null) cycleRunner = CycleRunner.of(locker());
		cycleRunner.start(cycle);
		outf("%s cycle started: %s", cycle.name(), cycle);
	}

	/**
	 * Stop the current cycle if running.
	 */
	public void stopCycle() {
		if (cycleRunner == null) return;
		var cycle = cycleRunner.cycle();
		if (cycle != null) outf("%s cycle stopped", cycle.name());
		cycleRunner.stop();
	}

	/**
	 * Return the active cycle, or null if not running.
	 */
	public CycleRunner.Cycle activeCycle() {
		return CycleRunner.activeCycle(cycleRunner);
	}

	/**
	 * Print to out.
	 */
	public void outf(String format, Object... args) {
		out(StringUtil.format(format, args));
	}

	/**
	 * Print to out.
	 */
	public void out(Object text) {
		out.println(indent + text);
		out.flush();
	}

	/**
	 * Print to err.
	 */
	public void errf(String format, Object... args) {
		err(StringUtil.format(format, args));
	}

	/**
	 * Print to err.
	 */
	public void err(Object text) {
		err.println(indent + text);
		err.flush();
	}

	/**
	 * Print exception to err with optional stack trace.
	 */
	public void err(Throwable t, boolean stackTrace) {
		if (stackTrace) t.printStackTrace(err);
		else err.println(indent + t);
		err.flush();
	}

	/**
	 * Set the current subject index.
	 */
	public void index(int i) {
		index = MathUtil.limit(i, 0, subjects.size() - 1);
	}

	/**
	 * Return the current subject index.
	 */
	public int index() {
		return index;
	}

	/**
	 * Return the current subject.
	 */
	public Object subject() {
		return subjects.get(index());
	}

	/**
	 * Runs the action; only logs the runtime error message, not the stack trace.
	 */
	public static <T> Action.Match<T> rt(Action.Match<T> match) {
		return (t, m, s) -> {
			try {
				match.execute(t, m, s);
			} catch (RuntimeInterruptedException e) {
				throw e;
			} catch (RuntimeException e) {
				t.err(e.getMessage());
			}
		};
	}

	/**
	 * Read and print available bytes from the stream.
	 */
	public void readBytes(InputStream in) throws IOException {
		var bytes = IoUtil.availableBytes(in);
		if (bytes.isEmpty()) return;
		out("IN <<< ");
		print(bytes);
	}

	/**
	 * Write ascii bytes to the output stream and mirror to out.
	 */
	public void writeAscii(OutputStream out, String text) throws IOException {
		writeBytes(out, ByteUtil.toAscii(text));
	}

	/**
	 * Write bytes to the output stream and mirror to out.
	 */
	public void writeBytes(OutputStream out, ByteProvider bytes) throws IOException {
		if (bytes.isEmpty()) return;
		out("OUT >>>");
		print(bytes);
		bytes.writeTo(0, out);
		out.flush();
	}

	@Override
	public void close() {
		CloseableUtil.close(cycleRunner);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, subjects.size(), commands.size());
	}

	private void showHelp() {
		out.println("Commands: (separate multiple commands with ;)");
		var cls = cls(subject());
		for (var command : commands)
			if (command.cls().isAssignableFrom(cls)) out(command.help());
	}

	private void preProcess() {
		var subject = subject();
		for (var preProcessor : preProcessors)
			execute(() -> preProcessor.accept(this, subject), false);
	}

	private String prompt() {
		return string(subject(), index()) + "> ";
	}

	private String readInput() {
		try {
			return in.readLine();
		} catch (IOException e) {
			err(e);
			exit = true; // Assume unable to recover
			return "";
		}
	}

	/**
	 * Attempt to execute the runnable function.
	 */
	private void execute(Excepts.Runnable<Exception> runnable, boolean stackTrace) {
		try {
			runnable.run();
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (Exception e) {
			err(e, stackTrace);
			ConcurrentUtil.delay(errorDelayMs); // slow down errors
		}
	}

	private void executeInput(String line) throws Exception {
		var inputs = Stream.<String>of(COMMAND_SPLIT_REGEX.split(StringUtil.trim(line)))
			.map(s -> StringUtil.unEscape(s)).toList();
		for (int i = 0; i < inputs.size(); i++)
			executeInput(inputs, i);
		addToHistory(line);
	}

	private void executeInput(List<String> inputs, int i) throws Exception {
		var context = new Action.Context<>(this, inputs, i, subject());
		if (StringUtil.blank(context.input())) return;
		for (var command : commands) {
			if (!command.assignable(context.subject())) continue;
			if (command.action().execute(BasicUtil.unchecked(context))) return;
		}
		err("Invalid command: " + context.input());
	}

	private void repeat(Action.Context<?> context, int n) throws Exception {
		for (; n > 1; n--) { // n-1 repeats followed by normal exec = n times
			for (int i = context.index() + 1; i < context.inputs().size(); i++) {
				if (in.ready()) return; // stop if any input available
				executeInput(context.inputs(), i);
			}
		}
	}

	private static <E extends Exception> boolean matches(Pattern pattern, String input,
		Excepts.Consumer<E, Matcher> consumer) throws Exception {
		var m = RegexUtil.matched(pattern, input);
		if (m == null) return false;
		consumer.accept(m);
		return true;
	}

	private void addToHistory(String input) {
		if (StringUtil.empty(input) || !historical.test(input)) return;
		while (history.size() >= historySize)
			history.remove(0);
		history.add(input);
	}

	private void history(int n, Integer index) throws Exception {
		if (index == null) {
			int count = Math.min(history.size(), n * historyBlock);
			for (int i = count; i > 0; i--)
				outf("%d) %s", i, history.get(history.size() - i));
		} else {
			var command = CollectionUtil.getOrDefault(history, history.size() - index, "");
			if (StringUtil.empty(command)) return;
			outf("%d) %s", index, command);
			executeInput(command);
		}
	}

	private void listSubjects() {
		for (int i = 0; i < subjects.size(); i++)
			out(string(subjects.get(i), i));
	}

	private String string(Object subject, int index) {
		StringBuilder b = new StringBuilder();
		if (subjects.size() <= 1) b.append(stringFn.apply(subject));
		else StringUtil.format(b, "%d) %s", index, stringFn.apply(subject));
		var cycle = activeCycle();
		if (cycle != null) b.append('[').append(cycle.name()).append(']');
		return b.toString();
	}

	private void indexDiff(int diff) {
		int n = subjects.size();
		index((n + index() + (diff % n)) % n);
	}

	private static <T> SubjectConsumer<Object> typed(Class<T> cls, SubjectConsumer<T> consumer) {
		return (t, s) -> {
			if (cls.isInstance(s)) consumer.accept(t, BasicUtil.<T>unchecked(s));
		};
	}

	private static int mDiff(Matcher m) {
		return m.group(1).chars().map(i -> i == '+' ? 1 : -1).sum();
	}

	private void print(String s) {
		if (s.isEmpty()) return;
		out.print(s);
		out.flush();
	}

	private void print(ByteProvider data) {
		bin.print(data).flush();
		for (var line : StringUtil.lines(binText))
			out(line);
		StringUtil.clear(binText);
	}

	@SuppressWarnings("resource")
	private BinaryPrinter binaryPrinter() {
		return BinaryPrinter.builder(BinaryPrinter.STD).out(StringUtil.asPrintStream(binText))
			.build();
	}

	private static boolean fast() {
		return fastMode.get() == Boolean.TRUE;
	}

	private static Class<?> cls(Object subject) {
		if (subject == null) return Object.class;
		return subject.getClass();
	}

	private static String start(Sgr sgr) {
		return sgr == null ? "" : sgr.toString();
	}

	private static String stop(Sgr sgr) {
		return sgr == null ? "" : Sgr.reset;
	}
}
