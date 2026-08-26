package ceri.ffm.core;

import static ceri.ffm.test.FfmTesting.A;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import ceri.common.array.Dimensions;
import ceri.common.collect.Lists;
import ceri.common.reflect.Handles;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.test.FfmTesting.Gen;
import ceri.ffm.type.Memory;

/**
 * Demonstrates access to values using layout var handles.
 */
public class VarHandleTester {

	/**
	 * Holds a var handle and array var handle.
	 */
	public record VarHandles(VarHandle v, VarHandle a) {
		public static VarHandles from(MemoryLayout layout, PathElement... elements) {
			return new VarHandles(layout.varHandle(elements),
				layout.arrayElementVarHandle(elements));
		}
	}

	/**
	 * Simple struct.
	 */
	public static class Simple {
		private Simple() {}

		// @Order(big) int i;
		// short s;
		// byte[] padding = new byte[2];

		public static final StructLayout LAYOUT = paddedStruct( //
			Layouts.INT.withOrder(ByteOrder.BIG_ENDIAN).withName("i"), // 4
			Layouts.SHORT.withName("s")); // 2 + 2 padding

		public static final VarHandles I = VarHandles.from(LAYOUT, //
			PathElement.groupElement("i"));
		public static final VarHandles S = VarHandles.from(LAYOUT, //
			PathElement.groupElement("s"));

		public static void set(MemorySegment m, long offset, int i) {
			I.v.set(m, offset, Gen.i('A', i));
			S.v.set(m, offset, Gen.s('a', i));
		}

		public static void aset(MemorySegment m, long offset, int index, int i) {
			I.a.set(m, offset, index, Gen.i('A', i));
			S.a.set(m, offset, index, Gen.s('a', i));
		}
	}

	/**
	 * Struct with pointers.
	 */
	public static class Refs {
		private Refs() {}

		// Pointer<Simple> ps;
		// Pointer<byte[2][3]> pbbb;

		public static final Dimensions BBB_DIMS = Dimensions.of(2, 3);
		public static final MemoryLayout S_LAYOUT = Simple.LAYOUT.withName("s");
		public static final MemoryLayout BBB_LAYOUT = array(Layouts.BYTE, BBB_DIMS).withName("bbb");

		public static final StructLayout LAYOUT = paddedStruct( //
			Layouts.POINTER.withTargetLayout(S_LAYOUT).withName("ps"), // 8
			Layouts.POINTER.withTargetLayout(BBB_LAYOUT).withName("pbbb")); // 8

		public static final VarHandles PS = VarHandles.from(LAYOUT, //
			PathElement.groupElement("ps"));
		public static final VarHandles PBBB = VarHandles.from(LAYOUT, //
			PathElement.groupElement("pbbb"));
		public static final VarHandles S_I = VarHandles.from(LAYOUT, //
			PathElement.groupElement("ps"), //
			PathElement.dereferenceElement(), //
			PathElement.groupElement("i"));
		public static final VarHandles S_S = VarHandles.from(LAYOUT, //
			PathElement.groupElement("ps"), //
			PathElement.dereferenceElement(), //
			PathElement.groupElement("s"));
		public static final VarHandles BBB = VarHandles.from(LAYOUT, //
			PathElement.groupElement("pbbb"), //
			PathElement.dereferenceElement(), //
			PathElement.sequenceElement(), //
			PathElement.sequenceElement());

		public static MemorySegment[] init(int n) {
			var mems = FfmTesting.Alloc.of().add(Refs.LAYOUT, n).add(Refs.S_LAYOUT, n)
				.add(Refs.BBB_LAYOUT, n).alloc();
			for (int i = 0; i < n; i++) {
				Refs.PS.a.set(mems[1], 0L, i, Memory.slice(mems[2], i, Refs.S_LAYOUT, 1));
				Refs.PBBB.a.set(mems[1], 0L, i, Memory.slice(mems[3], i, Refs.BBB_LAYOUT, 1));
			}
			return mems;
		}

