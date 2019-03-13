package ceri.common.event;

import java.util.function.Consumer;
import ceri.common.util.BasicUtil;

/**
 * A no-op listenable type.
 */
public class NullListenable<T> implements Listenable<T>, Listenable.Indirect<T> {
	private static final NullListenable<?> INSTANCE = new NullListenable<>();

	public static <T> NullListenable<T> of() {
		return BasicUtil.uncheckedCast(INSTANCE);
	}

	private NullListenable() {}

	@Override
	public Listenable<T> listeners() {
		return this;
	}

	@Override
	public boolean listen(Consumer<? super T> listener) {
		return false;
	}

	@Override
	public boolean unlisten(Consumer<? super T> listener) {
		return false;
	}

}
