package ceri.common.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import ceri.common.collection.ImmutableUtil;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.Locker;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.event.Listenable;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ObjIntFunction;
import ceri.common.function.RuntimeCloseable;
import ceri.common.io.IoUtil;
import ceri.common.math.MathUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.AnsiEscape;
import ceri.common.text.AnsiEscape.Sgr;
import ceri.common.text.AnsiEscape.Sgr.BasicColor;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.CloseableUtil;

/**
 * A tool for parsing keyboard input, and running commands against 1 or more subjects. Allows
 * pre-processing actions before waiting for input, and is able to notify a listener when the
 * subject index changes.
 */
public class ManualTester implements RuntimeCloseable {
	private static final Pattern COMMAND_SPLIT_REGEX = Pattern.compile("\\s*;\\s*");
	private final Function<Object, String> stringFn;
	private final List<SubjectConsumer<Object>> preProcessors;
	private final List<Command<?>> commands;
	private final StringBuilder binText = new StringBuilder();
	private final BinaryPrinter bin;
	public final BufferedReader in;
	public final PrintStream out;
	public final PrintStream err;
	public final List<Object> subjects;
	private final String indent;
	private final BasicColor promptColor;
	private final int delayMs;
	private final Locker locker = Locker.of();
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

		public void execute(ExceptionRunnable<Exception> runnable) {
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
		String indent = "    ";
		InputStream in = System.in;
		PrintStream out = System.out;
		PrintStream err = System.err;
		BasicColor promptColor = BasicColor.green;
		int delayMs = BasicUtil.conditionalInt(TestUtil.isTest, 0, 100);

		@SuppressWarnings("resource")
		protected Builder(List<?> subjects) {
			if (subjects.isEmpty()) throw new IllegalArgumentException("No subjects");
			this.subjects = subjects;
			command("\\?", (t, m, s) -> t.showHelp(), "? = show commands");
			command("\\!", (t, m, s) -> t.exit = true, "! = exit");
			command(":", (t, m, s) -> t.out(ReflectUtil.nameHash(s)), ": = subject type");
			command("~(\\d+)", (t, m, s) -> ConcurrentUtil.delay(Parse.i(m)),
				"~N = sleep for N ms");
			command(Object.class, "\\^(\\d+)", (c, m) -> c.tester().repeat(c, Parse.i(m)),
				"^N;... = repeat subsequent commands N times");
			addIndexCommands(subjects.size());
		}

		private void addIndexCommands(int n) {
			if (n <= 1) return;
			command("(\\*)", (t, m, s) -> t.listSubjects(), "* = list all subjects");
			command("(\\-+|\\++)", (t, m, s) -> t.indexDiff(mDiff(m)),
				"-|+ = previous/next subject");
			command("@(\\d+)", (t, m, s) -> t.index(Parse.i(m)),
				"@N = set subject index to N (0.." + (n - 1) + ")");
		}

		public Builder in(InputStream in) {
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

		public Builder indent(String indent) {
			this.indent = indent;
			return this;
		}

		public Builder stringFn(Function<Object, String> stringFn) {
			this.stringFn = stringFn;
			return this;
		}

		public Builder promptColor(BasicColor color) {
			this.promptColor = color;
			return this;
		}

		public Builder delayMs(int delayMs) {
			this.delayMs = delayMs;
			return this;
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
			return preProcessor((t, s) -> preProcessor.accept(t));
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
			ExceptionBiConsumer<?, Action.Context<T>, Matcher> action, String help) {
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
			command(cls, c -> false, separator);
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
			return stringFn.apply(BasicUtil.<T>uncheckedCast(s));
		});
	}

	protected ManualTester(Builder builder) {
		in = new BufferedReader(new InputStreamReader(builder.in));
		out = builder.out;
		err = builder.err;
		indent = builder.indent;
		promptColor = builder.promptColor;
		delayMs = builder.delayMs;
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
			print(promptColor() + prompt());
			execute(() -> {
				String input = readInput();
				print(promptNoColor());
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

	private String promptColor() {
		return promptColor == null ? "" :
			AnsiEscape.csi.sgr().fgColor(promptColor, false).toString();
	}

	private String promptNoColor() {
		return promptColor == null ? "" : Sgr.reset;
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
	private void execute(ExceptionRunnable<Exception> runnable, boolean stackTrace) {
		try {
			runnable.run();
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (Exception e) {
			err(e, stackTrace);
		}
	}

	private void executeInput(String line) throws Exception {
		var inputs = Stream.<String>of(COMMAND_SPLIT_REGEX.split(line))
			.map(s -> StringUtil.unEscape(s)).toList();
		for (int i = 0; i < inputs.size(); i++) {
			executeInput(inputs, i);
		}
	}

	private void executeInput(List<String> inputs, int i) throws Exception {
		var context = new Action.Context<>(this, inputs, i, subject());
		if (StringUtil.blank(context.input())) return;
		for (var command : commands) {
			if (!command.assignable(context.subject())) continue;
			if (command.action().execute(BasicUtil.uncheckedCast(context))) return;
		}
		err("Invalid command: " + context.input());
	}

	private void repeat(Action.Context<?> context, int n) throws Exception {
		for (; n > 0; n--) {
			for (int i = context.index() + 1; i < context.inputs().size(); i++) {
				if (in.ready()) return; // stop if any input available
				executeInput(context.inputs(), i);
			}
		}
	}

	private static <E extends Exception> boolean matches(Pattern pattern, String input,
		ExceptionConsumer<E, Matcher> consumer) throws Exception {
		var m = RegexUtil.matched(pattern, input);
		if (m == null) return false;
		consumer.accept(m);
		return true;
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
			if (cls.isInstance(s)) consumer.accept(t, BasicUtil.<T>uncheckedCast(s));
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

	private static Class<?> cls(Object subject) {
		if (subject == null) return Object.class;
		return subject.getClass();
	}
}
