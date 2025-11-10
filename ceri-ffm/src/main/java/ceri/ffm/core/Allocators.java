package ceri.ffm.core;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import ceri.common.array.RawArray;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.text.Chars;
import ceri.common.text.Strings;

public class Allocators {
	public static final OfArray<char[], ValueLayout.OfChar> CHARS =
		new OfArray<>(char.class, Layouts.CHAR, MemorySegment::ofArray);
	public static final OfArray<byte[], ValueLayout.OfByte> BYTES =
		new OfArray<>(byte.class, Layouts.BYTE, MemorySegment::ofArray);
	public static final OfArray<short[], ValueLayout.OfShort> SHORTS =
		new OfArray<>(short.class, Layouts.SHORT, MemorySegment::ofArray);
	public static final OfArray<int[], ValueLayout.OfInt> INTS =
		new OfArray<>(int.class, Layouts.INT, MemorySegment::ofArray);
	public static final OfArray<long[], ValueLayout.OfLong> LONGS =
		new OfArray<>(long.class, Layouts.LONG, MemorySegment::ofArray);
	public static final OfArray<float[], ValueLayout.OfFloat> FLOATS =
		new OfArray<>(float.class, Layouts.FLOAT, MemorySegment::ofArray);
	public static final OfArray<double[], ValueLayout.OfDouble> DOUBLES =
		new OfArray<>(double.class, Layouts.DOUBLE, MemorySegment::ofArray);
	public static final OfString STRING = new OfString();

	private Allocators() {}

	public record OfArray<A, L extends ValueLayout>(Class<?> componentType, L layout,
		Functions.Function<A, MemorySegment> wrapper) {
		public String name(Integer size) {
			return componentType().getSimpleName() + '[' + Strings.safe(size) + ']';
		}

		public L layout(String name, long align, ByteOrder order) {
			return Layouts.order(Layouts.align(Layouts.name(layout(), name), align), order);
		}

		public MemorySegment alloc(SegmentAllocator allocator, int count) {
			return alloc(allocator, layout(), count);
		}

		public MemorySegment alloc(SegmentAllocator allocator, L layout, int count) {
			return allocator.allocate(layout, count);
		}

		public MemorySegment allocFrom(SegmentAllocator allocator, A array) {
			return allocFrom(allocator, layout(), array);
		}

		public MemorySegment allocFrom(SegmentAllocator allocator, L layout, A array) {
			return allocator.allocateFrom(layout, wrap(array), layout(), 0, RawArray.length(array));
		}

		public A from(MemorySegment m, L layout, int size) {
			if (Memory.isNull(m)) return null;
			m = m.reinterpret(layout.byteSize() * size);
			return copy(layout, m, create(size));
		}

		public A copy(L layout, MemorySegment m, A array) {
			MemorySegment.copy(m, layout, 0, array, 0, RawArray.length(array));
			return array;
		}

		public MemorySegment wrap(A array) {
			return wrapper().apply(array);
		}

		private A create(int size) {
			return Reflect.unchecked(java.lang.reflect.Array.newInstance(componentType(), size));
		}
	}

	public static class OfString {
		private OfString() {}

		public MemorySegment alloc(SegmentAllocator allocator, String s, Charset charset) {
			if (s == null) return MemorySegment.NULL;
			return allocator.allocateFrom(s, Chars.safe(charset));
		}

		public String from(MemorySegment m, Charset charset, int maxLen) {
			if (Memory.isNull(m)) return null;
			if (m.byteSize() < maxLen) m = Memory.resize(m, maxLen);
			return m.getString(0, Chars.safe(charset));
		}
	}
}
