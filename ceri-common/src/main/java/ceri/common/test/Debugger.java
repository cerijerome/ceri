package ceri.common.test;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import ceri.common.reflect.Caller;
import ceri.common.reflect.ReflectUtil;

/**
 * Useful class for printf-style debugging. Log methods of interest, with information automatically
 * logged based on stack trace.
 */
public class Debugger {
	public static final Debugger DBG = new Debugger(System.err, 8, 0); // Global debugger
	private final PrintStream stream;
	private final int traceStartIndex;
	private final long stopCount;
	private final Map<Caller, Integer> methodCounts = new HashMap<>();
	private long totalCalls = 0;

	/**
	 * Creates a debugger writing to given stderr.
	 */
	public static Debugger of() {
		return of(System.err, 0, 0);
	}
	
	/**
	 * Creates a debugger writing to given stream, indent starting at given index in stack trace,
	 * stopping output after given number of method calls to the debugger.
	 */
	public static Debugger of(PrintStream stream, int traceStartIndex, int stopCount) {
		return new Debugger(stream, traceStartIndex, stopCount);
	}

	private Debugger(PrintStream stream, int traceStartIndex, int stopCount) {
		this.stream = stream;
		this.traceStartIndex = traceStartIndex;
		this.stopCount = stopCount;
	}

	/**
	 * Write caller info to log, with given message. Use this to log additional info within a
	 * method.
	 */
	public void log(Object... objs) {
		if (shouldStop(totalCalls++)) return;
		Caller caller = ReflectUtil.previousCaller(1);
		StringBuilder b = new StringBuilder();
		if (objs != null) for (int i = 0; i < objs.length; i++) {
			if (i > 0) b.append(", ");
			b.append(objs[i]);
		}
		print(caller, 1, b.toString());
	}

	/**
	 * Write caller info to log, increment method count, and output current count. Use this to log
	 * when a method is called.
	 */
	public void method(Object... objs) {
		if (shouldStop(totalCalls++)) return;
		Caller caller = ReflectUtil.previousCaller(1);
		int count = incrementMethodCount(caller);
		print(caller, 1, count, objs);
	}

	private void print(Caller caller, int indentOffset, Object msg, Object... objs) {
		StringBuilder b = new StringBuilder();
		int indents = indents(++indentOffset); // don't count this method
		while (indents-- > 0)
			b.append("  ");
		b.append(caller.cls).append('.').append(caller.method).append('(');
		if (objs != null) for (int i = 0; i < objs.length; i++) {
			if (i > 0) b.append(", ");
			b.append(objs[i]);
		}
		b.append(") (").append(caller.file).append(':').append(caller.line).append(") ");
		b.append(msg);
		stream.println(b.toString());
	}

	private int indents(int indentOffset) {
		StackTraceElement[] e = Thread.currentThread().getStackTrace();
		int len = e.length - ++indentOffset; // don't count this method
		if (len <= traceStartIndex) return 0;
		return len - traceStartIndex;
	}

	private boolean shouldStop(long totalCalls) {
		if (stopCount == 0) return false;
		return totalCalls >= stopCount;
	}

	private int incrementMethodCount(Caller caller) {
		int count = getMethodCount(caller);
		methodCounts.put(caller, count + 1);
		return count;
	}

	private int getMethodCount(Caller caller) {
		Integer i = methodCounts.get(caller);
		if (i == null) return 0;
		return i;
	}

}
