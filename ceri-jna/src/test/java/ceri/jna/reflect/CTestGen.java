package ceri.jna.reflect;

import static ceri.jna.util.JnaOs.linux;
import static ceri.jna.util.JnaOs.mac;
import com.sun.jna.Pointer;
import ceri.common.util.OsUtil;
import ceri.jna.reflect.CAnnotations.CInclude;
import ceri.jna.reflect.CAnnotations.CType;
import ceri.jna.reflect.CAnnotations.CType.Attr;
import ceri.jna.reflect.CAnnotations.CUndefined;
import ceri.jna.type.IntType;
import ceri.jna.type.Struct;
import ceri.jna.type.Struct.Fields;
import ceri.jna.type.Union;

@CInclude("symbols.h")
public class CTestGen {
	public static final byte Fb = 111;
	public static final short Fs = 112;
	public static final int Fi = 113;
	@CType(name = "FL")
	public static final long Fl = 114;
	public static long l = 115; // not final - ignored
	public static final double Fd = 0.116; // double - ignored
	@CType(os = mac)
	public static final int Flm = 117;
	@CType(os = linux)
	public static final int Fll = 117;

	public enum E {
		a(OsUtil.os().mac ? 0 : 1),
		@CType(name = "B")
		b(2),
		@CUndefined
		c(4);

		public final int value;

		private E(int value) {
			this.value = value;
		}
	}

	@SuppressWarnings("serial")
	@CType(name = "IB")
	public static class Ib extends IntType<Ib> {
		public Ib() {
			super(1, 0, true);
		}
	}

	@SuppressWarnings("serial")
	public static class Ii extends IntType<Ii> {
		public Ii(int i) {
			super(4, i, true);
		}
	}

	@SuppressWarnings("serial")
	public static class Il extends IntType<Il> {
		public Il(long l) {
			super(8, l, false);
		}
	}

	// abstract - ignored
	@SuppressWarnings("serial")
	public static abstract class Ia extends IntType<Ia> {
		public Ia() {
			super(2, 0, false);
		}
	}

	@Fields("i")
	public static class S extends Struct {
		public int i;

		public S(Pointer p) {
			super(p);
		}
	}

	@Fields("i")
	public static class U extends Union {
		public int i;
	}

	@Fields("i")
	@CType(name = "ST", attrs = Attr.typedef)
	public static class St extends Struct {
		public int i;
	}

	// abstract - ignored
	public static abstract class Sa extends Struct {
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
