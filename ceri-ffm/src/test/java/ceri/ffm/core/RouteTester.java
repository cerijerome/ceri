package ceri.ffm.core;

import ceri.ffm.type.Group.Fields;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Route;
import ceri.ffm.type.Struct;

public class RouteTester {

	@Fields({ "in", "pin", "l" })
	public static class outer extends Struct<outer> {
		public static final Supporter<outer> $ = support(outer.class);
		public static final Route<Pointer<inner>> IN = $.route(".in");
		public static final Route<Pointer<int[]>> IN_I = IN.sub(inner.I);
		public static final Route<Pointer.OfInt> IN_II = IN.sub(inner.II);
		public static final Route<Pointer.OfShort> IN_S = IN.sub(inner.S);
		public static final Route<Pointer<Pointer<inner>>> PIN = $.route(".pin");
		public static final Route<Pointer<inner>> PIN0 = $.route(".pin*");
		public static final Route<Pointer<int[]>> PIN0_I = PIN0.sub(inner.I);
		public static final Route<Pointer.OfInt> PIN0_II = PIN0.sub(inner.II);
		public static final Route<Pointer.OfShort> PIN0_S = PIN0.sub(inner.S);
		public static final Route<Pointer.OfLong> L = $.route(".l");
		public inner in;
		public Pointer<inner> pin;
		public long l;
	}

	@Fields({ "i", "s" })
	public static class inner extends Struct<inner> {
		public static final Supporter<inner> $ = support(inner.class);
		public static final Route<Pointer<int[]>> I = $.route(".i");
		public static final Route<Pointer.OfInt> II = $.route(".i[]");
		public static final Route<Pointer.OfShort> S = $.route(".s");
		public int[] i = new int[3];
		public short s;
	}

	public static void main(String[] args) {
		var p0 = outer.$.pointer();
		var p1 = inner.$.pointer();
		System.out.println(p0);
		System.out.println(p1);
		
		outer.IN_II.pointer(p0, 0).writeAll(false, 111, 222);
		outer.IN_II.pointer(p0, 2).write(333);
		outer.PIN.pointer(p0).write(p1);
		System.out.println(p0.get());
		System.out.println();
		
		outer.IN_I.pointer(p0).write(new int[] { -111, -222 });
		System.out.println(p0.get());
		System.out.println();
		
		outer.PIN0_II.pointer(p0, 1).write(777);
		System.out.println(p1);
		System.out.println(p1.get());
		System.out.println();
	}
}
