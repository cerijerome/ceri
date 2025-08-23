package ceri.jna.util;

import java.util.List;
import ceri.common.collection.Iterables;
import ceri.common.function.Accessible;
import ceri.common.function.Excepts.Consumer;
import ceri.common.function.Excepts.Function;
import ceri.common.function.Functions;
import ceri.common.util.OsUtil;

/**
 * Supported OS types for JNA code.
 */
public enum JnaOs implements Accessible<JnaOs> {
	unknown(null, null),
	mac("Mac", "__APPLE__"),
	linux("Linux", "__linux__");

	/** The list of supported OS types. */
	public static final List<JnaOs> KNOWN = List.of(mac, linux);
	/** Empty array */
	public static final JnaOs[] NONE = new JnaOs[0];
	/** The OS arch name */
	public final String name;
	/** The OS c code #ifdef check */
	public final String cdefine;

	/**
	 * Returns true if the given OS is not null and is known.
	 */
	public static boolean known(JnaOs os) {
		return os != null && os.known();
	}

	/**
	 * Apply the consumer to known OS types.
	 */
	public static <E extends Exception> void forEach(Consumer<E, JnaOs> consumer) throws E {
		Iterables.forEach(KNOWN, os -> os.accept(consumer));
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
		return from(OsUtil.os());
	}

	/**
	 * Determine the OS type.
	 */
	public static JnaOs from(OsUtil.Os os) {
		if (os == null) return unknown;
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

	/**
	 * Returns true if this OS is supported.
	 */
	public boolean known() {
		return this != unknown;
	}

	/**
	 * Overrides the current OS type with this OS type. Close to revert the override.
	 */
	public Functions.Closeable override() {
		return OsUtil.os(name, null, null);
	}

	/**
	 * Overrides the current OS to execute the function, passing in this OS type.
	 */
	@Override
	public <E extends Exception, T> T apply(Function<E, JnaOs, T> function) throws E {
		try (var _ = override()) {
			return function.apply(this);
		}
	}

	/**
	 * Adds the OS name to the end of the filename.
	 */
	public String file(String file) {
		if (file == null || !known()) return file;
		int i = file.lastIndexOf('.');
		if (i < 0) return file + "-" + name();
		return file.substring(0, i) + "-" + name() + file.substring(i);
	}
}