		public static void set(MemorySegment m, long offset, int i) {
			S_I.v.set(m, offset, Gen.i('A', i));
			S_S.v.set(m, offset, Gen.s('a', i));
			BBB_DIMS.forEach((a, j) -> BBB.v.set(m, offset, a[0], a[1], Gen.b('a', i + j)));
		}

		public static void aset(MemorySegment m, long offset, int index, int i) {
			S_I.a.set(m, offset, index, Gen.i('A', i));
			S_S.a.set(m, offset, index, Gen.s('a', i));
			BBB_DIMS.forEach((a, j) -> BBB.a.set(m, offset, index, a[0], a[1], Gen.b('a', i + j)));
		}
	}

	/**
	 * Nested struct without flexible array member.
	 */
	public static class Nested {
		private Nested() {}

		// @Order(big) int i;
		// byte[2][3] bbb;
		// Simple[3] ss

		public static final Dimensions BBB_DIMS = Dimensions.of(2, 3);
		public static final Dimensions SS_DIMS = Dimensions.of(3);

		public static final StructLayout LAYOUT = paddedStruct( //
			Layouts.INT.withOrder(ByteOrder.BIG_ENDIAN).withName("i"), // 4
			array(Layouts.BYTE, BBB_DIMS).withName("bbb"), // 6 + 6 padding
			array(Simple.LAYOUT, SS_DIMS).withName("ss")); // 16

		public static final VarHandles I = VarHandles.from(LAYOUT, //
			PathElement.groupElement("i"));
		public static final VarHandles BBB = VarHandles.from(LAYOUT, //
			PathElement.groupElement("bbb"), //
			PathElement.sequenceElement(), //
			PathElement.sequenceElement());
		public static final VarHandles SS_I = VarHandles.from(LAYOUT, //
			PathElement.groupElement("ss"), //
			PathElement.sequenceElement(), //
			PathElement.groupElement("i"));
		public static final VarHandles SS_S = VarHandles.from(LAYOUT, //
			PathElement.groupElement("ss"), //
			PathElement.sequenceElement(), //
			PathElement.groupElement("s"));

		public static void set(MemorySegment m, long offset, int i) {
			I.v.set(m, offset, Gen.i('A', i));
			BBB_DIMS.forEach((a, j) -> BBB.v.set(m, offset, a[0], a[1], Gen.b('a', i + j)));
			SS_DIMS.forEach((a, j) -> {
				SS_I.v.set(m, offset, a[0], Gen.i('Q', i + j));
				SS_S.v.set(m, offset, a[0], Gen.s('q', i + j));
			});
		}

		public static void aset(MemorySegment m, long offset, int index, int i) {
			I.a.set(m, offset, index, Gen.i('A', i));
			BBB_DIMS.forEach((a, j) -> BBB.a.set(m, offset, index, a[0], a[1], Gen.b('a', i + j)));
			SS_DIMS.forEach((a, j) -> {
				SS_I.a.set(m, offset, index, a[0], Gen.i('Q', i + j));
				SS_S.a.set(m, offset, index, a[0], Gen.s('q', i + j));
			});
		}
	}

	/**
	 * Nested struct without flexible value array member.
	 */
	public static class FlexValue {
		private FlexValue() {}

		// @Order(big) int i;
		// byte[2][3] bbb;
		// char[0] fa;
		
		public static final Dimensions BBB_DIMS = Dimensions.of(2, 3);

		public static final StructLayout LAYOUT = paddedStruct( //
			Layouts.INT.withOrder(ByteOrder.BIG_ENDIAN).withName("i"), // 4
			array(Layouts.BYTE, BBB_DIMS).withName("bbb"), // 6
			flexArray(Layouts.CHAR).withName("fa"));

		public static final VarHandles I = VarHandles.from(LAYOUT, //
			PathElement.groupElement("i"));
		public static final VarHandles BBB = VarHandles.from(LAYOUT, //
			PathElement.groupElement("bbb"), //
			PathElement.sequenceElement(), //
			PathElement.sequenceElement());
		public static final VarHandle FA = flexArrayVarHandle(LAYOUT);

