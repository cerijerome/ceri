package ceri.ci.zwave;

import ceri.ci.zwave.ZWaveAlerter.Builder;
import ceri.zwave.veralite.VeraLite;

public class ZWaveFactoryImpl implements ZWaveFactory {

	@Override
	public ZWaveController createController(String host) {
		return new ZWaveController(new VeraLite(host));
	}

	@Override
	public Builder builder(ZWaveController controller) {
		return ZWaveAlerter.builder(controller);
	}

}
