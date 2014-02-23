package ceri.x10.cm17a;

import java.io.Closeable;

public interface Cm17aConnector extends Closeable {

	void setRts(boolean on);

	void setDtr(boolean on);

}
