package ceri.zwave.veralite;

import ceri.zwave.command.CommandFactory;
import ceri.zwave.command.DelayExecutor;
import ceri.zwave.upnp.Dimming;
import ceri.zwave.upnp.SwitchPower;
import ceri.zwave.upnp.ZWaveNetwork;

public class VeraLite {
	public final ZWaveNetwork zWaveNetwork;
	public final SwitchPower switchPower;
	public final Dimming dimming;

	public VeraLite(String host, int callDelayMs) {
		if (host == null || host.isEmpty())
			throw new IllegalArgumentException("Host must be specified");
		CommandFactory factory = new CommandFactory(host, new DelayExecutor(callDelayMs));
		zWaveNetwork = new ZWaveNetwork(factory);
		switchPower = new SwitchPower(factory);
		dimming = new Dimming(factory);
	}

}
