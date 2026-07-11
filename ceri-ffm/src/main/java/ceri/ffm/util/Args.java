package ceri.ffm.util;

import java.lang.foreign.MemorySegment;
import java.nio.Buffer;
import java.util.Map;
import ceri.common.collect.Maps;
import ceri.common.function.Functions;
import ceri.common.io.Buffers;
import ceri.common.text.Chars;
import ceri.common.text.Joiner;
import ceri.common.text.Transformer;
import ceri.ffm.core.Segments;
import ceri.ffm.reflect.Refine.Nul;
import ceri.ffm.reflect.Refine.Size;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.type.BufferType;
import ceri.ffm.type.Group;
import ceri.ffm.type.Group.Fields;
import ceri.ffm.type.IntType.ssize_t;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.PointerType;
import ceri.ffm.type.Primitive;
import ceri.ffm.type.Struct;
import ceri.ffm.type.Union;

/**
 * Utility to create strings from method arguments. Arrays and Iterable types are expanded.
 */
public class Args {
	/** Compact transforms for a single line. */
	public static Transformer COMPACT = compact(16, 5);
	/** Longer transforms with multiple lines. */
	public static Transformer FULL = full();

	@Fields({ "i", "s" })
	public static class union extends Union<union> {
		public static final Union.Supporter<union> $ = Union.support(union.class);
		public int i = -1;
		public @Size(3) String s = "abc";
	}

	@Fields({ "i", "s" })
	public static class struct extends Struct<struct> {
		public static final Struct.Supporter<struct> $ = Struct.support(struct.class);
		public @Nul int[][] i = { { -1, 1, -2, 2, 0 }, {} };
		public @Size(3) String s = "ABC";
	}

	public static void main(String[] args) {
		Functions.Consumer<String> con = s -> System.out.println(s);
		System.out.println(con);

		Object[] values = { //
			"abc\nde\0", new int[] { 1, -1, 0, 2, -2 }, //
			Map.of("a", 1, "b", 2), ssize_t.$.ofAll(1, -1, 0, 2, -2), //
			new union(), new struct(), //
			Buffers.SHORT.of(-1, 1, 0), //
			Primitive.BOOL.allocAll(true, false, true), //
			Pointer.of(Pointer.ofByte(-1)), //
		};
		print(FULL, values);
		print(COMPACT, values);
	}

	private static void print(Transformer transformer, Object[] values) {
		FfmTesting.title(transformer.toString());
		System.out.println(Joiner.ARRAY.joinAll(transformer, values));
	}

	/**
	 * Wrapper to prevent string transformation.
	 */
	public record Raw(Object name) {
		@Override
		public String toString() {
			return String.valueOf(name());
		}
	}

	/**
	 * Shows escaped and quoted char sequences.
	 */
	public static String chars(CharSequence chars, int limit) {
		if (limit < 0 || chars.length() <= limit) return "\"" + Chars.escape(chars) + "\"";
		return "\"" + Chars.escape(chars.subSequence(0, limit)) + "..\"";
	}

	/**
	 * Shows struct and union member values as a map.
	 */
	public static String group(Transformer.Context context, Group<?, ?> group) {
		var map = Maps.<Raw, Object>link();
		Group.forEachMember(group, (m, t) -> map.put(new Raw(m.name()), t));
		return context.apply(map);
	}

	/**
	 * Shows typed pointer memory location and type instance.
	 */
	public static String typedPointer(Transformer.Context context,
		PointerType.Indexable<?, ?, ?> pointer) {
		var array = pointer.getArray(1, false);
		return context.apply(pointer.memory()) + context.apply(array);
	}

	/**
	 * Shows untyped pointer memory location.
	 */
	public static String pointer(Transformer.Context context, PointerType pointer) {
		return context.apply(pointer.memory());
	}

	/**
	 * Shows buffer content array.
	 */
	public static String buffer(Transformer.Context context, Buffer buffer) {
		var buffers = BufferType.from(buffer).buffers();
		var array = Buffers.apply(buffer, buffers::get);
		return context.apply(array);
	}

	public static String applyAll(Transformer transformer, Object... args) {
		var b = new StringBuilder();
		for (var arg : args)
			b.append(b.isEmpty() ? "" : ", ").append(transformer.apply(arg));
		return b.toString();
	}

	// support

	private static Transformer full() {
		return Transformer.builder() //
			.add(CharSequence.class, (_, c) -> chars(c, -1)) //
			.add(Buffer.class, Args::buffer) //
			.add(MemorySegment.class, (_, m) -> Segments.string(m)) //
			.add(PointerType.Indexable.class, Args::typedPointer) //
			.add(PointerType.class, Args::pointer) //
			.build();
	}

	private static Transformer compact(int stringSize, int sequenceSize) {
		return Transformer.builder() //
			.iterables(Transformer.joiner(Joiner.ARRAY, sequenceSize)) //
			.maps(Transformer.joiner(Joiner.LIST, sequenceSize), "=") //
			.add(CharSequence.class, (_, c) -> chars(c, stringSize)) //
			.add(Buffer.class, Args::buffer) //
			.add(MemorySegment.class, (_, m) -> Segments.string(m)) //
			.add(PointerType.Indexable.class, Args::typedPointer) //
			.add(PointerType.class, Args::pointer) //
			.add(Group.class, (c, g) -> group(c, g)) //
			.build();
	}
}
