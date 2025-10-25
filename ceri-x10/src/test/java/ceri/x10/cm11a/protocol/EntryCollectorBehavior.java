package ceri.x10.cm11a.protocol;

import static ceri.common.test.Assert.assertOrdered;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.log.test.LogModifier;
import ceri.x10.command.Command;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

public class EntryCollectorBehavior {

	@Test
	public void shouldDropNonMatchingEntries() {
		LogModifier.run(() -> {
			EntryCollector collector = new EntryCollector(_ -> {});
			collector.collect(Entry.function(House.G, FunctionType.on));
		}, Level.ERROR, EntryCollector.class);
	}

	@Test
	public void shouldResetAddressAfterFunction() {
		List<Command> list = new ArrayList<>();
		EntryCollector collector = new EntryCollector(list::add);
		collector.collect(Entry.address(House.E, Unit._1));
		collector.collect(Entry.address(House.E, Unit._2));
		collector.collect(Entry.function(House.E, FunctionType.on));
		collector.collect(Entry.address(House.E, Unit._3));
		collector.collect(Entry.function(House.E, FunctionType.on));
		assertOrdered(list, Command.on(House.E, Unit._1, Unit._2), Command.on(House.E, Unit._3));
	}
}
