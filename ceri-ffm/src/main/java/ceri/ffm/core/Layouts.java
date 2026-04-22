package ceri.ffm.core;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import ceri.common.array.Dimensions;
import ceri.common.array.RawArray;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.reflect.Handles;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;
import ceri.ffm.type.Terminator;

public class Layouts {
	public static final ValueLayout.OfBoolean BOOL = canonical(Native.Canonical.BOOL);
	public static final ValueLayout.OfByte BYTE = canonical(Native.Canonical.CHAR);
	public static final ValueLayout.OfShort SHORT = canonical(Native.Canonical.SHORT);
	public static final ValueLayout.OfInt INT = canonical(Native.Canonical.INT);
	public static final ValueLayout.OfLong LONG = canonical(Native.Canonical.LONG_LONG);
	public static final ValueLayout.OfFloat FLOAT = canonical(Native.Canonical.FLOAT);
	public static final ValueLayout.OfDouble DOUBLE = canonical(Native.Canonical.DOUBLE);
	public static final ValueLayout.OfChar CHAR = copy(ValueLayout.JAVA_CHAR, SHORT);
	public static final AddressLayout POINTER = canonical(Native.Canonical.VOID_P);

	private Layouts() {}

	/**
	 * Provides a fixed layout, and functionality based on the layout.
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
			return Math.max(0L, size) / layoutSize();
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
			return Segments.slice(memory, offset, length);
		}

		/**
		 * Provides a view of the memory segment, with optional nul-terminated boundary. Returns
		 * null if nul-termination is specified but not found.
		 */
		default MemorySegment sliceByCount(MemorySegment memory, long count, boolean nul) {
			return sliceByCount(memory, 0L, count, nul);
		}

		/**
		 * Provides a view of the memory segment, with optional nul-terminated boundary. Returns
		 * null if nul-termination is specified but not found.
		 */
		default MemorySegment sliceByCount(MemorySegment memory, long offset, long count,
			boolean nul) {
			return slice(memory, offset, size(count), nul);
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
	}

	/**
	 * Returns the byte size of the layout, 0 if null.
	 */
	public static long size(MemoryLayout layout) {
		return layout == null ? 0L : layout.byteSize();
	}

	/**
	 * Returns the total byte size from layout and count.
	 */
	public static long size(MemoryLayout layout, long count) {
		return size(layout) * Math.max(0L, count);
	}

	/**
	 * Returns the byte size of the layout, 0 if null. Fails if larger than int.
	 */
	public static int sizeInt(MemoryLayout layout) {
		return Math.toIntExact(size(layout));
	}

	/**
	 * Returns the total byte size from layout and count. Fails if larger than int.
	 */
	public static int sizeInt(MemoryLayout layout, long count) {
		return Math.toIntExact(size(layout, count));
	}

