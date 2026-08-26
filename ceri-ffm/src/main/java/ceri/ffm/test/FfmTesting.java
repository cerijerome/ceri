package ceri.ffm.test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Supplier;
import ceri.common.array.RawArray;
import ceri.common.collect.Lists;
import ceri.common.data.Bytes;
import ceri.common.function.Enclosure;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.ClassReInitializer;
import ceri.common.reflect.Reflect;
import ceri.common.test.BinaryPrinter;
import ceri.common.test.Testing;
import ceri.common.text.Chars;
import ceri.common.text.Strings;
import ceri.ffm.core.Caller;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Library;
import ceri.ffm.type.Memory;
import ceri.ffm.type.PointerType;
import ceri.ffm.type.Primitive;
import ceri.ffm.type.Support;
import ceri.ffm.util.FfmOs;

public class FfmTesting {
	public static BinaryPrinter P = BinaryPrinter.STD;
	public static final SegmentAllocator A = Arena.ofAuto();

	private FfmTesting() {}

	/**
	 * A wrapper for repeatedly overriding a native library.
	 */
	public static class Lib<T> implements Functions.Closeable {
		private final Enclosure.Repeater<RuntimeException, T> repeater;

		private Lib(Library<? super T> library, Supplier<? extends T> constructor) {
			repeater = Enclosure.Repeater.unsafe(() -> library.enclosed(constructor.get()));
		}

		/**
		 * Re-initializes the library override.
		 */
		public T init() {
			return repeater.init();
		}

		/**
		 * Returns the current override, which will be null if uninitialized.
		 */
		public T lib() {
			return repeater.ref();
		}

		@Override
		public void close() {
			repeater.close();
		}
	}

	/**
	 * Utility to generate test values.
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

		/**
		 * Generates a string value.
		 */
		public static String str(int c, int len) {
			return str(c, len, len);
		}

