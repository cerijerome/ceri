package ceri.x10.cm11a.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import ceri.common.collection.ImmutableUtil;
import ceri.common.data.ByteArrayDataInput;
import ceri.common.data.ByteArrayDataOutput;
import ceri.common.text.ToStringHelper;
import ceri.x10.cm11a.Entry;
import ceri.x10.cm11a.EntryDispatcher;
import ceri.x10.command.BaseCommand;
import ceri.x10.util.UnexpectedByteException;

/**
 * Input data buffer from CM11A. Contains a list of addresses and functions, and is able to convert
 * between to and from byte arrays.
 */
public class InputBuffer {
	private static final int HEADER_BYTES = 2;
	private static final int MAX_DATA_BYTES = 8;
	private static final int MAX_BYTES = HEADER_BYTES + MAX_DATA_BYTES;
	public Collection<Entry> entries;

	private InputBuffer(Collection<Entry> entries) {
		int count = 0;
		for (Entry entry : entries)
			count += Data.read.sizeInBytes(entry);
		if (count > MAX_DATA_BYTES) throw new IllegalArgumentException(
			"Size of entry data has exceeded " + MAX_DATA_BYTES + ": " + count);
		this.entries = ImmutableUtil.copyAsList(entries);
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this).children(entries).toString();
	}

	/**
	 * Creates a byte array from address and function entries.
	 */
	public void writeTo(DataOutput out) throws IOException {
		byte[] data = new byte[MAX_BYTES];
		int count = 0;
		BitSet bits = new BitSet(MAX_DATA_BYTES);
		ByteArrayDataOutput bOut = new ByteArrayDataOutput(data, HEADER_BYTES);
		for (Entry entry : entries) {
			if (entry.type != Entry.Type.address) bits.set(count);
			Data.read.writeEntryTo(entry, bOut);
			count += Data.read.sizeInBytes(entry);
		}
		data[0] = (byte) (count + 1);
		data[1] = bits.toByteArray()[0];
		if (count < MAX_DATA_BYTES) data = Arrays.copyOf(data, count + HEADER_BYTES);
		out.write(data);
	}

	/**
	 * Creates an InputBuffer from given command.
	 */
	public static InputBuffer create(BaseCommand<?> command) {
		Collection<Entry> entries = EntryDispatcher.toEntries(command);
		Collection<InputBuffer> buffers = create(entries);
		if (buffers.size() != 1) throw new IllegalArgumentException("Should not happen");
		return buffers.iterator().next();
	}

	/**
	 * Creates InputBuffers from given collection of address/function entries. As entries fill each
	 * InputBuffer, a new one is created to hold subsequent entries.
	 */
	public static Collection<InputBuffer> create(Iterable<Entry> entries) {
		Collection<InputBuffer> buffers = new ArrayList<>();
		Collection<Entry> bufferEntries = new ArrayList<>();
		int count = 0;
		for (Entry entry : entries) {
			int len = Data.read.sizeInBytes(entry);
			if (count + len > MAX_DATA_BYTES && !bufferEntries.isEmpty()) {
				buffers.add(new InputBuffer(bufferEntries));
				bufferEntries.clear();
				count = 0;
			}
			bufferEntries.add(entry);
			count += len;
		}
		if (!bufferEntries.isEmpty()) buffers.add(new InputBuffer(bufferEntries));
		return buffers;
	}

	/**
	 * Collects all entries from collection of InputBuffers.
	 */
	public static Collection<Entry> combine(Iterable<InputBuffer> buffers) {
		List<Entry> entries = new ArrayList<>();
		for (InputBuffer buffer : buffers)
			entries.addAll(buffer.entries);
		return entries;
	}

	/**
	 * Creates a data buffer by reading from the given input stream.
	 */
	public static InputBuffer readFrom(DataInput in) throws IOException {
		byte count = in.readByte();
		if (count < HEADER_BYTES || count >= MAX_BYTES) throw new UnexpectedByteException(count);
		byte[] data = new byte[count];
		in.readFully(data);
		in = new ByteArrayDataInput(data, 0);
		List<Entry> entries = new ArrayList<>();
		BitSet bits = BitSet.valueOf(new byte[] { in.readByte() });
		int i = 0;
		while (i < count - 1) {
			boolean isFunction = bits.get(i);
			Entry entry = Data.read.readEntryFrom(isFunction, in);
			entries.add(entry);
			i += Data.read.sizeInBytes(entry);
		}
		return new InputBuffer(entries);
	}

}
