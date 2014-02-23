package ceri.x10.cm11a;

import static ceri.common.test.TestUtil.assertCollection;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.CommandFactory;
import ceri.x10.command.DimCommand;
import ceri.x10.command.ExtCommand;
import ceri.x10.command.HouseCommand;
import ceri.x10.command.UnitCommand;

public class EntryDispatcherBehavior {
	private List<BaseCommand<?>> commands = new ArrayList<>();
	private EntryDispatcher dispatcher;
	
	@Before
	public void init() {
		commands.clear();
		dispatcher = new EntryDispatcher(commands);
	}
	
	@Test
	public void shouldDispatchExtendedCommands() {
		ExtCommand command = CommandFactory.extended("B2",  Byte.MAX_VALUE,  Byte.MIN_VALUE);
		dispatcher.dispatch(EntryDispatcher.toEntries(command));
		assertCollection(commands, command);
	}

	@Test
	public void shouldDispatchDimCommands() {
		DimCommand command1 = CommandFactory.dim("C3", 100);
		dispatcher.dispatch(EntryDispatcher.toEntries(command1));
		DimCommand command2 = CommandFactory.bright("C3", 100);
		dispatcher.dispatch(EntryDispatcher.toEntries(command2));
		assertCollection(commands, command1, command2);
	}

	@Test
	public void shouldDispatchUnitCommands() {
		UnitCommand command1 = CommandFactory.on("D4");
		dispatcher.dispatch(EntryDispatcher.toEntries(command1));
		UnitCommand command2 = CommandFactory.off("D4");
		dispatcher.dispatch(EntryDispatcher.toEntries(command2));
		assertCollection(commands, command1, command2);
	}

	@Test
	public void shouldDispatchHouseCommands() {
		HouseCommand command1 = CommandFactory.allLightsOff('E');
		dispatcher.dispatch(EntryDispatcher.toEntries(command1));
		HouseCommand command2 = CommandFactory.allLightsOn('E');
		dispatcher.dispatch(EntryDispatcher.toEntries(command2));
		HouseCommand command3 = CommandFactory.allUnitsOff('E');
		dispatcher.dispatch(EntryDispatcher.toEntries(command3));
		assertCollection(commands, command1, command2, command3);
	}


}
