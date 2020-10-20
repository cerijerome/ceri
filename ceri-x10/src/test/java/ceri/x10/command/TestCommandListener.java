package ceri.x10.command;

import ceri.common.concurrent.ValueCondition;
import ceri.x10.command.Command.Dim;
import ceri.x10.command.Command.Ext;

public class TestCommandListener implements CommandListener {
	public final ValueCondition<Command> sync = ValueCondition.of();

	public static TestCommandListener of() {
		return new TestCommandListener();
	}

	protected TestCommandListener() {}

	@Override
	public void allUnitsOff(Command command) {
		sync.signal(command);
	}

	@Override
	public void allLightsOn(Command command) {
		sync.signal(command);
	}

	@Override
	public void allLightsOff(Command command) {
		sync.signal(command);
	}

	@Override
	public void on(Command command) {
		sync.signal(command);
	}

	@Override
	public void off(Command command) {
		sync.signal(command);
	}

	@Override
	public void dim(Dim command) {
		sync.signal(command);
	}

	@Override
	public void bright(Dim command) {
		sync.signal(command);
	}

	@Override
	public void ext(Ext command) {
		sync.signal(command);
	}
}
