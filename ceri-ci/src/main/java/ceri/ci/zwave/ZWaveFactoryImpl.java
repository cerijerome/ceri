package ceri.ci.zwave;

import java.io.IOException;
import ceri.ci.zwave.ZWaveAlerter.Builder;
import ceri.zwave.veralite.VeraLite;

public class ZWaveFactoryImpl implements ZWaveFactory {

	@Override
	public ZWaveController createController(String host) {
		final VeraLite veraLite = new VeraLite(host);
		return new ZWaveController() {
			@Override
			public void off(int device) throws IOException {
				veraLite.switchPower.off(device);
			}

			@Override
			public void on(int device) throws IOException {
				veraLite.switchPower.on(device);
			}
		};
	}

	@Override
	public Builder builder(ZWaveController controller) {
		return ZWaveAlerter.builder(controller);
	}

}
