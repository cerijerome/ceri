package ceri.serial.jna.test;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.serial.jna.JnaUtil;

/**
 * Supports tests for JNA-based code.
 */
public class JnaTestUtil {

	private JnaTestUtil() {}

	/**
	 * Checks remaining memory from offset matches bytes.
	 */
	public static void assertMemory(Memory m, int offset, int... bytes) {
		assertMemory(m, offset, ArrayUtil.bytes(bytes));
	}

	/**
	 * Checks remaining memory from offset matches bytes.
	 */
	public static void assertMemory(Memory m, int offset, byte[] bytes) {
		assertThat(m.size() - offset, is((long) bytes.length));
		assertPointer(m, offset, bytes);
	}

	/**
	 * Checks memory at pointer offset, matches bytes.
	 */
	public static void assertPointer(Pointer p, int offset, int... bytes) {
		assertPointer(p, offset, ArrayUtil.bytes(bytes));
	}

	/**
	 * Checks memory at pointer offset, matches bytes.
	 */
	public static void assertPointer(Pointer p, int offset, byte[] bytes) {
		try {
			byte[] actual = JnaUtil.byteArray(p, offset, bytes.length);
			assertArray(actual, bytes);
		} catch (RuntimeException e) {
			throw new AssertionError(e.getMessage(), e);
		}
	}

}
