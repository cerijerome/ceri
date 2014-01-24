package ceri.ci.zwave;

import java.io.IOException;
import ceri.zwave.veralite.VeraLite;

public class ZWaveController {
	private final VeraLite veraLite;

	public ZWaveController(VeraLite veraLite) {
		this.veraLite = veraLite;
	}

	public void off(int device) throws IOException {
		veraLite.switchPower.off(device);
	}

	public void on(int device) throws IOException {
		veraLite.switchPower.on(device);
	}

}
