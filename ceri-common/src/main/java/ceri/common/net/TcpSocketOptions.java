package ceri.common.net;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.util.BasicUtil;

/**
 * Type-safe map of socket options and values.
 */
public class TcpSocketOptions {
	protected final Map<TcpSocketOption<?>, Object> map;

	/**
	 * Populate options from the given socket.
	 */
	public static TcpSocketOptions from(TcpSocket socket) throws IOException {
		var options = of();
		for (var option : TcpSocketOption.all)
			options.set(option, BasicUtil.uncheckedCast(socket.option(option)));
		return options.immutable();
	}

	/**
	 * Creates a mutable option container.
	 */
	public static TcpSocketOptions.Mutable of() {
		return of(LinkedHashMap::new);
	}

	/**
	 * Creates a mutable option container using the given map constructor.
	 */
	public static TcpSocketOptions.Mutable
		of(Supplier<Map<TcpSocketOption<?>, Object>> mapSupplier) {
		return new Mutable(mapSupplier.get());
	}

	public static class Mutable extends TcpSocketOptions {

		private Mutable(Map<TcpSocketOption<?>, Object> map) {
			super(map);
		}

		/**
		 * Copy option values.
		 */
		public TcpSocketOptions set(TcpSocketOptions options) {
			map.putAll(options.map);
			return this;
		}

		/**
		 * Set the option value.
		 */
		public <T> TcpSocketOptions set(TcpSocketOption<T> option, T value) {
			Objects.requireNonNull(option);
			Objects.requireNonNull(value);
			map.put(option, value);
			return this;
		}

		/**
		 * Create an immutable copy.
		 */
		public TcpSocketOptions immutable() {
			return new TcpSocketOptions(Map.copyOf(map));
		}
	}

	private TcpSocketOptions(Map<TcpSocketOption<?>, Object> map) {
		this.map = map;
	}

	/**
	 * Provides the set of options with values.
	 */
	public Set<TcpSocketOption<?>> options() {
		return Collections.unmodifiableSet(map.keySet());
	}

	/**
	 * Returns true if the option has been set.
	 */
	public boolean has(TcpSocketOption<?> option) {
		return map.containsKey(option);
	}

	/**
	 * Returns the option value, or null if unset.
	 */
	public <T> T get(TcpSocketOption<T> option) {
		return BasicUtil.uncheckedCast(map.get(option));
	}

	/**
	 * Applies all options.
	 */
	public void applyAll(TcpSocket socket) throws IOException {
		for (var option : map.keySet())
			apply(option, socket);
	}

	/**
	 * Applies the option only if set.
	 */
	public <T> boolean apply(TcpSocketOption<T> option, TcpSocket socket) throws IOException {
		return apply(option, socket::option);
	}

	/**
	 * Applies the option only if set.
	 */
	public <E extends Exception, T> boolean apply(TcpSocketOption<T> option,
		ExceptionBiConsumer<E, TcpSocketOption<T>, T> consumer) throws E {
		T value = get(option);
		if (value == null) return false;
		consumer.accept(option, value);
		return true;
	}
}
