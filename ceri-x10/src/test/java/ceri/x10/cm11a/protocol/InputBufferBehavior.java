package ceri.x10.cm11a.protocol;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import ceri.x10.cm11a.Entry;
import ceri.x10.cm11a.EntryDispatcher;
import ceri.x10.command.CommandFactory;
import ceri.x10.type.Address;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.Function;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;

public class InputBufferBehavior {

	@Test
	public void shouldCreateFromCommand() {
		InputBuffer buffer = InputBuffer.create(CommandFactory.extended("P16", 0xff, 0xff));
		assertCollection(buffer.entries, new Entry(Address.fromString("P16")), new Entry(
			new ExtFunction(House.P, (byte) 0xff, (byte) 0xff)));
	}

	@Test
	public void shouldCreateMultipleInputBuffersIfMaxSizeIsExceeded() {
		Collection<Entry> entries = new ArrayList<>();
		entries.addAll(EntryDispatcher.toEntries(CommandFactory.extended("A1", 0, 0)));
		entries.addAll(EntryDispatcher.toEntries(CommandFactory.extended("B1", 0, 0)));
		entries.addAll(EntryDispatcher.toEntries(CommandFactory.extended("C1", 0, 0)));
		Collection<InputBuffer> buffers = InputBuffer.create(entries);
		assertThat(buffers.size(), is(2));
	}

	@Test
	public void shouldCombineInputBuffersIntoEntries() {
		Entry entry1 = new Entry(Address.fromString("A1"));
		Entry entry2 = new Entry(new Function(House.A, FunctionType.ON));
		Entry entry3 = new Entry(Address.fromString("B1"));
		Entry entry4 = new Entry(new Function(House.B, FunctionType.ON));
		Collection<InputBuffer> buffers = new ArrayList<>();
		buffers.addAll(InputBuffer.create(Arrays.asList(entry1, entry2)));
		buffers.addAll(InputBuffer.create(Arrays.asList(entry3, entry4)));
		assertCollection(InputBuffer.combine(buffers), Arrays
			.asList(entry1, entry2, entry3, entry4));
	}

}
