package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaUtil;

public class CTermiosTest {

	@BeforeClass
	public static void beforeClass() {
		// ceri.jna.test.JnaTestUtil.testAsLinux();
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CTermios.class);
	}

	@Test
	public void testTcsetattr() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			var termios = new CTermios.Mac.termios();
			CTermios.tcsetattr(fd, 1, termios);
			lib.tcsetattr.assertAuto(List.of(lib.fd(fd), 1, termios.getPointer()));
		});
	}

	@Test
	public void testTcgetattr() throws CException {
		TestCLibNative.exec(lib -> {
			int fd = CFcntl.open("test", 0);
			lib.tcgetattr.autoResponse(list -> {
				Pointer p = (Pointer) list.get(1);
				p.setNativeLong(0, JnaUtil.unlong(0x1234));
				return 0;
			});
			var t = CTermios.Linux.tcgetattr(fd);
			assertEquals(t.c_iflag, JnaUtil.unlong(0x1234));
		});
	}

	@Test
	public void testOsCoverage() {
		JnaTestUtil.testForEachOs(CTermios.class);
	}
}
