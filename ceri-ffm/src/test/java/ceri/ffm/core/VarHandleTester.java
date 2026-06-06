package ceri.ffm.core;

import static ceri.ffm.test.FfmTesting.A;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import ceri.common.array.Dimensions;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.test.FfmTesting.Gen;

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

		public static final StructLayout LAYOUT = Layouts.paddedStruct( //
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

		public static final Dimensions BBB_DIMS = Dimensions.of(2, 3);
		public static final MemoryLayout S_LAYOUT = Simple.LAYOUT.withName("s");
		public static final MemoryLayout BBB_LAYOUT =
			Layouts.array(Layouts.BYTE, BBB_DIMS).withName("bbb");

		public static final StructLayout LAYOUT = Layouts.paddedStruct( //
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
				Refs.PS.a.set(mems[1], 0L, i, Segments.sliceAt(mems[2], i, Refs.S_LAYOUT));
				Refs.PBBB.a.set(mems[1], 0L, i, Segments.sliceAt(mems[3], i, Refs.BBB_LAYOUT));
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

		public static final Dimensions BBB_DIMS = Dimensions.of(2, 3);
		public static final Dimensions SS_DIMS = Dimensions.of(3);

		public static final StructLayout LAYOUT = Layouts.paddedStruct( //
			Layouts.INT.withOrder(ByteOrder.BIG_ENDIAN).withName("i"), // 4
			Layouts.array(Layouts.BYTE, BBB_DIMS).withName("bbb"), // 6 + 6 padding
			Layouts.array(Simple.LAYOUT, SS_DIMS).withName("ss")); // 16

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

		public static final Dimensions BBB_DIMS = Dimensions.of(2, 3);

		public static final StructLayout LAYOUT = Layouts.paddedStruct( //
			Layouts.INT.withOrder(ByteOrder.BIG_ENDIAN).withName("i"), // 4
			Layouts.array(Layouts.BYTE, BBB_DIMS).withName("bbb"), // 6
			Layouts.flexArray(Layouts.CHAR).withName("fa"));

		public static final VarHandles I = VarHandles.from(LAYOUT, //
			PathElement.groupElement("i"));
		public static final VarHandles BBB = VarHandles.from(LAYOUT, //
			PathElement.groupElement("bbb"), //
			PathElement.sequenceElement(), //
			PathElement.sequenceElement());
		public static final VarHandle FA = Layouts.flexArrayVarHandle(LAYOUT);

		public static void set(MemorySegment m, long offset, int i) {
			I.v.set(m, offset, Gen.i('A', i));
			BBB_DIMS.forEach((a, j) -> BBB.v.set(m, offset, a[0], a[1], Gen.b('a', i + j)));
			int count = (int) Layouts.flexArrayCount(LAYOUT, m.byteSize());
			for (int j = 0; j < count; j++)
				FA.set(m, offset, j, Gen.c('Q', i + j));
		}
	}

	/**
	 * Nested struct without flexible struct array member.
	 */
	public static class FlexStruct {
		private FlexStruct() {}

		public static final Dimensions BBB_DIMS = Dimensions.of(2, 3);

		public static final StructLayout LAYOUT = Layouts.paddedStruct( //
			Layouts.INT.withOrder(ByteOrder.BIG_ENDIAN).withName("i"), // 4
			Layouts.array(Layouts.BYTE, BBB_DIMS).withName("bbb"), // 6
			Layouts.flexArray(Simple.LAYOUT).withName("fa"));
		public static final VarHandles I = VarHandles.from(LAYOUT, //
			PathElement.groupElement("i"));
		public static final VarHandles BBB = VarHandles.from(LAYOUT, //
			PathElement.groupElement("bbb"), //
			PathElement.sequenceElement(), //
			PathElement.sequenceElement());
		public static final VarHandle FA_I = Layouts.flexArrayVarHandle(LAYOUT, //
			PathElement.groupElement("i"));
		public static final VarHandle FA_S = Layouts.flexArrayVarHandle(LAYOUT, //
			PathElement.groupElement("s"));

		public static void set(MemorySegment m, long offset, int i) {
			I.v.set(m, offset, Gen.i('A', i));
			BBB_DIMS.forEach((a, j) -> BBB.v.set(m, offset, a[0], a[1], Gen.b('a', i + j)));
			int count = (int) Layouts.flexArrayCount(LAYOUT, m.byteSize());
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
			Simple.aset(m, 0L, i, i + 1);
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
		var m = Layouts.flexStructAlloc(FfmTesting.A, FlexValue.LAYOUT, n);
		FlexValue.set(m, 0L, 0);
		FfmTesting.bin(m);
	}

	public static void flexStruct(int n) {
		FfmTesting.title();
		var m = Layouts.flexStructAlloc(FfmTesting.A, FlexStruct.LAYOUT, n);
		FlexStruct.set(m, 0L, 0);
		FfmTesting.bin(m);
	}
}
