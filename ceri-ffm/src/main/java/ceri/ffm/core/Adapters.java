package ceri.ffm.core;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import ceri.common.array.RawArray;
import ceri.common.io.Direction;
import ceri.common.reflect.Reflect;
import ceri.common.text.Chars;

/**
 * Call argument and return value processing.
 */
public class Adapters {
	private static final Call.Adapter NULL_ADAPTER = null;
	private static final Call.Resolver NULL_RESOLVER = null;
	private static final Arena arena = Arena.ofAuto();

	private Adapters() {}

	public static void main(String[] args) {
		int[] ints = { 0, 0, 3 };
		var alloc = Allocators.INTS;
		var layout = alloc.layout(null, 0, ByteOrder.BIG_ENDIAN);
		System.out.println(Arrays.toString(ints));
		var m = alloc.allocFrom(arena, layout, ints);
		m.setAtIndex(layout, 0, -1);
		m.setAtIndex(layout, 1, 1);
		alloc.copy(layout, m, ints);
		System.out.println(Arrays.toString(ints));
	}

	public static class Arg {
		private Arg() {}

		public static Call.Arg value(Calls.Context context, Class<?> cls) {
			var align = context.refine().align();
			var order = context.refine().order();
			return value(cls, align, order);
		}

		public static Call.Arg value(Class<?> cls, long align, ByteOrder order) {
			var layout = Layouts.set(Layouts.ofValue(cls), null, align, order);
			return Call.Arg.of(layout, NULL_ADAPTER, NULL_RESOLVER, cls.getSimpleName());
		}

		public static <T extends IntType<T>> Call.Arg intType(Calls.Context context, Class<T> cls) {
			var align = context.refine().align();
			var order = context.refine().order();
			return intType(cls, align, order);
		}

		public static <T extends IntType<T>> Call.Arg intType(Class<T> cls, long align,
			ByteOrder order) {
			var layout = Layouts.set(IntType.layout(cls), null, align, order);
			return Call.Arg.of(layout, (_, _, o) -> Reflect.<T>unchecked(o).getNative(),
				NULL_RESOLVER, cls.getSimpleName());
		}

		public static Call.Arg string(Calls.Context context) {
			return string(context.refine().chars().charset());
		}

		public static Call.Arg string(Charset charset) {
			return Call.Arg.of(Layouts.POINTER,
				(a, _, o) -> Allocators.STRING.alloc(a, (String) o, charset), NULL_RESOLVER,
				label(charset));
		}

		public static <A, L extends ValueLayout> Call.Arg array(Calls.Context context,
			Allocators.OfArray<A, L> alloc) {
			var dir = context.refine().direction();
			var align = context.refine().align();
			var order = context.refine().order();
			return array(alloc, dir, align, order);
		}

		public static <A, L extends ValueLayout> Call.Arg array(Allocators.OfArray<A, L> alloc,
			Direction dir, long align, ByteOrder order) {
			var layout = alloc.layout(null, align, order);
			return Call.Arg.of(Layouts.POINTER, arrayArgAdapter(dir, alloc, layout),
				arrayArgResolver(dir, alloc, layout), label(dir, alloc.name(null)));
		}
	}

	public static class Return {
		private Return() {}

		public static Call.Return value(Calls.Context context, Class<?> cls) {
			var align = context.refine().align();
			var order = context.refine().order();
			return value(cls, align, order);
		}

		public static Call.Return value(Class<?> cls, long align, ByteOrder order) {
			var layout = Layouts.set(Layouts.ofValue(cls), null, align, order);
			return Call.Return.of(layout, NULL_ADAPTER, cls.getSimpleName());
		}

		public static <T extends IntType<T>> Call.Return intType(Calls.Context context,
			Class<T> cls) {
			var align = context.refine().align();
			var order = context.refine().order();
			return intType(cls, align, order);
		}

		public static <T extends IntType<T>> Call.Return intType(Class<T> cls, long align,
			ByteOrder order) {
			var layout = Layouts.set(IntType.layout(cls), null, align, order);
			return Call.Return.of(layout, (_, _, n) -> IntType.from(cls, n), cls.getSimpleName());
		}

		public static Call.Return string(Calls.Context context) {
			var chars = context.refine().chars();
			return string(chars.charset(), chars.max());
		}

		public static Call.Return string(Charset charset, int max) {
			return Call.Return.of(Layouts.POINTER,
				(_, _, n) -> Allocators.STRING.from((MemorySegment) n, charset, max),
				label(charset));
		}

		public static <A, L extends ValueLayout> Call.Return array(Calls.Context context,
			Allocators.OfArray<A, L> alloc) {
			var size = context.refine().size();
			var align = context.refine().align();
			var order = context.refine().order();
			return array(alloc, size, align, order);
		}

		public static <A, L extends ValueLayout> Call.Return array(Allocators.OfArray<A, L> alloc,
			int size, long align, ByteOrder order) {
			var layout = alloc.layout(null, align, order);
			return Call.Return.of(Layouts.POINTER,
				(_, _, n) -> alloc.from((MemorySegment) n, layout, size), alloc.name(size));
		}
	}

	// support

	private static <A, L extends ValueLayout> Call.Adapter arrayArgAdapter(Direction dir,
		Allocators.OfArray<A, L> alloc, L layout) {
		return Direction.in(dir) ? (a, _, o) -> alloc.allocFrom(a, layout, Reflect.unchecked(o)) :
			(a, _, o) -> alloc.alloc(a, layout, RawArray.length(o));
	}

	private static <A, L extends ValueLayout> Call.Resolver arrayArgResolver(Direction dir,
		Allocators.OfArray<A, L> alloc, L layout) {
		return Direction.out(dir) ?
			(_, _, o, n) -> alloc.copy(layout, (MemorySegment) n, Reflect.unchecked(o)) :
			NULL_RESOLVER;
	}

	private static String label(Direction dir, String name) {
		return switch (dir) {
			case in -> "<" + name;
			case out -> ">" + name;
			default -> name;
		};
	}

	private static String label(Charset charset) {
		if (Charset.defaultCharset() == charset) return "string";
		return "string:" + Chars.compactName(charset);
	}
}
