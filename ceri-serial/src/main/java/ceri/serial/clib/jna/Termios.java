package ceri.serial.clib.jna;

import java.util.Arrays;
import java.util.List;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.serial.jna.Struct;

public class Termios {

	private Termios() {}

	public static class Size {
		public static final int TCFLAG_T = SizeOf.LONG;
		public static final int CC_T = SizeOf.CHAR;
		public static final int SPEED_T = SizeOf.LONG;
		public static final int TERMIOS = SizeOf.size(termios.class);

		private Size() {}
	}

	public static class termios extends Struct {
		private static final List<String> FIELDS = List.of( //
			"c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc", "c_ispeed", "c_ospeed");
		private static final int NCCS = 20;

		public static class ByReference extends termios implements Structure.ByReference {}

		public static class ByValue extends termios implements Structure.ByValue {}

		public NativeLong c_iflag; // input modes
		public NativeLong c_oflag; // output modes
		public NativeLong c_cflag; // control modes
		public NativeLong c_lflag; // local modes
		public byte[] c_cc = new byte[NCCS]; // special characters
		public NativeLong c_ispeed; // input speed
		public NativeLong c_ospeed; // output speed

		public static ByReference[] array(int count) {
			return Struct.<ByReference>array(count, ByReference::new, ByReference[]::new);
		}

		@Override
		public int hashCode() {
			return HashCoder.hash(c_iflag, c_oflag, c_cflag, c_lflag, c_cc, c_ispeed, c_ospeed);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof termios)) return false;
			termios other = (termios) obj;
			if (!EqualsUtil.equals(c_iflag, other.c_iflag)) return false;
			if (!EqualsUtil.equals(c_oflag, other.c_oflag)) return false;
			if (!EqualsUtil.equals(c_cflag, other.c_cflag)) return false;
			if (!EqualsUtil.equals(c_lflag, other.c_lflag)) return false;
			if (!Arrays.equals(c_cc, other.c_cc)) return false;
			if (!EqualsUtil.equals(c_ispeed, other.c_ispeed)) return false;
			if (!EqualsUtil.equals(c_ospeed, other.c_ospeed)) return false;
			return true;
		}

		@Override
		public String toString() {
			return ToStringHelper
				.createByClass(this, c_iflag, c_oflag, c_cflag, c_lflag, c_cc, c_ispeed, c_ospeed)
				.toString();
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

}
