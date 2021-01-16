import java.util.Arrays;
import java.util.List;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class JnaStructTester {

	public static class VarArray extends Structure {
		public short dummy0;
		public int dummy1;
		public byte count;
		public byte[] array = new byte[0];

		public VarArray() {}

		public VarArray(byte[] array) {
			this.count = (byte) array.length;
			this.array = array;
		}

		public VarArray(Pointer p) {
			super(p);
		}

		@Override
		protected void ensureAllocated() {
			if (count == 0) array = new byte[1];
			super.ensureAllocated();
			if (count == 0) array = new byte[0];
		}

		@Override
		protected void writeField(StructField structField) {
			if (structField.name.equals("array") && count == 0) return;
			super.writeField(structField);
		}

		@Override
		protected Object readField(StructField structField) {
			if (structField.name.equals("array")) {
				array = new byte[count];
				if (count == 0) return null;
			}
			return super.readField(structField);
		}

		@Override
		protected List<String> getFieldOrder() {
			return List.of("dummy0", "dummy1", "count", "array");
		}
	}

	public static void main(String[] args) {
		var va0 = new VarArray(new byte[] { 1, 2, 3, 4, 5, 6, 7 });
		va0.dummy0 = 0x4321;
		va0.dummy1 = 0xabcdef;
		va0.write();

		var va1 = new VarArray();
		va1.dummy0 = 0x4321;
		va1.dummy1 = 0xabcdef;
		va1.write();
		
		print(new Pointer(Pointer.nativeValue(va0.getPointer())));
		print(new Pointer(Pointer.nativeValue(va1.getPointer())));
	}

	private static void print(Pointer p) {
		var va = new VarArray(p);
		va.read();
		System.out.println(va);
		System.out.println("byte[] array=" + Arrays.toString(va.array));
		System.out.println();
	}
}