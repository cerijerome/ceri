package ceri.common.data;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

/**
 * Support for floating point types.
 */
public class Doubles {
	private Doubles() {}

	/**
	 * Copies buffer into a new array.
	 */
	public static float[] floats(FloatBuffer buffer) {
		var array = new float[buffer.remaining()];
		buffer.get(array);
		return array;
	}

	/**
	 * Copies buffer into a new array.
	 */
	public static double[] doubles(DoubleBuffer buffer) {
		var array = new double[buffer.remaining()];
		buffer.get(array);
		return array;
	}
}
