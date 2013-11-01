package ceri.ci.common;

import java.io.Closeable;

public interface Alerter extends Closeable {
	void alert(String...keys);
	void clear(String...keys);
}
