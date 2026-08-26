package ceri.ffm.core;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Objects;
import ceri.common.collect.Maps;
import ceri.common.except.Exceptions;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;
import ceri.common.util.Validate;
import ceri.ffm.type.Memory;

public class Layouts {
	public static final ValueLayout.OfBoolean BOOL = canonical(Native.Canonical.BOOL);
	public static final ValueLayout.OfByte BYTE = canonical(Native.Canonical.CHAR);
	public static final ValueLayout.OfShort SHORT = canonical(Native.Canonical.SHORT);
	public static final ValueLayout.OfInt INT = canonical(Native.Canonical.INT);
	public static final ValueLayout.OfLong LONG = canonical(Native.Canonical.LONG_LONG);
	public static final ValueLayout.OfFloat FLOAT = canonical(Native.Canonical.FLOAT);
	public static final ValueLayout.OfDouble DOUBLE = canonical(Native.Canonical.DOUBLE);
	public static final ValueLayout.OfChar CHAR =
		set(ValueLayout.JAVA_CHAR, null, SHORT.byteAlignment(), SHORT.order());
	public static final AddressLayout POINTER = canonical(Native.Canonical.VOID_P);
	public static final MemoryLayout EMPTY = MemoryLayout.sequenceLayout(0, BYTE);
	public static final long ALIGN_NATURAL = 0L;
	private static final int MASK_LE = 1;
	private static final int MASK_BE = 2;
	private static final int MASK_MAX = MASK_LE | MASK_BE;

	private Layouts() {}

	/**
	 * Interface that provides a fixed layout, and additional functionality based on the layout.
	 */
	public interface Provider<L extends MemoryLayout> {
		/**
		 * Provides the layout for this type.
		 */
		L layout();

		/**
		 * Returns the layout size in bytes.
		 */
		default int layoutSize() {
			return Math.toIntExact(layout().byteSize());
		}

		/**
		 * Returns byte size from the given element count.
		 */
		default long size(long count) {
			return layoutSize() * Math.max(0L, count);
		}

		/**
		 * Returns byte size from the given element count and optional nul-termination.
		 */
		default long size(long count, boolean nul) {
			return size(count) + termSize(nul);
		}

		/**
		 * Returns byte size from the given element count.
		 */
		default int sizeInt(long count) {
			return Math.toIntExact(size(count));
		}

		/**
		 * Returns byte size from the given element count and optional nul-termination.
		 */
		default int sizeInt(long count, boolean nul) {
			return sizeInt(count) + termSize(nul);
		}

		/**
		 * Returns element count from the given byte size.
		 */
		default long count(long size) {
			return layoutSize() == 0L ? 0L : Math.max(0L, size) / layoutSize();
		}

		/**
		 * Returns element count from the given byte size. Fails if larger than int.
		 */
		default int countInt(long size) {
			return Math.toIntExact(count(size));
		}

		/**
		 * Provides a view of the memory segment, with optional nul-terminated boundary. Returns
		 * null if nul-termination is specified but not found.
		 */
		default MemorySegment slice(MemorySegment memory, long offset, boolean nul) {
			return slice(memory, offset, Long.MAX_VALUE, nul);
		}

		/**
		 * Provides a view of the memory segment, with optional nul-terminated boundary. Returns
		 * null if nul-termination is specified but not found.
		 */
		default MemorySegment slice(MemorySegment memory, long offset, long length, boolean nul) {
			if (nul) return term().slice(memory, offset, length);
			return Memory.slice(memory, offset, length);
		}

		/**
		 * Returns a terminator matching layout size.
		 */
		default Terminator term() {
			return Terminator.of(layoutSize());
		}

		/**
		 * Returns terminator size or 0.
		 */
		default int termSize(boolean nul) {
			return nul ? layoutSize() : 0;
		}

		/**
		 * Allocates memory with alignment matching the layout.
		 */
		default MemorySegment allocate(SegmentAllocator allocator, long size) {
			if (allocator == null) return null;
			return allocator.allocate(size, layout().byteAlignment());
		}
	}

	/**
	 * Returns the byte size of the layout, or 0 if null.
	 */
	public static long size(MemoryLayout layout) {
		return layout == null ? 0L : layout.byteSize();
	}

	/**
	 * Returns the total byte size from layout and count, or 0 if null.
	 */
	public static long size(MemoryLayout layout, long count) {
		return size(layout) * Math.max(0L, count);
	}

	/**
	 * Returns the byte size of the layout, or 0 if null. Fails if larger than int.
	 */
	public static int sizeInt(MemoryLayout layout) {
		return Math.toIntExact(size(layout));
	}

	/**
	 * Returns the total byte size from layout and count, or 0 if null. Fails if larger than int.
	 */
	public static int sizeInt(MemoryLayout layout, long count) {
		return Math.toIntExact(size(layout, count));
	}

	/**
	 * Returns the number of layout units available within the size.
	 */
	public static long count(MemoryLayout layout, long size) {
		var lsize = size(layout);
		return lsize == 0L ? 0L : Math.max(0L, size) / lsize;
	}

	/**
	 * Returns the number of layout units available within the size.
	 */
	public static int countInt(MemoryLayout layout, long size) {
		return Math.toIntExact(count(layout, size));
	}

	/**
	 * Returns the byte order of the layout, or native if null.
	 */
	public static ByteOrder order(ValueLayout layout) {
		return layout == null ? ByteOrder.nativeOrder() : layout.order();
	}

	/**
	 * Determines the byte order for compound layouts. Returns null if both or no orders present.
	 */
	public static ByteOrder findOrder(MemoryLayout layout) {
		return switch (maskOrder(layout)) {
			case MASK_LE -> ByteOrder.LITTLE_ENDIAN;
			case MASK_BE -> ByteOrder.BIG_ENDIAN;
			default -> null;
		};
	}

