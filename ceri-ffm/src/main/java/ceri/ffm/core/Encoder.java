package ceri.ffm.core;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.util.List;
import ceri.common.collect.Lists;
import ceri.common.function.Functions;
import ceri.common.io.Direction;

/**
 * Dynamically encodes types to memory.
 */
public class Encoder {
	private final List<Functions.Consumer<MemorySegment>> encodings = Lists.of();
	private final List<Functions.Consumer<MemorySegment>> updates = Lists.of();
	private final Direction direction;
	private final long alignment;
	private long offset = 0;

	// TODO:
	// - add padding for alignment?
	// - track max alignment rather than constructor?

	/**
	 * Returns a new encoder.
	 */
	public static Encoder of(Direction direction, long alignment) {
		return new Encoder(direction, alignment);
	}

	private Encoder(Direction direction, long alignment) {
		this.direction = direction;
		this.alignment = alignment;
	}

	/**
	 * Returns true if the encoder is expecting type encoding.
	 */
	public boolean in() {
		return Direction.in(direction);
	}

	/**
	 * Returns true if the encoder is expecting type update.
	 */
	public boolean out() {
		return Direction.out(direction);
	}

	/**
	 * Accepts an encoding with known length, and an update to read back from the memory after
	 * processing.
	 */
	public Encoder accept(Segments.Consumer encoding, Segments.Consumer update, long length) {
		var offset = align();
		if (encoding != null && in()) encodings.add(m -> encoding.accept(m, offset, length));
		if (update != null && out()) updates.add(m -> update.accept(m, offset, length));
		inc(length);
		return this;
	}

	/**
	 * Adds padding/nul-termination of given size.
	 */
	public Encoder acceptNul(long length) {
		return accept((m, o, l) -> Segments.fill(m, o, l, 0), null, length);
	}

	/**
	 * Allocates memory and applies the encodings in sequence. The result also provides a method to
	 * update the source.
	 */
	public Native.Adapted<MemorySegment> alloc(SegmentAllocator allocator) {
		var memory = allocator.allocate(offset, alignment);
		for (var encoding : encodings)
			encoding.accept(memory);
		return Native.Adapted.of(memory, updates.isEmpty() ? null : this::resolve);
	}

	// support

	private long inc(long length) {
		offset += length;
		return offset;
	}

	private long align() {
		return inc(Layouts.padding(offset, alignment));
	}

	private void resolve(MemorySegment memory) {
		for (var update : updates)
			update.accept(memory);
	}
}