	/**
	 * Returns the number of layout units available within the size.
	 */
	public static long count(MemoryLayout layout, long size) {
		var lsize = size(layout);
		if (lsize == 0L) return 0L;
		return Math.max(0L, size) / lsize;
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

	public static ValueLayout ofInt(int size) {
		return switch (size) {
			case Byte.BYTES -> BYTE;
			case Short.BYTES -> SHORT;
			case Integer.BYTES -> INT;
			case Long.BYTES -> LONG;
			default -> throw Exceptions.illegalArg("Unsupported size: " + size);
		};
	}

	public static ValueLayout ofInt(int size, long alignment) {
		return ofInt(size).withByteAlignment(alignment);
	}

	public static ValueLayout ofInt(int size, long alignment, ByteOrder order) {
		return ofInt(size, alignment).withOrder(order);
	}

	public static <L extends MemoryLayout> L set(L layout, String name, long align) {
		return align(name(layout, name), align);
	}

	/**
	 * Sets layout name, byte alignment and order. Only sets the value if name is non-null, align is
	 * > 0 or order is non-null.
	 */
	public static <L extends MemoryLayout> L set(L layout, String name, long align,
		ByteOrder order) {
		return order(align(name(layout, name), align), order);
	}

	/**
	 * Applies settings to the layout, and returns an existing type or constructs a new type
	 * depending on whether the the layout is changed.
	 */
	public static <L extends MemoryLayout, R> R with(R current,
		Functions.Function<L, R> constructor, L layout, String name, long align, ByteOrder order) {
		var modified = Layouts.set(layout, name, align, order);
		return layout.equals(modified) ? current : constructor.apply(modified);
	}

	/**
	 * Returns a simple string descriptor for the layout.
	 */
	public static String desc(MemoryLayout layout) {
		if (layout == null) return Strings.NULL;
		var name = layout.name().orElse(null);
		if (name == null)
			return String.format("0x%x/%d", layout.byteSize(), layout.byteAlignment());
		return String.format("%s/0x%x/%d", name, layout.byteSize(), layout.byteAlignment());
	}

	/**
	 * Copies byte alignment and order from one layout to another.
	 */
	public static <L extends ValueLayout> L copy(L layout, ValueLayout from) {
		if (from == null) return layout;
		return set(layout, null, from.byteAlignment(), from.order());
	}

	/**
	 * Sets layout name if value is non-null.
	 */
	public static <L extends MemoryLayout> L name(L layout, String name) {
		if (layout == null || name == null) return layout;
		return Reflect
			.unchecked(Strings.isEmpty(name) ? layout.withoutName() : layout.withName(name));
	}

	/**
	 * Sets layout alignment if value is > 0.
	 */
	public static <L extends MemoryLayout> L align(L layout, long align) {
		if (layout == null || align <= 0) return layout;
		return Reflect.unchecked(layout.withByteAlignment(align));
	}

	/**
	 * Calculates the padding required from the offset for the given byte alignment.
	 */
	public static long padding(long offset, long align) {
		if (offset == 0 || offset % align == 0) return 0;
		return align - (offset % align);
	}

	/**
	 * Calculates the padding required from the offset for the layout byte alignment.
	 */
	public static long padding(long offset, MemoryLayout layout) {
		if (layout == null) return 0L;
		return padding(offset, layout.byteAlignment());
	}

	/**
	 * Sets layout byte order if value is non-null and layout is a value layout.
	 */
	public static <L extends MemoryLayout> L order(L layout, ByteOrder order) {
		if (order == null || !(layout instanceof ValueLayout vlayout)) return layout;
		return Reflect.unchecked(vlayout.withOrder(order));
	}

	/**
	 * Returns the selected address layout target, or null if not an address.
	 */
	public static MemoryLayout target(StructLayout layout, PathElement... elements) {
		if (layout == null || elements == null) return null;
		return (layout.select(elements) instanceof AddressLayout address) ?
			address.targetLayout().orElse(null) : null;
	}

	/**
	 * Creates a struct layout from member layouts.
	 */
	public static StructLayout struct(Collection<? extends MemoryLayout> members) {
		if (members == null) return null;
		return MemoryLayout.structLayout(members.toArray(MemoryLayout[]::new));
	}

	/**
	 * Creates a struct layout from member layouts, adding padding. Does not add trailing padding if
	 * the last member is a flexible array.
	 */
	public static StructLayout paddedStruct(MemoryLayout... members) {
		if (members == null) return null;
		return paddedStruct(Arrays.asList(members));
	}

	/**
	 * Creates a struct layout from member layouts, adding padding. Does not add trailing padding if
	 * the last member is a flexible array.
	 */
	public static StructLayout paddedStruct(Iterable<? extends MemoryLayout> members) {
		if (members == null) return null;
		var layouts = Lists.<MemoryLayout>of();
		long offset = 0L, align = 0L;
		for (var layout : members) {
			offset += addPadding(layouts, offset, layout.byteAlignment());
			layouts.add(layout);
			align = Math.max(align, layout.byteAlignment());
			offset += layout.byteSize();
		}
		if (!isFlexArray(Lists.last(layouts))) addPadding(layouts, offset, align);
		return struct(layouts);
	}

	/**
	 * Creates a union layout from member layouts.
	 */
	public static UnionLayout union(Collection<? extends MemoryLayout> members) {
		if (members == null) return null;
		return MemoryLayout.unionLayout(members.toArray(MemoryLayout[]::new));
	}

	/**
	 * Creates nested sequence layouts to match the dimensions.
	 */
	public static MemoryLayout array(MemoryLayout layout, Dimensions dims) {
		if (layout == null) return null;
		if (dims == null) dims = Dimensions.NONE;
		for (int i = dims.count() - 1; i >= 0; i--)
			layout = MemoryLayout.sequenceLayout(dims.dim(i), layout);
		return layout;
	}

	/**
	 * Creates nested sequence layouts to match the dimensions.
	 */
	public static MemoryLayout array(MemoryLayout layout, int... dims) {
		if (layout == null || RawArray.isEmpty(dims)) return layout;
		return array(layout, Dimensions.of(dims));
	}

	/**
	 * Creates an 0-length flexible array member layout.
	 */
	public static SequenceLayout flexArray(MemoryLayout element) {
		if (element == null) return null;
		return MemoryLayout.sequenceLayout(0, element);
	}

	/**
	 * Returns true if the member layout is an array of zero length.
	 */
	public static boolean isFlexArray(MemoryLayout member) {
		return member != null && (member instanceof SequenceLayout seq) && seq.elementCount() == 0L;
	}

	/**
	 * Returns true if the struct has a flexible array member.
	 */
	public static boolean hasFlexArray(StructLayout layout) {
		return flexArrayIndex(layout) > 0;
	}

	/**
	 * Returns the index of a flexible array member within the struct layout, or -1.
	 */
	public static int flexArrayIndex(StructLayout layout) {
		if (layout == null) return -1;
		var members = layout.memberLayouts();
		for (int index = members.size() - 1; index > 0; index--) {
			var last = Lists.at(members, index);
			if (!(last instanceof PaddingLayout))
				return (last instanceof SequenceLayout) ? index : -1;
		}
		return -1;
	}

	/**
	 * Allocates the struct layout containing a flexible array member of given length.
	 */
	public static MemorySegment flexStructAlloc(SegmentAllocator allocator, StructLayout layout,
		long length) {
		if (allocator == null || layout == null) return null;
		long size = flexStructSize(layout, length);
		return allocator.allocate(size, layout.byteAlignment());
	}

	/**
	 * Returns the struct layout size containing a flexible array member of given count.
	 */
	public static long flexStructSize(StructLayout layout, long length) {
		int index = flexArrayIndex(layout);
		if (index < 0) return size(layout);
		var flexArray = (SequenceLayout) layout.memberLayouts().get(index);
		var offset = layout.byteOffset(PathElement.groupElement(index));
		var size = flexArray.elementLayout().scale(offset, length);
		return Math.max(layout.byteSize(), size);
	}

	/**
	 * Returns the flexible array count from its containing struct layout and size.
	 */
	public static long flexArrayCount(StructLayout layout, long size) {
		int index = flexArrayIndex(layout);
		if (index < 0) return 0;
		var flexArray = (SequenceLayout) layout.memberLayouts().get(index);
		var offset = layout.byteOffset(PathElement.groupElement(index));
		return count(flexArray.elementLayout(), size - offset);
	}

	/**
	 * Returns a var handle {@code (MemorySegment memory, long offset, int index)} to access an
	 * element of a flexible array member. Adds the member offset to the given offset.
	 */
	public static VarHandle flexArrayVarHandle(StructLayout layout,
		MemoryLayout.PathElement... paths) {
		int index = flexArrayIndex(layout);
		if (index < 0) return null;
		var flexArray = (SequenceLayout) layout.memberLayouts().get(index);
		long offset = layout.byteOffset(PathElement.groupElement(index));
		var handle = flexArray.elementLayout().arrayElementVarHandle(paths);
		return MethodHandles.filterCoordinates(handle, 1, Handles.Math.addExact(offset));
	}

	// support

	private static <L extends ValueLayout> L canonical(Native.Canonical canonical) {
		return Reflect.unchecked(Maps.getOrThrow(Native.LINKER.canonicalLayouts(), canonical.name));
	}

	private static long addPadding(List<MemoryLayout> layouts, long offset, long align) {
		var padding = padding(offset, align);
		if (padding != 0) layouts.add(MemoryLayout.paddingLayout(padding));
		return padding;
	}
}
