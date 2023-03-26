package ceri.jna.util;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.math.MathUtil.ubyte;
import java.util.function.Function;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.jna.util.Struct.Fields;

/**
 * Demonstration of various var array struct types.
 */
public class VarStructTester {

	@Fields({ "dummy1", "dummy2", "dummy3" })
	public static class Sub extends Struct {
		public short dummy1;
		public int dummy2;
		public byte dummy3;

		public static class ByRef extends Sub implements Structure.ByReference {
			public ByRef(int dummy1, int dummy2, int dummy3) {
				super(null);
				this.dummy1 = (short) dummy1;
				this.dummy2 = dummy2;
				this.dummy3 = (byte) dummy3;
			}

			public ByRef(Pointer p) {
				super(p);
			}
		}

		public Sub(Pointer p) {
			super(p);
		}
	}

	@Fields({ "dummy", "count", "array" })
	public static class ByteVar extends VarStruct {
		public int dummy;
		public byte count;
		public byte[] array = new byte[0];

		public static class ByRef extends ByteVar implements Structure.ByReference {
			public ByRef(int dummy, int... array) {
				super(dummy, array);
			}

			public ByRef(Pointer p) {
				super(p);
			}
		}

		public ByteVar(int dummy, int... array) {
			this.dummy = dummy;
			this.count = (byte) array.length;
			this.array = bytes(array);
		}

		public ByteVar(Pointer p) {
			super(p);
		}

		@Override
		protected void setVarArray(int count) {
			array = new byte[count];
		}

		@Override
		protected int varCount() {
			return ubyte(count);
		}
	}

	@Fields({ "length", "dummy1", "dummy2", "array" })
	public static class LenVar extends VarStruct {
		private static final int lastOffset = new LenVar(0, 0).lastOffset();
		public int length;
		public byte dummy1;
		public short dummy2;
		public byte[] array = new byte[0];

		public LenVar(int dummy1, int dummy2, int... array) {
			this.dummy1 = (byte) dummy1;
			this.dummy2 = (short) dummy2;
			this.length = lastOffset + array.length;
			this.array = bytes(array);
		}

		public LenVar(Pointer p) {
			super(p);
		}

		@Override
		protected void setVarArray(int count) {
			array = new byte[count];
		}

		@Override
		protected int varCount() {
			return length - lastOffset;
		}
	}

	@Fields({ "dummy", "count", "array" })
	public static class TypeVar extends VarStruct {
		public int dummy;
		public byte count;
		public Sub[] array = new Sub[0];

		public TypeVar(int dummy, Sub... array) {
			this.dummy = dummy;
			this.count = (byte) array.length;
			this.array = array;
		}

		public TypeVar(Pointer p) {
			super(p);
		}

		@Override
		protected void setVarArray(int count) {
			array = new Sub[count];
		}

		@Override
		protected int varCount() {
			return ubyte(count);
		}
	}

	@Fields({ "dummy", "count", "array" })
	public static class RefVar extends VarStruct {
		public int dummy;
		public byte count;
		public Sub.ByRef[] array = new Sub.ByRef[0];

		public RefVar(int dummy, Sub.ByRef... array) {
			this.dummy = dummy;
			count = (byte) array.length;
			this.array = array;
		}

		public RefVar(Pointer p) {
			super(p);
		}

		@Override
		protected void setVarArray(int count) {
			array = new Sub.ByRef[count];
		}

		@Override
		protected int varCount() {
			return ubyte(count);
		}
	}

	@Fields({ "length", "dummy1", "dummy2", "array" })
	public static class LenRefVar extends VarStruct {
		private static final int lastOffset = new LenRefVar(0, 0).lastOffset();
		public int length;
		public byte dummy1;
		public short dummy2;
		public Sub.ByRef[] array = new Sub.ByRef[0];

		public LenRefVar(int dummy1, int dummy2, Sub.ByRef... array) {
			this.dummy1 = (byte) dummy1;
			this.dummy2 = (short) dummy2;
			this.length = lastOffset + (array.length * Native.POINTER_SIZE);
			this.array = array;
		}

		public LenRefVar(Pointer p) {
			super(p);
		}

		@Override
		protected void setVarArray(int count) {
			array = new Sub.ByRef[count];
		}

		@Override
		protected int varCount() {
			return (length - lastOffset) / Native.POINTER_SIZE;
		}
	}

	@Fields({ "dummy", "count", "array" })
	public static class VarRefVar extends VarStruct {
		public int dummy;
		public byte count;
		public ByteVar.ByRef[] array = new ByteVar.ByRef[0];

		public VarRefVar(int dummy, ByteVar.ByRef... array) {
			this.dummy = dummy;
			count = (byte) array.length;
			this.array = array;
		}

		public VarRefVar(Pointer p) {
			super(p);
		}

		@Override
		protected void setVarArray(int count) {
			array = new ByteVar.ByRef[count];
		}

		@Override
		protected int varCount() {
			return ubyte(count);
		}
	}

	public static void main(String[] args) {
		var s0 = new Sub.ByRef(0xaaaa, 0xabcdef, 0xff);
		var s1 = new Sub.ByRef(0xbbbb, 0x123456, 0xee);
		var s2 = new Sub.ByRef(0xccbb, 0xaabbcc, 0xdd);
		// Primitive var array
		printCopy(new ByteVar(0x89abcdef), ByteVar::new);
		printCopy(new ByteVar(0x89abcdef, 1, 2, 3, 4, 5), ByteVar::new);
		// Primitive var array; count from total length
		printCopy(new LenVar(0xef, 0xabcd), LenVar::new);
		printCopy(new LenVar(0xef, 0xabcd, 1, 2, 3, 4, 5), LenVar::new);
		// Struct var array; ByRef types copied to end of struct on write
		printCopy(new TypeVar(0x89abcdef), TypeVar::new);
		printCopy(new TypeVar(0x89abcdef, s0, s1, s2), TypeVar::new);
		// Struct pointer var array; does not copy var structs
		printCopy(new RefVar(0x89abcdef), RefVar::new);
		printCopy(new RefVar(0x89abcdef, s0, s1, s2), RefVar::new);
		// Struct pointer var array; does not copy var structs; count from total length
		printCopy(new LenRefVar(0xef, 0xabcd), LenRefVar::new);
		printCopy(new LenRefVar(0xef, 0xabcd, s0, s1, s2), LenRefVar::new);
		// Var struct pointer var array; does not copy var structs
		printCopy(new VarRefVar(0x89abcdef), VarRefVar::new);
		printCopy(new VarRefVar(0x89abcdef, new ByteVar.ByRef(0xaaaa, 1, 2, 3, 4, 5),
			new ByteVar.ByRef(0xbbbb), new ByteVar.ByRef(0xccbb, 1, 2, 3)), VarRefVar::new);
	}

	private static <T extends Struct> void printCopy(T t, Function<Pointer, T> constructor) {
		System.out.println("------------------------------------------------------------");
		System.out.println(t.getClass().getSimpleName() + ":");
		System.out.println();
		System.out.println(t + " ==>");
		Pointer p = new Pointer(Pointer.nativeValue(Struct.write(t).getPointer()));
		T copy = Struct.read(constructor.apply(p));
		System.out.println(copy + " <==");
		System.out.println();
	}

}