		/**
		 * Generates a string value.
		 */
		public static String str(int c, int min, int max) {
			int len = Maths.random(min, max);
			var b = new StringBuilder();
			for (int i = 0; i < len; i++)
				b.append((char) (c + i));
			return b.toString();
		}
	}

	/**
	 * Allocates a contiguous memory block of sliced into multiple sizes, with alignment padding.
	 */
	public static class Alloc {
		private final SegmentAllocator allocator;
		private long alignment = 1L;
		private long offset = 0L;
		private int filler = 0xff;
		private final List<long[]> slices = Lists.<long[]>of();

		/**
		 * Returns a new block allocator instance with auto allocator.
		 */
		public static Alloc of() {
			return of(Memory.auto());
		}

		/**
		 * Returns a new block allocator instance with given allocator.
		 */
		public static Alloc of(SegmentAllocator allocator) {
			return new Alloc(allocator);
		}

		private Alloc(SegmentAllocator allocator) {
			this.allocator = allocator;
		}

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
			alignment = Math.max(alignment, align);
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
			mems[0] = fill(allocator.allocate(offset, alignment), filler);
			int index = 1;
			for (var slice : slices)
				mems[index++] = fill(Memory.slice(mems[0], slice[0], slice[1]), 0);
			return mems;
		}
	}

	/**
	 * Identifier snapshot for a method call, with current OS, thread and stack trace.
	 */
	public static class Identifier {
		private static final int BACK = 2;
		private final Class<?> cls;
		private final String descriptor;
		private final FfmOs ffmOs;
		private final Thread thread;
		private final StackTraceElement[] elements;
		private static int stackSize = 5;

		/**
		 * Sets the default stack size, to use when -1 is specified.
		 */
		public static void stack(int size) {
			stackSize = size;
		}

		private Identifier(String format, Object... args) {
			var caller = Reflect.previousCaller(BACK);
			cls = caller.cls();
			descriptor = Strings.isEmpty(format) ? caller.method : Strings.format(format, args);
			ffmOs = FfmOs.current();
			thread = Thread.currentThread();
			elements = thread.getStackTrace();
		}

		/**
		 * Prints this identifier.
		 */
		public void print() {
			print(0);
		}

		/**
		 * Prints this identifier and a limited stack trace.
		 */
		public void print(int stack) {
			FfmTesting.out(this);
			printStack(stack);
		}

		/**
		 * Prints this identifier as a title.
		 */
		public void title() {
			title(0);
		}

		/**
		 * Prints this identifier as a title, and prints a limited stack trace.
		 */
		public void title(int stack) {
			FfmTesting.title(toString());
			printStack(stack);
		}

		@Override
		public String toString() {
			return String.format("%s.%s%s:%s:%s", Reflect.name(cls), descriptor,
				Reflect.hashId(cls), ffmOs, thread.threadId());
		}

		private void printStack(int count) {
			if (count < 0) count = stackSize;
			count = Math.min(count, elements.length - BACK - 1);
			for (int i = 0; i < count; i++)
				FfmTesting.out("- " + elements[BACK + 1 + i]);
		}
	}

	/**
	 * Returns a wrapper for repeatedly overriding a native library.
	 */
	public static <T> Lib<T> lib(Library<? super T> library, Functions.Supplier<T> constructor) {
		return new Lib<>(library, constructor);
	}

	/**
	 * Allocates a contiguous memory block of sliced into multiple sizes, with alignment padding.
	 */
	public static MemorySegment[] alloc(MemoryLayout... layouts) {
		return Alloc.of().add(layouts).alloc();
	}

	/**
	 * Allocates a segment using an encoded, nul-term string.
	 */
	public static MemorySegment alloc(String s) {
		return alloc(s, null);
	}

	/**
	 * Allocates a segment using an encoded, nul-term string.
	 */
	public static MemorySegment alloc(String s, Charset charset) {
		return Memory.auto().allocateFrom(s, Chars.safe(charset));
	}

	/**
	 * Allocates a segment from values.
	 */
	public static MemorySegment allocBytes(int... values) {
		return Primitive.BYTE.allocAll(Memory.auto(), false, values);
	}

	/**
	 * Allocates a segment from values.
	 */
	public static MemorySegment alloc(int... values) {
		return Primitive.INT.allocAll(Memory.auto(), false, values);
	}

	/**
	 * Allocates a segment from values.
	 */
	public static MemorySegment alloc(long... values) {
		return Primitive.LONG.allocAll(Memory.auto(), false, values);
	}

	/**
	 * Allocates a segment filled with random bytes.
	 */
	public static MemorySegment randomBytes(int size) {
		return Primitive.BYTE.allocArray(A, Testing.randomBytes(size), false);
	}

	/**
	 * Allocates a segment filled with random values, and nuls at given indexes.
	 */
	public static MemorySegment random(int size, Support<?, ?, ?, ?> support, double... nuls) {
		var memory = randomBytes(support.layoutSize() * size);
		var term = support.term();
		for (var nul : nuls)
			term.set(memory, (int) (nul * support.layoutSize()));
		return memory;
	}

	/**
	 * Prints the object as an argument.
	 */
	public static void arg(Object arg) {
		P.message(Caller.Transform.COMPACT.apply(arg));
	}

	/**
	 * Creates an identifier for the caller method.
	 */
	public static Identifier identifier() {
		return new Identifier("");
	}

	/**
	 * Creates an identifier for the caller, with custom descriptor.
	 */
	public static Identifier identifier(String format, Object... args) {
		return new Identifier(format, args);
	}

	/**
	 * Prints the caller method as a title.
	 */
	public static void title() {
		var caller = Reflect.previousCaller(1);
		title(caller.cls + "." + caller.method);
	}

	/**
	 * Prints a title with border.
	 */
	public static void title(String format, Object... args) {
		var title = Strings.format(format, args);
		var border = "+-" + "-".repeat(title.length()) + "-+";
		P.message(border);
		P.message("| " + title + " |");
		P.message(border);
	}

	/**
	 * Prints the object.
	 */
	public static void out(Object obj) {
		P.message(RawArray.toString(obj));
	}

	/**
	 * Prints the binary contents of the memory segments.
	 */
	public static void bin(MemorySegment... ms) {
		for (var m : ms)
			bin(m, 0);
	}

	/**
	 * Prints the binary contents of the pointer.
	 */
	public static void bin(PointerType.Raw... ps) {
		for (var p : ps) {
			P.message(p);
			print(p.memory());
			P.message("");
		}
	}

	/**
	 * Prints the binary contents of the memory segment range.
	 */
	public static void bin(MemorySegment m, long offset) {
		bin(m, offset, Long.MAX_VALUE);
	}

	/**
	 * Prints the binary contents of the memory segment range.
	 */
	public static void bin(MemorySegment m, long offset, long length) {
		P.message(Memory.string(m));
		print(Memory.slice(m, offset, length));
		P.message("");
	}

	/**
	 * Reloads and instantiates the test class, overriding the current OS. Support classes are
	 * reloaded if accessed.
	 */
	public static void testAsOs(FfmOs os, Class<?> testCls, Class<?>... reloads) {
		os.accept(_ -> ClassReInitializer.of(testCls, reloads).reinit());
	}

	/**
	 * Reloads and instantiates the test class for each supported OS, overriding the current OS.
	 * Support classes are reloaded if accessed.
	 */
	public static void testForEachOs(Class<?> testCls, Class<?>... reloads) {
		FfmOs.forEach(os -> testAsOs(os, testCls, reloads));
	}

	// support

	private static void print(MemorySegment m) {
		if (m != null) P.print(m.toArray(Layouts.BYTE));
	}

	private static MemorySegment fill(MemorySegment m, int filler) {
		Memory.fill(m, filler);
		return m;
	}
}
