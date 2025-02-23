package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.util.CloseableUtil;
import ceri.jna.clib.ErrNo;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.util.JnaLibrary;

public class CUtilTest {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();

	@After
	public void after() {
		CloseableUtil.close(ref);
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CUtil.class);
	}

	@Test
	public void testRequireContiguous() throws IOException {
		CUtil.requireContiguous(new CPoll.pollfd[0]);
		CUtil.requireContiguous(CPoll.pollfd.array(0));
		CUtil.requireContiguous(CPoll.pollfd.array(2));
		assertThrown(CException.class, () -> CUtil
			.requireContiguous(new CPoll.pollfd[] { new CPoll.pollfd(), new CPoll.pollfd() }));
	}

	@Test
	public void testTty() {
		var lib = ref.init();
		lib.isatty.autoResponses(0, 1);
		assertEquals(CUtil.tty(), false);
		assertEquals(CUtil.tty(), true);
		lib.isatty.error.setFrom(ErrNo.EBADFD::lastError);
		assertEquals(CUtil.tty(), false);
	}
}
