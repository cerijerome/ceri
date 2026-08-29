package ceri.ffm.reflect;

import ceri.common.util.Os;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.reflect.CAnnotations.CType;
import ceri.ffm.reflect.CAnnotations.CType.Attr;
import ceri.ffm.reflect.CAnnotations.CUndefined;
import ceri.ffm.reflect.Refine.Size;
import ceri.ffm.reflect.Refine.Unsigned;
import ceri.ffm.type.Group.Fields;
import ceri.ffm.type.IntType;
import ceri.ffm.type.Struct;
import ceri.ffm.type.Union;
import ceri.ffm.util.FfmOs;

@CInclude("symbols.h")
public class CTestGen {
	public static final byte Fb = -111;
	public static final short Fs = -112;
	public static final int Fi = -113;
	@CType(attrs = Attr.signed)
	public static final byte Fbs = -111;
	@CType(attrs = Attr.signed)
	public static final short Fss = -112;
	@CType(attrs = Attr.signed)
	public static final int Fis = -113;
	@CType(name = "FL")
	public static final long Fl = 114;
	public static long l = 115; // not final - ignored
	public static final double Fd = 0.116; // double - ignored
	@CType(os = FfmOs.mac)
	public static final int Flm = 117;
	@CType(os = FfmOs.linux)
	public static final int Fll = 117;

	public enum E {
		a(Os.info().mac ? 0 : 1),
		@CType(name = "B")
		b(2),
		@CUndefined
		c(4);

		public final int value;

		private E(int value) {
			this.value = value;
		}
	}

	@CType(name = "IB")
	@Size(Byte.BYTES)
	@Unsigned
	public static class Ib extends IntType<Ib> {
		public Ib(Number n) {
			super(n);
		}
	}

	@Size(Integer.BYTES)
	@Unsigned
	public static class Ii extends IntType<Ii> {
		public Ii(Number n) {
			super(n);
		}
	}

	@Size(Long.BYTES)
	public static class Il extends IntType<Il> {
		public Il(Number n) {
			super(n);
		}
	}

	// abstract - ignored
	@Size(Short.BYTES)
	public static class Ia extends IntType<Ia> {
		public Ia(Number n) {
			super(n);
		}
	}

	@Fields("i")
	public static class S extends Struct<S> {
		public int i;
	}

	@Fields("i")
	public static class U extends Union<U> {
		public int i;
	}

	@Fields("i")
	@CType(name = "ST", attrs = Attr.typedef)
	public static class St extends Struct<St> {
		public int i;
	}

	// abstract - ignored
	public static abstract class Sa extends Struct<Sa> {
		public int i;
	}

	public static class Nested {
		public static final int NFi = 200;
	}

	@CUndefined
	public static class Undefined {}

	// not static - ignored
	public class Inner {}
}
