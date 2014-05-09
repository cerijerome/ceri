package ceri.ci.zwave;

import java.util.Collection;

public interface ZWaveFactory {

	ZWaveController createController(String host);
	ZWaveAlerter.Builder builder(ZWaveController controller);
	ZWaveGroup createGroup(ZWaveController controller, Collection<Integer> devices);

}
