package ceri.ffm.type;

import java.lang.foreign.MemorySegment;
import ceri.ffm.core.Segments;

public class PointerTypeTester {

	public static class myptr extends PointerType {
		public static final Supporter<myptr> $ = PointerType.support(myptr.class);

		public myptr(MemorySegment memory) {
			super(memory);
		}
	}

	public static void main(String[] args) {
		var p = new myptr(Segments.auto().allocate(13));
		var pp = Pointer.of(p);
		var v = Pointer.ofVoid(p.memory());
		var pv = Pointer.of(v).asConst();
		var i = Primitive.INT.wrapAll(1, 2, 3);
		var pi = Pointer.ofInt(i).asConst();
		var i2 = i.asSlice(1, 4);
		var pi2 = Pointer.ofInt(i2);
		System.out.println(p);
		System.out.println(pp);
		System.out.println(v);
		System.out.println(pv);
		System.out.println(i);
		System.out.println(pi);
		System.out.println(i2);
		System.out.println(pi2);
	}

}
