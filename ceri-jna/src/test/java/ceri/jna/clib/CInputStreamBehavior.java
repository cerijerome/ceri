package ceri.jna.clib;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.CallSync;
import ceri.common.util.Enclosed;
import ceri.jna.clib.test.TestCLibNative;

public class CInputStreamBehavior {
	private static TestCLibNative lib;
	private static Enclosed<RuntimeException, ?> enc;
	private static int fd;
	private CInputStream in;

	@BeforeClass
	public static void beforeClass() {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = lib.open("test", 0);
	}

	@AfterClass
	public static void afterClass() {
		lib.close(fd);
		enc.close();
	}

	@Before
	public void before() {
		lib.write.autoResponses(0).reset();
		in = CInputStream.of(fd);
		in.bufferSize(3);
	}

	@After
	public void after() {
		in.close();
	}

	@Test
	public void shouldFailIfClosed() {
		lib.write.autoResponses(1);
		in.close();
		assertThrown(() -> in.read());
		assertThrown(() -> in.read(new byte[3]));
		assertThrown(() -> in.available());
	}

}
