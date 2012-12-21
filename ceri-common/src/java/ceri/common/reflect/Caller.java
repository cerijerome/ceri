package ceri.common.reflect;

import ceri.common.util.HashCoder;

/**
 * Encapsulates information about the caller of a method.
 */
public class Caller {
	public static final Caller NULL = new Caller(null, 0, null, null);
	private static final String UNKNOWN = "[Unknown]";
	public final String cls;
	public final String fullCls;
	public final int line;
	public final String method;
	public final String file;
	private final int hashCode;

	private Caller(String fullCls, int line, String method, String file) {
		this.fullCls = fullCls == null ? UNKNOWN : fullCls;
		cls = simpleClassName(fullCls);
		this.line = line;
		this.method = method == null ? UNKNOWN : method;
		this.file = file == null ? UNKNOWN : file;
		hashCode = HashCoder.hash(this.fullCls, this.line, this.method, this.file);
	}

	/**
	 * Create caller information from stack trace element.
	 */
	public static Caller fromStackTraceElement(StackTraceElement s) {
		if (s == null) return NULL;
		return new Caller(s.getClassName(), s.getLineNumber(), s.getMethodName(), s
			.getFileName());
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Caller)) return false;
		Caller info = (Caller)obj;
		if (hashCode != info.hashCode) return false;
		if (!fullCls.equals(info.fullCls)) return false;
		if (line != info.line) return false;
		if (!method.equals(info.method)) return false;
		if (!file.equals(info.file)) return false;
		return true;
	}

	@Override
	public String toString() {
		return fullCls + '.' + method + '(' + file + ':' + line + ')';
	}

	private String simpleClassName(String fullClassName) {
		if (fullClassName == null) return UNKNOWN;
		int index = fullClassName.lastIndexOf('.');
		if (index == -1) return fullClassName;
		return fullClassName.substring(index + 1);
	}

}