	/**
	 * Returns a natural int-based layout based on type size in bytes. Fails if not a power of 2 or
	 * larger than long.
	 */
	public static ValueLayout ofInt(int size) {
		return switch (size) {
			case Byte.BYTES -> BYTE;
			case Short.BYTES -> SHORT;
			case Integer.BYTES -> INT;
			case Long.BYTES -> LONG;
			default -> throw Exceptions.illegalArg("Unsupported size: " + size);
		};
	}

	/**
	 * Returns a simple string descriptor for the layout.
	 */
	public static String string(MemoryLayout layout) {
		if (layout == null) return Strings.NULL;
		var b = new StringBuilder().append(layout.name().orElse(""));
		if (!b.isEmpty()) b.append(':');
		return b.append(orderSymbol(layout)).append(layout.byteSize()).append('/')
			.append(layout.byteAlignment()).toString();
	}

	/**
	 * Sets layout name, byte alignment and order. Ignores name if empty/null, align if <= 0, and
	 * order if null.
	 */
	public static <L extends MemoryLayout> L set(L layout, String name, long align,
		ByteOrder order) {
		layout = align(name(layout, name), align);
		if (order == null || !(layout instanceof ValueLayout vlayout)) return layout;
		return Reflect.unchecked(vlayout.withOrder(order));
	}

	/**
	 * Gets layout name, or empty string if not available.
	 */
	public static String name(MemoryLayout layout) {
		return layout == null ? "" : layout.name().orElse("");
	}

	/**
	 * Sets the layout name if not empty, or removes the current name if empty string. Does nothing
	 * if null string.
	 */
	public static <L extends MemoryLayout> L name(L layout, String name) {
		if (layout == null || name == null) return layout;
		if (Strings.isEmpty(name))
			return layout.name().isEmpty() ? layout : Reflect.unchecked(layout.withoutName());
		if (name.equals(name(layout))) return layout;
		return Reflect.unchecked(layout.withName(name));
	}

	/**
	 * Gets layout alignment, or 0 if null.
	 */
	public static long align(MemoryLayout layout) {
		return layout == null ? 0L : layout.byteAlignment();
	}

	/**
	 * Sets layout alignment if value is > 0 and a power of 2. Does nothing if setting the layout
	 * alignment would fail.
	 */
	public static <L extends MemoryLayout> L align(L layout, long align) {
		if (layout == null || align <= 0 || align == layout.byteAlignment()
			|| Long.bitCount(align) != 1) return layout;
		if (layout instanceof SequenceLayout s && align < s.elementLayout().byteAlignment())
			return layout;
		if (layout instanceof GroupLayout g) for (var member : g.memberLayouts())
			if (align < member.byteAlignment()) return layout;
		return Reflect.unchecked(layout.withByteAlignment(align));
	}

	/**
	 * Returns an alignment value limited to the layout size. This value allows the layout to be
	 * used as an element in a sequence.
	 */
	public static <L extends MemoryLayout> long elementAlign(L layout, long align) {
		return Math.min(align, size(layout));
	}

	/**
	 * Sets layout byte order if non-null and the layout is a value layout.
	 */
	public static <L extends ValueLayout> L order(L layout, ByteOrder order) {
		if (layout == null || order == null || Objects.equals(layout.order(), order)) return layout;
		return Reflect.unchecked(layout.withOrder(order));
	}

	/**
	 * Calculates the padding required from the offset for the given byte alignment.
	 */
	public static long padding(long offset, long align) {
		if (align <= 1L) return 0L;
		long diff = offset % align;
		return diff == 0L ? 0L : align - diff;
	}

	/**
	 * Calculates the padding required from the offset for the layout byte alignment.
	 */
	public static long padding(long offset, MemoryLayout layout) {
		return layout == null ? 0L : padding(offset, layout.byteAlignment());
	}

	public static long validateAlignment(long offset, long align) {
		return Validate.equal(padding(offset, align), 0L, "Alignment padding");
	}

	/**
	 * Creates a struct layout from member layouts.
	 */
	public static StructLayout struct(Collection<? extends MemoryLayout> members) {
		if (members == null) return null;
		return MemoryLayout.structLayout(members.toArray(MemoryLayout[]::new));
	}

	/**
	 * Creates a union layout from member layouts.
	 */
	public static UnionLayout union(Collection<? extends MemoryLayout> members) {
		if (members == null) return null;
		return MemoryLayout.unionLayout(members.toArray(MemoryLayout[]::new));
	}

	// support

	private static <L extends ValueLayout> L canonical(Native.Canonical canonical) {
		return Reflect.unchecked(Maps.getOrThrow(Native.LINKER.canonicalLayouts(), canonical.name));
	}

	private static String orderSymbol(MemoryLayout layout) {
		return switch (maskOrder(layout)) {
			case MASK_LE -> "<";
			case MASK_BE -> ">";
			default -> "";
		};
	}

	private static int maskOrder(MemoryLayout layout) {
		return switch (layout) {
			case ValueLayout v -> mask(order(v));
			case SequenceLayout s -> maskOrder(s.elementLayout());
			case GroupLayout g -> maskGroupOrder(g);
			case null, default -> 0;
		};
	}

	private static int maskGroupOrder(GroupLayout layout) {
		int mask = 0;
		for (var member : layout.memberLayouts()) {
			mask |= maskOrder(member);
			if (mask >= MASK_MAX) break;
		}
		return mask;
	}

	private static int mask(ByteOrder order) {
		return switch (order) {
			case LITTLE_ENDIAN -> MASK_LE;
			case BIG_ENDIAN -> MASK_BE;
			case null -> 0;
		};
	}
}
