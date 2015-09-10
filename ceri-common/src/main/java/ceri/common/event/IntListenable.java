package ceri.common.event;

import java.util.function.IntConsumer;

public interface IntListenable {

	boolean listen(IntConsumer listener);

	boolean unlisten(IntConsumer listener);

}
