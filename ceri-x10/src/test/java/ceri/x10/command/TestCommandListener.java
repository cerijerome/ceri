package ceri.x10.command;

import ceri.common.concurrent.ValueCondition;

public class TestCommandListener implements Command.Listener {
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
	public void dim(Command.Dim command) {
		sync.signal(command);
	}

	@Override
	public void bright(Command.Dim command) {
		sync.signal(command);
	}

	@Override
	public void ext(Command.Ext command) {
		sync.signal(command);
	}
}
