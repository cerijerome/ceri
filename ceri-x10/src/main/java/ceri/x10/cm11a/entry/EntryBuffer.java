package ceri.x10.cm11a.entry;

import static ceri.common.validation.ValidationUtil.validateMax;
import static ceri.common.validation.ValidationUtil.validateRange;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import ceri.common.collection.ImmutableUtil;
import ceri.common.data.ByteArray.Encoder;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteUtil;
import ceri.common.data.ByteWriter;
import ceri.common.text.ToStringHelper;
import ceri.x10.command.Command;

/**
 * Encapsulates the buffer received from an CM11a. Contains a list of addresses and functions, and
 * is able to convert to and from bytes.
 */
public class EntryBuffer {
	private static final int HEADER_SIZE = 2;
	private static final int MAX_DATA_BYTES = 8;
	private static final int MAX_BYTES = HEADER_SIZE + MAX_DATA_BYTES;
	public final List<Entry> entries;

	/**
	 * Creates a data buffer by reading from the given input stream.
	 */
	public static EntryBuffer decode(ByteReader r) {
		int count = r.readUbyte();
		validateRange(count, HEADER_SIZE - 1, MAX_BYTES - 1);
		List<Entry> entries = new ArrayList<>();
		int bits = r.readUbyte();
		for (int i = 0; i < count - 1;) {
			boolean isFunction = ByteUtil.bit(bits, i);
			Entry entry = Receive.decode(isFunction, r);
			entries.add(entry);
			i += Receive.size(entry);
		}
		return EntryBuffer.of(entries);
	}

	/**
	 * Creates an InputBuffer from given command.
	 */
	public static List<EntryBuffer> allFrom(Command command) {
		List<Entry> inputs = Entry.allFrom(command);
		return allFrom(inputs);
	}

	/**
	 * Creates InputBuffers from given collection of address/function inputs. As inputs fill each
	 * InputBuffer, a new one is created to hold subsequent inputs.
	 */
	public static List<EntryBuffer> allFrom(Collection<Entry> entries) {
		List<EntryBuffer> buffers = new ArrayList<>();
		List<Entry> bufferEntries = new ArrayList<>();
		int count = 0;
		for (Entry entry : entries) {
			int len = Receive.size(entry);
			if (count + len > MAX_DATA_BYTES && !bufferEntries.isEmpty()) {
				buffers.add(new EntryBuffer(bufferEntries));
				bufferEntries.clear();
				count = 0;
			}
			bufferEntries.add(entry);
			count += len;
		}
		if (!bufferEntries.isEmpty()) buffers.add(new EntryBuffer(bufferEntries));
		return buffers;
	}

	/**
	 * Collects all inputs from collection of InputBuffers.
	 */
	public static Collection<Entry> combine(Collection<EntryBuffer> buffers) {
		List<Entry> inputs = new ArrayList<>();
		for (EntryBuffer buffer : buffers)
			inputs.addAll(buffer.entries);
		return inputs;
	}

	private static EntryBuffer of(Collection<Entry> entries) {
		validateMax(size(entries), MAX_DATA_BYTES, "Total entry size");
		return new EntryBuffer(ImmutableUtil.copyAsList(entries));
	}

	private EntryBuffer(List<Entry> entries) {
		this.entries = entries;
	}

	public int size() {
		return size(entries);
	}

	/**
	 * Creates a byte array from count, bits, and entries.
	 */
	public ByteProvider encode() {
		return Encoder.fixed(size()).apply(encoder -> encode(encoder)).immutable();
	}

	/**
	 * Writes bytes for count, bits, and entries.
	 */
	public void encode(ByteWriter<?> w) {
		int count = size() - 1;
		int bits = 0;
		List<ByteProvider> encodeds = new ArrayList<>();
		for (Entry entry : entries) {
			if (!entry.isAddress()) bits |= 1 << count;
			encodeds.add(Receive.encode(entry));
		}
		w.writeBytes(count, bits);
		encodeds.forEach(encoded -> w.writeFrom(encoded));
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this).children(entries).toString();
	}

	private static int size(Collection<Entry> entries) {
		return entries.stream().mapToInt(Receive::size).sum() + HEADER_SIZE;
	}

}
