package ceri.common.reflect;

import ceri.common.util.EqualsUtil;
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

	Caller(String fullCls, int line, String method, String file) {
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
		return new Caller(s.getClassName(), s.getLineNumber(), s.getMethodName(), s.getFileName());
	}

	public Class<?> cls() {
		try {
			return Class.forName(fullCls);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Caller)) return false;
		Caller other = (Caller) obj;
		if (line != other.line) return false;
		if (!EqualsUtil.equals(fullCls, other.fullCls)) return false;
		if (!EqualsUtil.equals(method, other.method)) return false;
		if (!EqualsUtil.equals(file, other.file)) return false;
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