		public static void set(MemorySegment m, long offset, int i) {
			I.v.set(m, offset, Gen.i('A', i));
			BBB_DIMS.forEach((a, j) -> BBB.v.set(m, offset, a[0], a[1], Gen.b('a', i + j)));
			int count = (int) flexArrayCount(LAYOUT, m.byteSize());
			for (int j = 0; j < count; j++)
				FA.set(m, offset, j, Gen.c('Q', i + j));
		}
	}

	/**
	 * Nested struct without flexible struct array member.
	 */
	public static class FlexStruct {
		private FlexStruct() {}

		// @Order(big) int i;
		// byte[2][3] bbb;
		// Simple[0] fa;

		public static final Dimensions BBB_DIMS = Dimensions.of(2, 3);

		public static final StructLayout LAYOUT = paddedStruct( //
			Layouts.INT.withOrder(ByteOrder.BIG_ENDIAN).withName("i"), // 4
			array(Layouts.BYTE, BBB_DIMS).withName("bbb"), // 6
			flexArray(Simple.LAYOUT).withName("fa"));
		public static final VarHandles I = VarHandles.from(LAYOUT, //
			PathElement.groupElement("i"));
		public static final VarHandles BBB = VarHandles.from(LAYOUT, //
			PathElement.groupElement("bbb"), //
			PathElement.sequenceElement(), //
			PathElement.sequenceElement());
		public static final VarHandle FA_I = flexArrayVarHandle(LAYOUT, //
			PathElement.groupElement("i"));
		public static final VarHandle FA_S = flexArrayVarHandle(LAYOUT, //
			PathElement.groupElement("s"));

		public static void set(MemorySegment m, long offset, int i) {
			I.v.set(m, offset, Gen.i('A', i));
			BBB_DIMS.forEach((a, j) -> BBB.v.set(m, offset, a[0], a[1], Gen.b('a', i + j)));
			int count = (int) flexArrayCount(LAYOUT, m.byteSize());
			for (int j = 0; j < count; j++) {
				FA_I.set(m, offset, j, Gen.i('Q', i + j));
				FA_S.set(m, offset, j, Gen.s('q', i + j));
			}
		}
	}

	public static void main(String[] args) {
		simple(3);
		refs(3);
		nested(3);
		flexValue(5);
		flexStruct(3);
	}

	public static void simple(int n) {
		FfmTesting.title();
		var m = FfmTesting.A.allocate(Simple.LAYOUT);
		Simple.set(m, 0L, 0);
		FfmTesting.bin(m);
		m = FfmTesting.A.allocate(Simple.LAYOUT, n);
		for (int i = 0; i < n; i++)
			Simple.set(m, Simple.LAYOUT.scale(0L, i), i + 1);
		FfmTesting.bin(m);
		for (int i = 0; i < n; i++)
			Simple.aset(m, 0L, i, i + 2);
		FfmTesting.bin(m);
	}

	public static void refs(int n) {
		FfmTesting.title();
		var m = Refs.init(1);
		Refs.set(m[1], 0, 0);
		FfmTesting.bin(m);
		m = Refs.init(n);
		for (int i = 0; i < n; i++)
			Refs.set(m[1], Refs.LAYOUT.scale(0L, i), i + 1);
		FfmTesting.bin(m);
		for (int i = 0; i < n; i++)
			Refs.aset(m[1], 0L, i, i + 1);
		FfmTesting.bin(m);
	}

	public static void nested(int n) {
		FfmTesting.title();
		var m = A.allocate(Nested.LAYOUT);
		Nested.set(m, 0L, 0);
		FfmTesting.bin(m);
		m = FfmTesting.A.allocate(Nested.LAYOUT, n);
		for (int i = 0; i < n; i++)
			Nested.set(m, Nested.LAYOUT.scale(0L, i), i + 1);
		FfmTesting.bin(m);
		for (int i = 0; i < n; i++)
			Nested.aset(m, 0L, i, i + 1);
		FfmTesting.bin(m);
	}

	public static void flexValue(int n) {
		FfmTesting.title();
		var m = flexStructAlloc(FfmTesting.A, FlexValue.LAYOUT, n);
		FlexValue.set(m, 0L, 0);
		FfmTesting.bin(m);
	}

