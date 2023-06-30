package ceri.common.net;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import ceri.common.util.BasicUtil;

/**
 * Type-safe map of socket options and values.
 */
public class TcpSocketOptions {
	protected final Map<TcpSocketOption<?>, Object> map;

	public static TcpSocketOptions from(TcpSocket socket) throws IOException {
		var options = of();
		for (var option : TcpSocketOption.all)
			options.set(option, BasicUtil.uncheckedCast(socket.option(option)));
		return options.immutable();
	}

	public static TcpSocketOptions.Mutable of() {
		return of(LinkedHashMap::new);
	}

	public static TcpSocketOptions.Mutable
		of(Supplier<Map<TcpSocketOption<?>, Object>> mapSupplier) {
		return new Mutable(mapSupplier.get());
	}

	public static class Mutable extends TcpSocketOptions {

		private Mutable(Map<TcpSocketOption<?>, Object> map) {
			super(map);
		}

		public TcpSocketOptions set(TcpSocketOptions options) {
			map.putAll(options.map);
			return this;
		}

		public <T> TcpSocketOptions set(TcpSocketOption<T> option, T value) {
			Objects.requireNonNull(option);
			Objects.requireNonNull(value);
			map.put(option, value);
			return this;
		}

		public TcpSocketOptions immutable() {
			return new TcpSocketOptions(Map.copyOf(map));
		}
	}

	private TcpSocketOptions(Map<TcpSocketOption<?>, Object> map) {
		this.map = map;
	}

	public Set<TcpSocketOption<?>> options() {
		return Collections.unmodifiableSet(map.keySet());
	}

	public boolean has(TcpSocketOption<?> option) {
		return map.containsKey(option);
	}

	public <T> T get(TcpSocketOption<T> option) {
		return BasicUtil.uncheckedCast(map.get(option));
	}

	public void applyAll(TcpSocket socket) throws IOException {
		for (var option : map.keySet())
			apply(option, socket);
	}

	public <T> boolean apply(TcpSocketOption<T> option, TcpSocket socket) throws IOException {
		T value = get(option);
		if (value == null) return false;
		socket.option(option, value);
		return true;
	}

}
