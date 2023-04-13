package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.FileTestHelper;

public class CFcntlTest {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createFiles() throws IOException {
		helper = FileTestHelper.builder().file("file1", "test").build();
	}

	@AfterClass
	public static void deleteFiles() {
		helper.close();
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CFcntl.class);
	}

	@Test
	public void testFailToOpenWithParameters() {
		assertThrown(() -> CFcntl.open(helper.path("").toString(), 3));
		assertThrown(() -> CFcntl.open(helper.path("").toString(), 3, 0666));
	}

	@Test
	public void testValidFd() throws CException {
		assertEquals(CFcntl.validFd(-1), false);
		assertEquals(CFcntl.validFd(0), true);
		assertEquals(CFcntl.validFd(1), true);
		assertThrown(() -> CFcntl.validateFd(-1));
		assertEquals(CFcntl.validateFd(0), 0);
		assertEquals(CFcntl.validateFd(1), 1);
	}

}
