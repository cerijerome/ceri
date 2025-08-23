package ceri.x10.cm11a.protocol;

import static ceri.common.validation.ValidationUtil.validateMax;
import static ceri.common.validation.ValidationUtil.validateRange;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import ceri.common.collection.Immutable;
import ceri.common.collection.Lists;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteUtil;
import ceri.common.data.ByteWriter;
import ceri.common.text.ToString;
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
		var entries = Lists.<Entry>of();
		int bits = r.readUbyte();
		for (int i = 0; i < count - 1;) {
			boolean isFunction = ByteUtil.bit(bits, i);
			var entry = Receive.decode(isFunction, r);
			entries.add(entry);
			i += Receive.size(entry);
		}
		return EntryBuffer.of(entries);
	}

	/**
	 * Creates an EntryBuffer from given command.
	 */
	public static List<EntryBuffer> allFrom(Command command) {
		List<Entry> entries = Entry.allFrom(command);
		return allFrom(entries);
	}

	/**
	 * Creates EntryBuffers from given collection of address/function inputs. As inputs fill each
	 * EntryBuffer, a new one is created to hold subsequent inputs.
	 */
	public static List<EntryBuffer> allFrom(Collection<Entry> entries) {
		var buffers = Lists.<EntryBuffer>of();
		var bufferEntries = Lists.<Entry>of();
		int count = 0;
		for (Entry entry : entries) {
			int len = Receive.size(entry);
			if (count + len > MAX_DATA_BYTES) {
				buffers.add(EntryBuffer.of(bufferEntries));
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
	 * Collects all inputs from collection of EntryBuffers.
	 */
	public static Collection<Entry> combine(EntryBuffer... buffers) {
		return combine(Arrays.asList(buffers));
	}

	/**
	 * Collects all inputs from collection of EntryBuffers.
	 */
	public static Collection<Entry> combine(Collection<EntryBuffer> buffers) {
		List<Entry> inputs = Lists.of();
		for (EntryBuffer buffer : buffers)
			inputs.addAll(buffer.entries);
		return inputs;
	}

	public static EntryBuffer of(Entry... entries) {
		return of(Arrays.asList(entries));
	}

	public static EntryBuffer of(Collection<Entry> entries) {
		validateMax(size(entries), MAX_BYTES, "Total entry size");
		return new EntryBuffer(Immutable.list(entries));
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
		return ByteArray.Encoder.fixed(size()).apply(encoder -> encode(encoder)).immutable();
	}

	/**
	 * Writes bytes for count, bits, and entries.
	 */
	public void encode(ByteWriter<?> w) {
		int count = 0;
		int bits = 0;
		List<ByteProvider> encodeds = Lists.of();
		for (Entry entry : entries) {
			if (!entry.isAddress()) bits |= 1 << count;
			count += Receive.size(entry);
			encodeds.add(Receive.encode(entry));
		}
		w.writeBytes(size() - 1, bits);
		encodeds.forEach(encoded -> w.writeFrom(encoded));
	}

	@Override
	public int hashCode() {
		return Objects.hash(entries);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof EntryBuffer other)) return false;
		return Objects.equals(entries, other.entries);
	}

	@Override
	public String toString() {
		return ToString.ofClass(this).childrens(entries).toString();
	}

	private static int size(Collection<Entry> entries) {
		return entries.stream().mapToInt(Receive::size).sum() + HEADER_SIZE;
	}
}
