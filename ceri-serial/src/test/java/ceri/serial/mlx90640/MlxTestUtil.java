package ceri.serial.mlx90640;

import static ceri.serial.mlx90640.Mlx90640.COLUMNS;
import static ceri.serial.mlx90640.Mlx90640.ROWS;
import java.lang.reflect.Array;
import ceri.common.data.ByteUtil;

/**
 * Support for ML90640 tests.
 */
public class MlxTestUtil {

	private MlxTestUtil() {}

	/**
	 * Print array values.
	 */
	public static void printArray(Object array) {
		System.out.print("{ ");
		for (int i = 0; i < Array.getLength(array); i++) {
			if (i > 0) System.out.print(", ");
			System.out.print(Array.get(array, i));
		}
		System.out.println(" }");
	}

	/**
	 * Print array values in rows.
	 */
	public static void printRows(Object array) {
		for (int p = 0, i = 0; i < ROWS; i++) {
			System.out.print("{ ");
			for (int j = 0; j < COLUMNS; j++, p++) {
				if (j > 0) System.out.print(", ");
				System.out.print(Array.get(array, p));
			}
			System.out.println(" },");
		}
	}

	/**
	 * Generate a byte array from 2-byte words.
	 */
	public static byte[] bytes(int... words) {
		byte[] bytes = new byte[words.length * Short.BYTES];
		for (int p = 0, i = 0; i < words.length; i++)
			p = ByteUtil.writeMsb(words[i], bytes, p, Short.BYTES);
		return bytes;
	}

}
