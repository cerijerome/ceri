package ceri.x10.cm11a;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

public interface Cm11aConnector extends Closeable {

	InputStream in();

	OutputStream out();

}
