package ceri.serial.jna;

import static ceri.common.test.TestUtil.assertArray;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;

public class JnaTestUtil {

	private JnaTestUtil() {}

	public static void assertMemory(Memory m, byte[] bytes) {
		assertMemory(m, 0, bytes);
	}

	public static void assertMemory(Memory m, int offset, byte[] bytes) {
		assertMemory(m, offset, JnaUtil.size(m) - offset, bytes);
	}

	public static void assertMemory(Pointer p, int offset, int length, int... bytes) {
		assertMemory(p, offset, length, ArrayUtil.bytes(bytes));
	}

	public static void assertMemory(Pointer p, int offset, int length, byte[] bytes) {
		byte[] memBytes = JnaUtil.byteArray(p, offset, length);
		assertArray(memBytes, bytes);
	}

}
