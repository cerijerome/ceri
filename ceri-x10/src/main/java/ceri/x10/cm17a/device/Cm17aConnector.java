package ceri.x10.cm17a.device;

import java.io.IOException;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;

public interface Cm17aConnector extends Listenable.Indirect<StateChange> {

	void setRts(boolean on) throws IOException;

	void setDtr(boolean on) throws IOException;

}
