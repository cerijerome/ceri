package ceri.common.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collection.ImmutableUtil;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionRunnable;
import ceri.common.io.IoUtil;
import ceri.common.math.MathUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.AnsiEscape;
import ceri.common.text.AnsiEscape.Sgr.BasicColor;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

/**
 * A tool for parsing keyboard input, and running commands against 1 or more subjects. Allows
 * pre-processing actions before waiting for input, and is able to notify a listener when the
 * subject index changes.
 */
public class ManualTester {
	private static final Pattern COMMAND_SPLIT_REGEX = Pattern.compile("\\s*;\\s*");
	private final Function<Object, String> stringFn;
	private final List<SubjectConsumer<Object>> preProcessors;
	private final Map<Class<?>, List<Command<?>>> commands;
	private final SubjectConsumer<Object> listener;
	private final StringBuilder binText = new StringBuilder();
	private final BinaryPrinter bin;
	public final InputStream in;
	public final PrintStream out;
	public final PrintStream err;
	public final List<Object> subjects;
	private final String indent;
	private final BasicColor promptColor;
	private final int delayMs;
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
			if (s.isEmpty()) return null;
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
			if (s.isEmpty()) return null;
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
			if (s.isEmpty()) return null;
			return Integer.parseInt(s);
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
	}

	public static interface SubjectConsumer<T> {
		void accept(T subject, ManualTester tester) throws Exception;
	}

	public static interface Action<T> {
		boolean execute(String input, T subject, ManualTester tester) throws Exception;
	}

	public static interface MatcherAction<T> {
		void execute(Matcher m, T subject, ManualTester tester) throws Exception;
	}

	private static record Command<T>(Action<T> action, String help) {}

	/**
	 * A pre-processor for capturing and processing events.
	 */
	public static class EventCatcher implements Consumer<ManualTester> {
		private final BiConsumer<Object, ManualTester> processor;
		private final Queue<Object> events = new ConcurrentLinkedQueue<>();

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

	public static EventCatcher eventCatcher() {
		return eventCatcher(false);
	}

	public static EventCatcher eventCatcher(boolean stackTrace) {
		return eventCatcher((s, tester) -> {
			if (s instanceof Throwable t) tester.err(t, stackTrace);
			else tester.out(s);
		});
	}

	public static EventCatcher eventCatcher(BiConsumer<Object, ManualTester> processor) {
		return new EventCatcher(processor);
	}

	public static class Builder {
		final List<?> subjects;
		Function<Object, String> stringFn = String::valueOf;
		final List<SubjectConsumer<Object>> preProcessors = new ArrayList<>();
		final Map<Class<?>, List<Command<?>>> commands = new LinkedHashMap<>();
		String indent = "    ";
		SubjectConsumer<Object> listener = (s, t) -> {};
		InputStream in = System.in;
		PrintStream out = System.out;
		PrintStream err = System.err;
		BasicColor promptColor = BasicColor.blue;
		int delayMs = 100;

		protected Builder(List<?> subjects) {
			if (subjects.isEmpty()) throw new IllegalArgumentException("No subjects");
			this.subjects = subjects;
			command("\\?", (m, t) -> t.showHelp(), "? = show commands");
			command("\\!", (m, t) -> t.exit = true, "! = exit");
			command(Object.class, ":",
				(m, s, t) -> t.out(ReflectUtil.className(s) + ReflectUtil.hashId(s)),
				": = subject type");
			addIndexCommands(subjects.size());
			command("~(\\d+)", (m, t) -> ConcurrentUtil.delay(Parse.i(m)), "~[N] = sleep for N ms");
		}

		private void addIndexCommands(int n) {
			if (n <= 1) return;
			command("(\\*)", (m, t) -> t.listSubjects(), "* = list all subjects");
			command("(\\-+|\\++)", (m, t) -> t.indexDiff(mDiff(m)), "-|+ = previous/next subject");
			command("@(\\d+)", (m, t) -> t.index(Parse.i(m)),
				"@[N] = set subject index (0.." + (n - 1) + ")");
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

		public <T> Builder listener(Class<T> cls, SubjectConsumer<T> preProcessor) {
			return listener(typed(cls, preProcessor));
		}

		public Builder listener(SubjectConsumer<Object> listener) {
			this.listener = listener;
			return this;
		}

		public <T> Builder preProcessor(Class<T> cls, SubjectConsumer<T> preProcessor) {
			return preProcessor(typed(cls, preProcessor));
		}

		public Builder preProcessor(Consumer<ManualTester> preProcessor) {
			return preProcessor((s, t) -> preProcessor.accept(t));
		}

		public Builder preProcessor(SubjectConsumer<Object> preProcessor) {
			preProcessors.add(preProcessor);
			return this;
		}

		public Builder command(String pattern,
			ExceptionBiConsumer<Exception, Matcher, ManualTester> action, String help) {
			return command(Object.class, pattern, (m, s, t) -> action.accept(m, t), help);
		}

		public <T> Builder command(Class<T> cls, String pattern, MatcherAction<T> action,
			String help) {
			Pattern p = Pattern.compile(pattern);
			return command(cls, new Command<>(
				(i, s, t) -> executeWithMatcher(i, p, m -> action.execute(m, s, t)), help));
		}

		public <T> Builder command(Class<T> cls, Action<T> action, String help) {
			return command(cls, new Command<>(action, help));
		}

		private <T> Builder command(Class<T> cls, Command<T> command) {
			commands.computeIfAbsent(cls, c -> new ArrayList<>()).add(command);
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
		return builderList(subjects).stringFn(s -> stringFn.apply(BasicUtil.<T>uncheckedCast(s)));
	}

	protected ManualTester(Builder builder) {
		in = builder.in;
		out = builder.out;
		err = builder.err;
		indent = builder.indent;
		promptColor = builder.promptColor;
		delayMs = builder.delayMs;
		stringFn = builder.stringFn;
		preProcessors = ImmutableUtil.copyAsList(builder.preProcessors);
		commands = ImmutableUtil.copyAsMapOfLists(builder.commands);
		bin = binaryPrinter();
		listener = builder.listener;
		subjects = List.copyOf(builder.subjects);
	}

	public void run() {
		showHelp();
		notifyListener();
		while (!exit) {
			ConcurrentUtil.delay(delayMs); // try to avoid err/out print conflict
			preProcess();
			print(promptColor() + prompt());
			execute(() -> {
				String input = readInput();
				print(promptNoColor());
				executeInput(input, subject());
			}, true);
		}
	}

	public void out(Object text) {
		out.println(indent + text);
		out.flush();
	}

	public void err(Object text) {
		err.println(indent + text);
		err.flush();
	}

	public void err(Throwable t, boolean stackTrace) {
		if (stackTrace) t.printStackTrace(err);
		else err.println(indent + t);
		err.flush();
	}

	public void index(int i) {
		int orig = index;
		index = MathUtil.limit(i, 0, subjects.size() - 1);
		if (index != orig) notifyListener();
	}

	public int index() {
		return index;
	}

	public Object subject() {
		return subjects.get(index());
	}

	public void readBytes(InputStream in) throws IOException {
		var bytes = IoUtil.availableBytes(in);
		if (bytes.isEmpty()) return;
		out("IN <<< ");
		print(bytes);
	}

	public void writeAscii(OutputStream out, String text) throws IOException {
		writeBytes(out, ByteUtil.toAscii(text));
	}

	public void writeBytes(OutputStream out, ByteProvider bytes) throws IOException {
		if (bytes.isEmpty()) return;
		out("OUT >>>");
		print(bytes);
		bytes.writeTo(0, out);
		out.flush();
	}

	private void notifyListener() {
		execute(() -> listener.accept(subject(), this), false);
	}

	private void showHelp() {
		out.println("Commands: (separate multiple commands with ;)");
		var cls = ReflectUtil.getClass(subject());
		if (cls != null) commands.forEach((type, actions) -> {
			if (type.isAssignableFrom(cls)) for (var action : actions)
				out(action.help);
		});
	}

	private void preProcess() {
		var subject = subject();
		for (var preProcessor : preProcessors)
			execute(() -> preProcessor.accept(subject, this), false);
	}

	private String prompt() {
		return string(subject(), index()) + "> ";
	}

	private String promptColor() {
		return promptColor == null ? "" :
			AnsiEscape.csi.sgr().fgColor(promptColor, false).toString();
	}

	private String promptNoColor() {
		return promptColor == null ? "" : AnsiEscape.csi.sgr().reset().toString();
	}

	private String readInput() {
		try {
			//return StringUtil.unEscape(IoUtil.pollString(in).trim());
			return IoUtil.pollString(in).trim();
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
			if (exit) return;
			runnable.run();
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (Exception e) {
			err(e, stackTrace);
		}
	}

	private void executeInput(String input, Object subject) throws Exception {
		
		String[] commands = COMMAND_SPLIT_REGEX.split(input);
		for (var command : commands)
			executeCommand(StringUtil.unEscape(command), subject);
	}

	private void executeCommand(String input, Object subject) throws Exception {
		if (StringUtil.blank(input)) return;
		var cls = subject.getClass();
		for (var entry : commands.entrySet()) {
			if (!entry.getKey().isAssignableFrom(cls)) continue;
			for (var command : entry.getValue())
				if (execute(input, subject, BasicUtil.uncheckedCast(command))) return;
		}
		err("Invalid command: " + input);
	}

	private boolean execute(String input, Object subject, Command<Object> command)
		throws Exception {
		return command.action.execute(input, subject, this);
	}

	private void listSubjects() {
		for (int i = 0; i < subjects.size(); i++)
			out(string(subjects.get(i), i));
	}

	private String string(Object subject, int index) {
		if (subjects.size() <= 1) return stringFn.apply(subject);
		return String.format("%d) %s", index, stringFn.apply(subject));
	}

	private void indexDiff(int diff) {
		int n = subjects.size();
		index((n + index() + (diff % n)) % n);
	}

	private static <T> SubjectConsumer<Object> typed(Class<T> cls, SubjectConsumer<T> consumer) {
		return (s, t) -> {
			if (cls.isInstance(s)) consumer.accept(BasicUtil.<T>uncheckedCast(s), t);
		};
	}

	private static boolean executeWithMatcher(String input, Pattern pattern,
		ExceptionConsumer<Exception, Matcher> action) throws Exception {
		Matcher matcher = RegexUtil.matched(pattern, input);
		if (matcher == null) return false;
		action.accept(matcher);
		return true;
	}

	private static int mDiff(Matcher m) {
		return m.group(1).chars().map(i -> i == '+' ? 1 : i == '-' ? -1 : 0).sum();
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
}
