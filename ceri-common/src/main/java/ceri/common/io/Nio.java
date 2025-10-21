package ceri.common.io;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import ceri.common.concurrent.Concurrent;
import ceri.common.function.Excepts.Consumer;

public class Nio {

	private Nio() {}

	public static void selectKeys(Selector selector, Consumer<IOException, SelectionKey> consumer)
		throws IOException, InterruptedException {
		Concurrent.checkInterrupted();
		for (var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove())
			consumer.accept(i.next());
	}

	public static void clearKeys(Set<SelectionKey> selectedKeys) {
		for (var i = selectedKeys.iterator(); i.hasNext(); i.remove())
			i.next();
	}
}
