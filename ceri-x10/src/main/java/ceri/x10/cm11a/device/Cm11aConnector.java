package ceri.x10.cm11a.device;

import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;

public interface Cm11aConnector extends Listenable.Indirect<StateChange> {
	InputStream in();

	OutputStream out();

}
