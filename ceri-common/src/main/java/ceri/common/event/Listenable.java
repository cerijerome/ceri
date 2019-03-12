package ceri.common.event;

import java.util.function.Consumer;

public interface Listenable<T> {

	boolean listen(Consumer<? super T> listener);

	boolean unlisten(Consumer<? super T> listener);

	static interface Indirect<T> {
		Listenable<T> listeners();

		static <T> Indirect<T> from(Listenable<T> listenable) {
			return () -> listenable;
		}
	}
}
