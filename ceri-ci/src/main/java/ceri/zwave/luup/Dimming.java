package ceri.zwave.luup;

import java.io.IOException;
import ceri.zwave.veralite.CommandFactory;

public class Dimming {
	private static final int MAX_LEVEL = 99;
	public static final String sid = "urn:upnp-org:serviceId:Dimming1";
	private final CommandFactory factory;

	public static enum Action implements ceri.zwave.veralite.Action {
		SetLoadLevelTarget;
	}

	public static enum Variable implements ceri.zwave.veralite.Variable {
		LoadLevelStatus,
		LoadLevelTarget;
	}

	public Dimming(CommandFactory factory) {
		this.factory = factory;
	}

	public void level(int device, int level) throws IOException {
		if (level > MAX_LEVEL) level = MAX_LEVEL;
		if (level < 0) level = 0;
		factory.action(Action.SetLoadLevelTarget, sid).device(device).targetValue(level).execute();
	}

	public void on(int device) throws IOException {
		level(device, MAX_LEVEL);
	}

	public void off(int device) throws IOException {
		level(device, 0);
	}

}
