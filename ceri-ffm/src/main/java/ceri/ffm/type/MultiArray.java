package ceri.ffm.type;

import java.lang.foreign.MemorySegment;
import ceri.common.array.Dimensions;
import ceri.common.array.RawArray;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Segments;

/**
 * Support for accessing multi-dimensional arrays. Terminology uses 'twig' for a 1-d array.
 */
public class MultiArray {

	private MultiArray() {}

	/**
	 * An action for iteration.
	 */
	public interface Iteration<T> {
		/** Accepts a value, memory and offset, returns the number of bytes consumed. */
		long apply(T value, MemorySegment memory, long offset);
	}

	/**
	 * Modifies dimensions to the required count, for fixed-size and nul-term arrays. A missing
	 * nul-term twig dimension is set to the given maximum size, otherwise missing dimensions are
	 * set to size 1. A nul-term maximum of 0 uses the default maximum..
	 */
	public static Dimensions fix(Dimensions dims, int count, boolean nulTerm, int nulTermMax) {
		if (count <= 0) return Dimensions.NONE;
		if (dims.count() == count) return dims;
		var ints = new int[count];
		for (int i = 0; i < count; i++)
			ints[i] = dims.dim(i);
		if (nulTerm && dims.count() < count) ints[count - 1] = nulTermMax;
		return Dimensions.of(ints);
	}

	/**
	 * Creates a multi-dimensional array stub with empty twigs, to use with replace-twigs logic.
	 */
	public static <T> T twigStubs(Class<?> cls, Dimensions dims) {
		if (cls == null || dims == null || dims.isEmpty()) return null;
		int[] sizes = dims.array();
		sizes[sizes.length - 1] = 0;
		return RawArray.ofType(cls, sizes);
	}

	/**
	 * Iterates over the leaves of a multi-dimensional array. The action accepts each element, the
	 * memory segment, and the offset into the segment, returning the bytes consumed.
	 */
	public static <T> long iterate(Object array, MemorySegment memory, Iteration<T> action) {
		int dims = RawArray.dimensions(array);
		return iterate(array, 0, dims, memory, 0L, action);
	}

	/**
	 * Iterates over the leaves of a multi-dimensional array. The action accepts each element, the
	 * memory segment, and the offset into the segment, returning the bytes consumed.
	 */
	public static <T> long iterate(Object array, MemorySegment memory, long offset, long length,
		Iteration<T> action) {
		return iterate(array, Segments.slice(memory, offset, length), action);
	}

	/**
	 * Iterates over the 1-d arrays of a multi-dimensional array. The action accepts each 1-d array,
	 * the memory segment, and the offset into the segment, returning the bytes consumed.
	 */
	public static <A> long iterateTwigs(Object array, MemorySegment memory, Iteration<A> action) {
		int dims = RawArray.dimensions(array);
		return iterate(array, 1, dims, memory, 0L, action);
	}

	/**
	 * Iterates over the 1-d arrays of a multi-dimensional array. The action accepts each 1-d array,
	 * the memory segment, and the offset into the segment, returning the bytes consumed.
	 */
	public static <A> long iterateTwigs(Object array, MemorySegment memory, long offset,
		long length, Iteration<A> action) {
		return iterateTwigs(array, Segments.slice(memory, offset, length), action);
	}

	/**
	 * Iterates over the 1-d arrays of a multi-dimensional array. The action accepts each 1-d array,
	 * and returns a replacement. Returning the same array or null results in no action.
	 */
	public static <A, U> U replaceTwigs(Object array, Functions.Operator<A> action) {
		int dims = RawArray.dimensions(array);
		return Reflect.unchecked(replace(array, 1, dims, action));
	}

	// support

	private static <T> long iterate(Object array, int min, int dims, MemorySegment memory,
		long offset, Iteration<T> action) {
		if (dims < min) return offset;
		if (dims == min) return offset + action.apply(Reflect.unchecked(array), memory, offset);
		dims--;
		for (int i = 0; i < RawArray.length(array); i++)
			offset = iterate(RawArray.get(array, i), min, dims, memory, offset, action);
		return offset;
	}

	private static <T> Object replace(Object array, int min, int dims,
		Functions.Operator<T> action) {
		if (dims < min) return null;
		if (dims == min) return action.apply(Reflect.unchecked(array));
		dims--;
		for (int i = 0; i < RawArray.length(array); i++) {
			var element = RawArray.get(array, i);
			var replace = replace(element, min, dims, action);
			if (replace != null && replace != element) RawArray.set(array, i, replace);
		}
		return array;
	}
}
