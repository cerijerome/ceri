package ceri.serial.jna;

import static ceri.serial.jna.JnaTestUtil.assertTestStruct;
import static ceri.serial.jna.JnaTestUtil.p;
import static ceri.serial.jna.JnaTestUtil.populate;
import java.util.HashSet;
import java.util.Set;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.serial.jna.JnaTestUtil.TestStruct;

/**
 * Provides JNA test data. Prevents gc that may unexpectedly cause a test to fail.
 * 
 * <pre>
 * memoryArrayByVal = { { -1, -2, -3 }, { -4, -5, -6 }, { -7, -8, -9 } }
 * structByVal = {
 * 	{ 0x1111, memoryArrayByVal[0], { -1, -2, -3 } }
 * 	{ 0x2222, memoryArrayByVal[1], { -4, -5, -6 } }
 * 	{ 0x3333, memoryArrayByVal[2], { -1, -2, -3 } }
 * }
 * </pre>
 */
public class JnaTestData {
	public static final int ARRAY_SIZE = 3;
	private static final int BYTES = TestStruct.BYTES;
	private static final int I_MULTIPLIER = 0x1111;
	private final Set<Memory> memCache = new HashSet<>(); // prevent gc on memory
	public final Memory[] memoryArrayByVal;
	public final TestStruct[] structArrayByVal;
	public final Pointer[] pointerArrayByVal;

	public static JnaTestData of() {
		return new JnaTestData();
	}

	private JnaTestData() {
		memoryArrayByVal = memoryArrayByVal();
		structArrayByVal = structArrayByVal(memoryArrayByVal);
		pointerArrayByVal = pointerArrayByVal(structArrayByVal);
	}

	public Pointer structArrayByValPointer(int i) {
		return p(structArrayByVal[0].getPointer().share(i * TestStruct.SIZE));
	}

	public Pointer structArrayByRefPointer(int i) {
		return p(pointerArrayByVal[0].share(i * Pointer.SIZE));
	}

	public Pointer pointerArrayByValPointer(int i) {
		return structArrayByRefPointer(i);
	}

	public void assertStruct(TestStruct t, int i) {
		assertTestStruct(t, structArrayByVal[i]);
	}

	public void assertEmptyStruct(TestStruct t) {
		assertTestStruct(t, new TestStruct());
	}

	/**
	 * Create contiguous memory array.
	 */
	private Memory[] memoryArrayByVal() {
		Memory m = m(ARRAY_SIZE * BYTES);
		for (int i = 0; i < m.size(); i++)
			m.setByte(i, (byte) -(i + 1));
		Memory[] array = new Memory[ARRAY_SIZE];
		for (int i = 0; i < array.length; i++)
			array[i] = JnaUtil.share(m, i * BYTES, BYTES);
		return array;
	}

	/**
	 * Create contiguous struct array.
	 */
	private TestStruct[] structArrayByVal(Memory[] ps) {
		TestStruct[] array = new TestStruct[ARRAY_SIZE];
		Memory m = m((array.length + 1) * TestStruct.SIZE);
		for (int i = 0; i < array.length; i++) {
			Pointer p = m.share(i * TestStruct.SIZE);
			array[i] = new TestStruct(p);
			populate(array[i], (i + 1) * I_MULTIPLIER, ps[i], JnaUtil.bytes(ps[i], 0, BYTES));
			array[i].write();
		}
		return array;
	}

	/**
	 * Create null-terminated contiguous pointer array referencing structs.
	 */
	private Pointer[] pointerArrayByVal(TestStruct[] ts) {
		Pointer[] array = new Pointer[ts.length];
		Memory m = m((array.length + 1) * Pointer.SIZE); // null-terminated
		for (int i = 0; i < array.length; i++) {
			array[i] = m.share(i * Pointer.SIZE);
			array[i].setPointer(0, ts[i].getPointer());
		}
		return array;
	}

	/**
	 * Allocate memory and save reference to avoid gc.
	 */
	private Memory m(int size) {
		Memory m = JnaUtil.calloc(size);
		memCache.add(m);
		return m;
	}
}
