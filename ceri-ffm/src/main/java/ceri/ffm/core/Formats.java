package ceri.ffm.core;

import java.lang.foreign.MemorySegment;
import java.nio.Buffer;
import ceri.common.array.RawArray;
import ceri.common.collect.Maps;
import ceri.common.io.Buffers;
import ceri.common.math.Maths;
import ceri.common.text.Chars;
import ceri.common.text.Joiner;
import ceri.common.text.Transformer;
import ceri.ffm.type.BufferType;
import ceri.ffm.type.Callback;
import ceri.ffm.type.Group;
import ceri.ffm.type.Memory;
import ceri.ffm.type.PointerType;

/**
 * Provides argument string formatting.
 */
public class Formats {
	/** Compact transforms for a single line. */
	public static Transformer COMPACT = compactTransformer(16, 5);
	/** Longer transforms with multiple lines. */
	public static Transformer VERBOSE = verboseTransformer();

	private Formats() {}

	/**
	 * Apply the compact transformer to the argument.
	 */
	public static String compact(Object obj) {
		return COMPACT.apply(obj);
	}

	/**
	 * Apply the verbose transformer to the argument.
	 */
	public static String verbose(Object obj) {
		return VERBOSE.apply(obj);
	}

	/**
	 * Shows integer in decimal, and hex if outside a simple decimal range.
	 */
	public static String integer(Number number) {
		if (Maths.within(number.longValue(), -1, 9)) return String.valueOf(number);
		return String.format("%1$d|0x%1$x", number);
	}

	/**
	 * Shows escaped and quoted char sequences.
	 */
	public static String chars(CharSequence chars, int limit) {
		if (limit < 0 || chars.length() <= limit) return "\"" + Chars.escape(chars) + "\"";
		return "\"" + Chars.escape(chars.subSequence(0, Math.max(0, limit - 1))) + "..\"";
	}

	/**
	 * Shows struct and union member values as a map.
	 */
	public static String group(Transformer.Context context, Group<?, ?> group) {
		var map = Maps.<Object, Object>link();
		Group.forEachMember(group, (m, t) -> map.put(Transformer.raw(m.name()), t));
		return context.apply(map);
	}

	/**
	 * Shows typed pointer memory location and type instance.
	 */
	public static String typedPointer(Transformer.Context context,
		PointerType.Indexable<?, ?, ?> pointer) {
		var array = pointer.getArray(1, false);
		return context.apply(pointer.memory())
			+ (RawArray.isEmpty(array) ? "" : context.apply(array));
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

	// support

	private static Transformer verboseTransformer() {
		return Transformer.builder() //
			.add(Formats::integer, Byte.class, Short.class, Integer.class, Long.class) //
			.add((_, c) -> Formats.chars(c, -1), CharSequence.class) //
			.add(Formats::buffer, Buffer.class) //
			.add((_, m) -> Memory.string(m), MemorySegment.class) //
			.add(Formats::typedPointer, PointerType.Indexable.class) //
			.add(Formats::pointer, PointerType.class) //
			.add(c -> Callback.toString(c), Callback.class) //
			.build();
	}

	private static Transformer compactTransformer(int stringSize, int sequenceSize) {
		return Transformer.builder() //
			.iterables(Transformer.joiner(Joiner.ARRAY, sequenceSize)) //
			.maps(Transformer.joiner(Joiner.LIST, sequenceSize), "=") //
			.add(Formats::integer, Byte.class, Short.class, Integer.class, Long.class) //
			.add((_, c) -> Formats.chars(c, stringSize), CharSequence.class) //
			.add(Formats::buffer, Buffer.class) //
			.add((_, m) -> Memory.string(m), MemorySegment.class) //
			.add(Formats::typedPointer, PointerType.Indexable.class) //
			.add(Formats::pointer, PointerType.class) //
			.add(c -> Callback.toString(c), Callback.class) //
			.add(Formats::group, Group.class) //
			.build();
	}
}