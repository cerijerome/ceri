package ceri.x10.cm11a.entry;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import ceri.x10.cm11a.device.Entry;
import ceri.x10.cm11a.device.EntryDispatcher;
import ceri.x10.command.Address;
import ceri.x10.command.CommandFactory;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.Function;

public class InputBufferBehavior {

	@Test
	public void shouldCreateFromCommand() {
		InputBuffer buffer = InputBuffer.from(CommandFactory.extended("P16", 0xff, 0xff));
		assertCollection(buffer.entries, Entry.of(Address.from("P16")),
			Entry.of(ExtFunction.of(House.P, 0xff, 0xff)));
	}

	@Test
	public void shouldCreateMultipleInputBuffersIfMaxSizeIsExceeded() {
		Collection<Entry> entries = new ArrayList<>();
		entries.addAll(EntryDispatcher.toEntries(CommandFactory.extended("A1", 0, 0)));
		entries.addAll(EntryDispatcher.toEntries(CommandFactory.extended("B1", 0, 0)));
		entries.addAll(EntryDispatcher.toEntries(CommandFactory.extended("C1", 0, 0)));
		Collection<InputBuffer> buffers = InputBuffer.allFrom(entries);
		assertThat(buffers.size(), is(2));
	}

	@Test
	public void shouldCombineInputBuffersIntoEntries() {
		Entry entry1 = Entry.of(Address.from("A1"));
		Entry entry2 = Entry.of(Function.of(House.A, FunctionType.on));
		Entry entry3 = Entry.of(Address.from("B1"));
		Entry entry4 = Entry.of(Function.of(House.B, FunctionType.on));
		Collection<InputBuffer> buffers = new ArrayList<>();
		buffers.addAll(InputBuffer.allFrom(Arrays.asList(entry1, entry2)));
		buffers.addAll(InputBuffer.allFrom(Arrays.asList(entry3, entry4)));
		assertCollection(InputBuffer.combine(buffers), Arrays
			.asList(entry1, entry2, entry3, entry4));
	}

}
