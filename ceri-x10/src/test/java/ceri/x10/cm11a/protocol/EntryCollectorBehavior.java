package ceri.x10.cm11a.protocol;

import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.x10.command.FunctionType.on;
import static ceri.x10.command.House.E;
import static ceri.x10.command.House.G;
import static ceri.x10.command.Unit._1;
import static ceri.x10.command.Unit._2;
import static ceri.x10.command.Unit._3;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.log.test.LogModifier;
import ceri.x10.command.Command;

public class EntryCollectorBehavior {

	@Test
	public void shouldDropNonMatchingEntries() {
		LogModifier.run(() -> {
			EntryCollector collector = new EntryCollector(_ -> {});
			collector.collect(Entry.function(G, on));
		}, Level.ERROR, EntryCollector.class);
	}

	@Test
	public void shouldResetAddressAfterFunction() {
		List<Command> list = new ArrayList<>();
		EntryCollector collector = new EntryCollector(list::add);
		collector.collect(Entry.address(E, _1));
		collector.collect(Entry.address(E, _2));
		collector.collect(Entry.function(E, on));
		collector.collect(Entry.address(E, _3));
		collector.collect(Entry.function(E, on));
		assertIterable(list, Command.on(E, _1, _2), Command.on(E, _3));
	}

}