	public static void flexStruct(int n) {
		FfmTesting.title();
		var m = flexStructAlloc(FfmTesting.A, FlexStruct.LAYOUT, n);
		FlexStruct.set(m, 0L, 0);
		FfmTesting.bin(m);
	}

	// support

	/**
	 * Creates a struct layout from member layouts, adding padding. Does not add trailing padding if
	 * the last member is a flexible array.
	 */
	private static StructLayout paddedStruct(MemoryLayout... members) {
		if (members == null) return null;
		return paddedStruct(Arrays.asList(members));
	}

	/**
	 * Creates a struct layout from member layouts, adding padding. Does not add trailing padding if
	 * the last member is a flexible array.
	 */
	private static StructLayout paddedStruct(Iterable<? extends MemoryLayout> members) {
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
		return Layouts.struct(layouts);
	}

	private static long addPadding(List<MemoryLayout> layouts, long offset, long align) {
		var padding = Layouts.padding(offset, align);
		if (padding != 0) layouts.add(MemoryLayout.paddingLayout(padding));
		return padding;
	}

	/**
	 * Creates nested sequence layouts to match the dimensions.
	 */
	private static MemoryLayout array(MemoryLayout layout, Dimensions dims) {
		if (layout == null) return null;
		if (dims == null) dims = Dimensions.NONE;
		for (int i = dims.count() - 1; i >= 0; i--)
			layout = MemoryLayout.sequenceLayout(dims.dim(i), layout);
		return layout;
	}

	/**
	 * Creates an 0-length flexible array member layout.
	 */
	private static SequenceLayout flexArray(MemoryLayout element) {
		if (element == null) return null;
		return MemoryLayout.sequenceLayout(0, element);
	}

	/**
	 * Returns true if the member layout is an array of zero length.
	 */
	private static boolean isFlexArray(MemoryLayout member) {
		return member != null && (member instanceof SequenceLayout seq) && seq.elementCount() == 0L;
	}

	/**
	 * Returns the index of a flexible array member within the struct layout, or -1.
	 */
	private static int flexArrayIndex(StructLayout layout) {
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
	private static MemorySegment flexStructAlloc(SegmentAllocator allocator, StructLayout layout,
		long length) {
		if (allocator == null || layout == null) return null;
		long size = flexStructSize(layout, length);
		return allocator.allocate(size, layout.byteAlignment());
	}

	/**
	 * Returns the struct layout size containing a flexible array member of given count.
	 */
	private static long flexStructSize(StructLayout layout, long length) {
		int index = flexArrayIndex(layout);
		if (index < 0) return Layouts.size(layout);
		var flexArray = (SequenceLayout) layout.memberLayouts().get(index);
		var offset = layout.byteOffset(PathElement.groupElement(index));
		var size = flexArray.elementLayout().scale(offset, length);
		return Math.max(layout.byteSize(), size);
	}

	/**
	 * Returns the flexible array count from its containing struct layout and size.
	 */
	private static long flexArrayCount(StructLayout layout, long size) {
		int index = flexArrayIndex(layout);
		if (index < 0) return 0;
		var flexArray = (SequenceLayout) layout.memberLayouts().get(index);
		var offset = layout.byteOffset(PathElement.groupElement(index));
		return Layouts.count(flexArray.elementLayout(), size - offset);
	}

	/**
	 * Returns a var handle {@code (MemorySegment memory, long offset, int index)} to access an
	 * element of a flexible array member. Adds the member offset to the given offset.
	 */
	private static VarHandle flexArrayVarHandle(StructLayout layout,
		MemoryLayout.PathElement... paths) {
		int index = flexArrayIndex(layout);
		if (index < 0) return null;
		var flexArray = (SequenceLayout) layout.memberLayouts().get(index);
		long offset = layout.byteOffset(PathElement.groupElement(index));
		var handle = flexArray.elementLayout().arrayElementVarHandle(paths);
		return MethodHandles.filterCoordinates(handle, 1, Handles.Math.addExact(offset));
	}
}
