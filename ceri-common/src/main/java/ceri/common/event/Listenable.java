package ceri.common.event;

import java.util.function.Consumer;

public interface Listenable<T> {

	boolean listen(Consumer<? super T> listener);

	boolean unlisten(Consumer<? super T> listener);

}
