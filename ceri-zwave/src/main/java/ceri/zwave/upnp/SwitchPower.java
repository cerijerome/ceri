package ceri.zwave.upnp;

import java.io.IOException;
import ceri.zwave.command.CommandFactory;

public class SwitchPower {
	public static final String sid = "urn:upnp-org:serviceId:SwitchPower1";
	private final CommandFactory factory;

	public static enum Action implements ceri.zwave.command.Action {
		SetTarget;
	}

	public static enum Variable implements ceri.zwave.command.Variable {
		Status,
		Target;
	}

	public SwitchPower(CommandFactory factory) {
		this.factory = factory;
	}

	private void level(int device, int level) throws IOException {
		factory.action(Action.SetTarget, sid).device(device).targetValue(level).execute();
	}

	public void on(int deviceNumber) throws IOException {
		level(deviceNumber, 1);
	}

	public void off(int deviceNumber) throws IOException {
		level(deviceNumber, 0);
	}

}
