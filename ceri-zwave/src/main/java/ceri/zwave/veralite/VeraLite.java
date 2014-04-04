package ceri.zwave.veralite;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import ceri.common.util.BasicUtil;
import ceri.zwave.command.CommandFactory;
import ceri.zwave.command.DelayExecutor;
import ceri.zwave.upnp.Dimming;
import ceri.zwave.upnp.SwitchPower;
import ceri.zwave.upnp.ZWaveNetwork;

public class VeraLite {
	public final ZWaveNetwork zWaveNetwork;
	public final SwitchPower switchPower;
	public final Dimming dimming;

	public static void main(String[] args) throws Exception {
		//final VeraLite vl = new VeraLite("192.168.0.109:3480");
		final VeraLite vl = new VeraLite("10.244.160.105:3480");

		ExecutorService exs = Executors.newFixedThreadPool(1);
		exs.execute(r(vl, 6, 400, 6));
		exs.execute(r(vl, 7, 300, 8));
		//exs.shutdown();
		exs.awaitTermination(100, TimeUnit.SECONDS);
	}

	private static Runnable r(final VeraLite vl, final int device, final int delay,
		final int iterations) {
		return () -> {
			try {
				onOff(vl, device, delay, iterations);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static void onOff(VeraLite vl, int device, int delay, int iterations) throws Exception {
		for (int i = 0; i < iterations; i++) {
			vl.switchPower.on(device);
			BasicUtil.delay(delay);
			vl.switchPower.off(device);
			BasicUtil.delay(delay);
		}
	}

	public VeraLite(String host) {
		if (host == null || host.isEmpty()) throw new IllegalArgumentException(
			"Host must be specified");
		CommandFactory factory = new CommandFactory(host, new DelayExecutor(300));
		zWaveNetwork = new ZWaveNetwork(factory);
		switchPower = new SwitchPower(factory);
		dimming = new Dimming(factory);
	}

}
