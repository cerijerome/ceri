package ceri.common.io;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.ExceptionConsumer;

public class NioUtil {

	private NioUtil() {}

	public static void selectKeys(Selector selector,
		ExceptionConsumer<IOException, SelectionKey> consumer)
		throws IOException, InterruptedException {
		ConcurrentUtil.checkInterrupted();
		for (var i = selector.selectedKeys().iterator(); i.hasNext(); i.remove())
			consumer.accept(i.next());
	}

	public static void clearKeys(Set<SelectionKey> selectedKeys) {
		for (var i = selectedKeys.iterator(); i.hasNext(); i.remove())
			i.next();
	}

}
