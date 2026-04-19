package ceri.ffm.core;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.ref.Reference;
import ceri.common.function.Closeables;
import ceri.common.function.Functions;

/**
 * Combines arena lifecycle management and an allocated memory segment.
 */
public class Alloc implements Functions.Closeable {
	public static final Alloc NULL = new Alloc(null, null);
	@SuppressWarnings("resource")
	public static final Scope DEF = (s, a) -> of(Segments.arena(), Arena::close, s, a);
	@SuppressWarnings("resource")
	public static final Scope GLOBAL = (s, a) -> of(Arena.global(), _ -> {}, s, a);
	@SuppressWarnings("resource")
	public static final Scope SHARED = (s, a) -> of(Arena.ofShared(), Arena::close, s, a);
	@SuppressWarnings("resource")
	public static final Scope CONFINED = (s, a) -> of(Arena.ofConfined(), Arena::close, s, a);
	@SuppressWarnings("resource")
	public static final Scope AUTO =
		(s, a) -> of(Arena.ofAuto(), Reference::reachabilityFence, s, a);
	private final Functions.Closeable closer;
	public final MemorySegment memory;

	/**
	 * Allocates bytes with alignment based on arena scope.
	 */
	public interface Scope {
		/**
		 * Allocate layout with layout alignment.
		 */
		default Alloc of(MemoryLayout layout) {
			return of(layout, 1L);
		}

		/**
		 * Allocate sequential layouts with layout alignment.
		 */
		default Alloc of(MemoryLayout layout, long count) {
			if (layout == null) return null;
			return of(layout.byteSize() * count, layout.byteAlignment());
		}

		/**
		 * Allocate bytes with no alignment.
		 */
		default Alloc of(long size) {
			return of(size, 1L);
		}

		/**
		 * Allocate bytes with given alignment.
		 */
		Alloc of(long size, long align);
	}

	/**
	 * Wraps an existing segment without an arena.
	 */
	public static Alloc ref(MemorySegment memory) {
		return new Alloc(memory, null);
	}

	private static Alloc of(Arena arena, Functions.Consumer<Arena> closer, long size, long align) {
		return new Alloc(arena.allocate(size, align), () -> closer.accept(arena));
	}

	private Alloc(MemorySegment memory, Functions.Closeable closer) {
		this.closer = closer;
		this.memory = memory;
	}

	/**
	 * Returns the address of the segment.
	 */
	public long address() {
		return Segments.address(memory);
	}

	/**
	 * Returns true if the segment is zero size.
	 */
	public boolean isNull() {
		return Segments.isNull(memory);
	}

	/**
	 * Returns true if the segment is zero size.
	 */
	public boolean isEmpty() {
		return size() == 0L;
	}

	/**
	 * Returns the size of the segment.
	 */
	public long size() {
		return Segments.size(memory);
	}

	/**
	 * Returns the size of the segment from the offset; 0 if out of range.
	 */
	public long size(long offset) {
		return Segments.size(memory, offset);
	}

	/**
	 * Returns the size of the segment.
	 */
	public int sizeInt() {
		return Math.toIntExact(size());
	}

	/**
	 * Returns the size of the segment from the offset; 0 if out of range.
	 */
	public int sizeInt(long offset) {
		return Math.toIntExact(size(offset));
	}

	/**
	 * Returns the number of layout units available within bounds, or 0 if null.
	 */
	public long count(MemoryLayout layout) {
		return count(layout, 0L);
	}

	/**
	 * Returns the number of layout units available within bounds, or 0 if null.
	 */
	public long count(MemoryLayout layout, long offset) {
		return Segments.count(memory, layout, offset);
	}

	/**
	 * Returns true if non-null and the slice is within bounds.
	 */
	public boolean within(long offset, long length) {
		return Segments.within(memory, offset, length);
	}

	/**
	 * Returns true if non-null and the slice is within bounds.
	 */
	public boolean within(long offset, MemoryLayout layout, long count) {
		return Segments.within(memory, offset, layout, count);
	}

	/**
	 * Resizes the segment unless unchanged, null, or a null pointer.
	 */
	public Alloc resize(long offset) {
		return resize(offset, Long.MAX_VALUE);
	}

	/**
	 * Resizes the segment unless unchanged, null, or a null pointer.
	 */
	public Alloc resize(long offset, long length) {
		return instance(Segments.resize(this.memory, offset, length));
	}

	/**
	 * Resizes the segment unless unchanged, null, or a null pointer.
	 */
	public Alloc resize(long offset, MemoryLayout layout, long count) {
		return instance(Segments.resize(memory, offset, layout, count));
	}

	/**
	 * Returns a view of the memory segment.
	 */
	public Alloc slice(long offset) {
		return slice(offset, Long.MAX_VALUE);
	}

	/**
	 * Returns a view of the memory segment.
	 */
	public Alloc slice(long offset, long length) {
		return instance(Segments.slice(memory, offset, length));
	}

	/**
	 * Returns a view of the memory segment.
	 */
	public Alloc slice(long offset, MemoryLayout layout, long count) {
		return instance(Segments.slice(memory, offset, layout, count));
	}

	/**
	 * Returns an index view of a segment array that starts at the given offset.
	 */
	public Alloc indexSlice(MemoryLayout layout, long index) {
		return indexSlice(0L, layout, index);
	}

	/**
	 * Returns an index view of a segment array that starts at the given offset.
	 */
	public Alloc indexSlice(long offset, MemoryLayout layout, long index) {
		return slice(layout.scale(offset, index), layout, 1);
	}

	/**
	 * Fill the memory segment with zero bytes.
	 */
	public Alloc clear() {
		if (isAlive()) memory.fill((byte) 0);
		return this;
	}

	/**
	 * Returns true if the memory segment is valid.
	 */
	public boolean isAlive() {
		return memory.scope().isAlive();
	}

	@Override
	public void close() {
		Closeables.close(closer);
	}

	private Alloc instance(MemorySegment memory) {
		return this.memory == memory ? this : new Alloc(memory, closer);
	}
}