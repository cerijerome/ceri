package ceri.serial.clib.jna;

import java.util.function.IntFunction;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.serial.jna.Struct;
import ceri.serial.jna.Struct.Fields;

public class StructArraySpeedTester {
	private static final int S_BYTES = 1;
	private static final int M_BYTES = 1024;
	private static final int L_BYTES = 1024 * 64;

	public static void main(String[] args) {
		compareTest(1); // 77/1/1 ms (probably due to class-loading)
		compareTest(64); // 46/35/[33] ms (due to page sizes?)
		compareTest(1024); // 229/[85]/211 ms
	}

	private static void compareTest(int size) {
		System.out.printf("Testing: size=%d%n", size);
		long t;
		t = testStruct(Small::array, size);
		System.out.printf("Small::array  time: %d ms%n", t);
		t = testStruct(Medium::array, size);
		System.out.printf("Medium::array time: %d ms%n", t);
		t = testStruct(Large::array, size);
		System.out.printf("Large::array  time: %d ms%n", t);
		System.out.println();
	}

	private static <T extends Struct> long testStruct(IntFunction<T[]> arrayFn, int size) {
		long t0 = System.currentTimeMillis();
		for (T t : arrayFn.apply(size))
			t.read();
		return System.currentTimeMillis() - t0;
	}

	@Fields({ "f0", "f1", "f2", "f3", "f4" })
	public static class Small extends Struct {
		public byte[] f0 = new byte[S_BYTES];
		public short f1;
		public int f2;
		public NativeLong f3;
		public Pointer f4;

		public static class ByReference extends Small implements Structure.ByReference {}

		public static ByReference[] array(int count) {
			return arrayByVal(ByReference::new, ByReference[]::new, count);
		}
	}

	@Fields({ "f0", "f1", "f2", "f3", "f4" })
	public static class Medium extends Struct {
		public byte[] f0 = new byte[M_BYTES];
		public short f1;
		public int f2;
		public NativeLong f3;
		public Pointer f4;

		public static class ByReference extends Medium implements Structure.ByReference {}

		public static ByReference[] array(int count) {
			return arrayByVal(ByReference::new, ByReference[]::new, count);
		}
	}

	@Fields({ "f0", "f1", "f2", "f3", "f4" })
	public static class Large extends Struct {
		public byte[] f0 = new byte[L_BYTES];
		public short f1;
		public int f2;
		public NativeLong f3;
		public Pointer f4;

		public static class ByReference extends Large implements Structure.ByReference {}

		public static ByReference[] array(int count) {
			return arrayByVal(ByReference::new, ByReference[]::new, count);
		}
	}

}
