package ceri.common.util;

import java.util.Objects;

/**
 * Wraps a safe resource reference, with access via public field, to prevent try-with-resource
 * warnings. Useful as a function return; use only if the resource is closed elsewhere.
 */
public class Safe<T extends AutoCloseable> {
	private static final Safe<?> NULL = new Safe<>(null);
	public final T res;

	public static <T extends AutoCloseable> Safe<T> empty() {
		return BasicUtil.uncheckedCast(NULL);
	}

	public static <T extends AutoCloseable> Safe<T> of(T res) {
		return res == null ? empty() : new Safe<>(res);
	}

	private Safe(T res) {
		this.res = res;
	}

	public boolean valid() {
		return res != null;
	}

	@SuppressWarnings("resource")
	public Safe<T> validate() {
		Objects.requireNonNull(res);
		return this;
	}
}
