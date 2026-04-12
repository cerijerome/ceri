package ceri.ffm.test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.List;
import ceri.common.collect.Lists;
import ceri.common.data.Bytes;
import ceri.common.reflect.Reflect;
import ceri.common.test.BinaryPrinter;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Memory;

public class FfmTesting {
	public static BinaryPrinter P = BinaryPrinter.STD;
	public static final Arena A = Arena.ofAuto();

	private FfmTesting() {}

	/**
	 * Generation of test values.
	 */
	public static class Gen {
		private Gen() {}

		/**
		 * Generates a char value.
		 */
		public static char c(int c, int index) {
			return (char) (c + index);
		}

		/**
		 * Generates a byte value.
		 */
		public static byte b(int c, int index) {
			return (byte) (c + index);
		}

		/**
		 * Generates a short value.
		 */
		public static short s(int c, int index) {
			return (short) (Bytes.fromMsb(c++, c) + (index * 0x202));
		}

		/**
		 * Generates an int value.
		 */
		public static int i(int c, int index) {
			return (int) (Bytes.fromMsb(c++, c++, c++, c) + (index * 0x4040404));
		}

		/**
		 * Generates a long value.
		 */
		public static long l(int c, int index) {
			return Bytes.fromMsb(c++, c++, c++, c++, c++, c++, c++, c)
				+ (index * 0x808080808080808L);
		}
	}

	/**
	 * Allocates a contiguous memory block of sliced into multiple sizes, with alignment padding.
	 */
	public static class Alloc {
		private long offset = 0L;
		private int filler = 0xff;
		private final List<long[]> slices = Lists.<long[]>of();

		/**
		 * Returns a new block allocator instance.
		 */
		public static Alloc of() {
			return new Alloc();
		}

		private Alloc() {}

		/**
		 * Set the alignment padding fill value.
		 */
		public Alloc filler(int filler) {
			this.filler = filler;
			return this;
		}

		/**
		 * Add memory sub-blocks.
		 */
		public Alloc add(MemoryLayout... layouts) {
			for (var layout : layouts)
				add(layout, 1);
			return this;
		}

		/**
		 * Add a memory sub-block.
		 */
		public Alloc add(MemoryLayout layout, long count) {
			return add(layout.scale(0L, count), layout.byteAlignment());
		}

		/**
		 * Add a memory sub-block.
		 */
		public Alloc add(long size, long align) {
			offset += Layouts.padding(offset, align);
			slices.add(new long[] { offset, size });
			offset += size;
			return this;
		}

		/**
		 * Returns an array of the full block followed by slices for each size.
		 */
		public MemorySegment[] alloc() {
			var mems = new MemorySegment[slices.size() + 1];
			mems[0] = fill(A.allocate(offset), filler);
			int index = 1;
			for (var slice : slices)
				mems[index++] = fill(Memory.slice(mems[0], slice[0], slice[1]), 0);
			return mems;
		}
	}

	/**
	 * Allocates a contiguous memory block of sliced into multiple sizes, with alignment padding.
	 */
	public static MemorySegment[] alloc(MemoryLayout... layouts) {
		return Alloc.of().add(layouts).alloc();
	}

	/**
	 * Prints the binary contents of the memory segments.
	 */
	public static void print(MemorySegment... ms) {
		for (var m : ms)
			print(m, 0);
	}

	/**
	 * Prints the binary contents of the memory segment range.
	 */
	public static void print(MemorySegment m, long offset) {
		print(m, offset, Long.MAX_VALUE);
	}

	/**
	 * Prints the binary contents of the memory segment range.
	 */
	public static void print(MemorySegment m, long offset, long length) {
		P.message(m);
		m = Memory.slice(m, offset, length);
		if (m != null) P.print(m.toArray(Layouts.BYTE));
		P.message("");
	}

	/**
	 * Prints the caller method as a title.
	 */
	public static void title() {
		var s = Reflect.previousMethodName(1);
		P.message("%s%n%s", s, "-".repeat(s.length()));
	}
	
	// support

	private static MemorySegment fill(MemorySegment m, int filler) {
		Memory.fill(m, filler);
		return m;
	}
}
