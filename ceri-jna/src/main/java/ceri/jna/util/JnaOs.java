package ceri.jna.util;

import java.util.List;
import ceri.common.collection.CollectionUtil;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.Functional;
import ceri.common.function.RuntimeCloseable;
import ceri.common.util.OsUtil;

/**
 * Supported OS types for JNA code.
 */
public enum JnaOs implements Functional<JnaOs> {
	unknown(null, null),
	mac("Mac", "__APPLE__"),
	linux("Linux", "__linux__");

	public static final List<JnaOs> KNOWN = List.of(mac, linux);
	/** The OS arch name */
	public final String name;
	/** The OS c code #ifdef check */
	public final String cdefine;

	public static boolean known(JnaOs os) {
		return os != null && os.known();
	}

	/**
	 * Apply the consumer to known OS types.
	 */
	public static <E extends Exception> void forEach(ExceptionConsumer<E, JnaOs> consumer)
		throws E {
		CollectionUtil.forEach(KNOWN, os -> os.accept(consumer));
	}

	/**
	 * Returns true if the given OS types are compatible.
	 */
	public static boolean compatible(JnaOs os1, JnaOs os2) {
		return !known(os1) || !known(os2) || os1 == os2;
	}
	
	/**
	 * Returns true if the given OS type is compatible with the current OS type.
	 */
	public static boolean current(JnaOs os) {
		return !known(os) || compatible(os, current());
	}
	
	/**
	 * Determine the current OS type.
	 */
	public static JnaOs current() {
		var os = OsUtil.os();
		if (os.mac) return mac;
		if (os.linux) return linux;
		return unknown;
	}

	/**
	 * Determine the current OS type, throw an exception if unknown.
	 */
	public static JnaOs validCurrent() {
		var os = current();
		if (os.known()) return os;
		throw new IllegalStateException("Unsupported OS: " + OsUtil.os());
	}

	private JnaOs(String name, String cdefine) {
		this.name = name;
		this.cdefine = cdefine;
	}

	public boolean known() {
		return this != unknown;
	}

	public RuntimeCloseable override() {
		return OsUtil.os(name, null, null);
	}

	@Override
	public <E extends Exception, T> T apply(ExceptionFunction<E, JnaOs, T> function) throws E {
		try (var _ = override()) {
			return function.apply(this);
		}
	}

	public String file(String file) {
		if (file == null || !known()) return file;
		int i = file.lastIndexOf('.');
		if (i < 0) return file + "-" + name();
		return file.substring(0, i) + "-" + name() + file.substring(i);
	}
}
