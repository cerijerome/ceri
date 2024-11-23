package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertEquals;
import java.util.function.LongFunction;

/**
 * Class to test int type behavior.
 */
public class IntTypeTester {

	@SuppressWarnings("serial")
	public static class Int32 extends IntType {
		public Int32(long value) {
			super(4, value, false);
		}
	}

	@SuppressWarnings("serial")
	public static class Uint32 extends IntType {
		public Uint32(long value) {
			super(4, value, true);
		}
	}

	@SuppressWarnings("serial")
	public static class Int64 extends IntType {
		public Int64(long value) {
			super(8, value, false);
		}
	}

	@SuppressWarnings("serial")
	public static class Uint64 extends IntType {
		public Uint64(long value) {
			super(8, value, true);
		}
	}

	public static void main(String[] args) {
		testInt32(-1L, -1, -1L);
		testInt32(0x80000000L, 0x80000000, 0x80000000L);
		testInt32(-0x80000000L, 0x80000000, -0x80000000L);
		testInt32(0x100000000L, 0, 0); // Error: exceeds capacity
		testInt32(-0x100000000L, 0, 0); // Error: exceeds capacity
		testInt32(0xffffffffL, -1, 0xffffffffL);
		testInt32(-0xffffffffL, 0, 0); // Error: exceeds capacity
		System.out.println();

		testUint32(-1L, -1, 0xffffffffL); // different
		testUint32(0x80000000L, 0x80000000, 0x80000000L);
		testUint32(-0x80000000L, 0x80000000, 0x80000000L); // different
		testUint32(0x100000000L, 0, 0); // Error: exceeds capacity
		testUint32(-0x100000000L, 0, 0); // Error: exceeds capacity
		testUint32(0xffffffffL, -1, 0xffffffffL);
		testUint32(-0xffffffffL, 0, 0); // Error: exceeds capacity
		System.out.println();

		testInt64(-1L, -1L, -1L);
		testInt64(0x80000000L, 0x80000000L, 0x80000000L);
		testInt64(-0x80000000L, -0x80000000L, -0x80000000L);
		testInt64(0x100000000L, 0x100000000L, 0x100000000L);
		testInt64(-0x100000000L, -0x100000000L, -0x100000000L);
		testInt64(0xffffffffL, 0xffffffffL, 0xffffffffL);
		testInt64(-0xffffffffL, -0xffffffffL, -0xffffffffL);
		System.out.println();

		testUint64(-1L, -1L, -1L);
		testUint64(0x80000000L, 0x80000000L, 0x80000000L);
		testUint64(-0x80000000L, -0x80000000L, -0x80000000L);
		testUint64(0x100000000L, 0x100000000L, 0x100000000L);
		testUint64(-0x100000000L, -0x100000000L, -0x100000000L);
		testUint64(0xffffffffL, 0xffffffffL, 0xffffffffL);
		testUint64(-0xffffffffL, -0xffffffffL, -0xffffffffL);
		System.out.println();

	}

	private static void testInt32(long n, Object number, long value) {
		test("Int32", Int32::new, n, number, value);
	}

	private static void testUint32(long n, Object number, long value) {
		test("Uint32", Uint32::new, n, number, value);
	}

	private static void testInt64(long n, Object number, long value) {
		test("Int64", Int64::new, n, number, value);
	}

	private static void testUint64(long n, Object number, long value) {
		test("Uint64", Uint64::new, n, number, value);
	}

	private static <T extends IntType> void test(String name, LongFunction<T> constructor, long n,
		Object number, long value) {
		System.out.printf("%s(%d 0x%x) => ", name, n, n);
		try {
			var t = constructor.apply(n);
			System.out.printf("%s ", t);
			assertEquals(t.toNative(), number, "toNative()");
			System.out.printf("number=0x%x ", number);
			assertEquals(t.longValue(), value, "longValue()");
			System.out.printf("long=0x%x", value);
		} catch (RuntimeException e) {
			System.out.print("ERROR: " + e.getMessage());
		} finally {
			System.out.println();
		}
	}

}
