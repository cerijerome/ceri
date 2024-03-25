package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.fail;
import static ceri.common.test.AssertUtil.throwIt;
import org.junit.Test;
import ceri.common.test.Captor;

public class CExceptionBehavior {

	@Test
	public void testInterceptWithoutError() throws CException {
		var captor = Captor.ofInt();
		CException.intercept(() -> {}, captor);
		captor.verifyInt(0);
	}

	@Test
	public void testInterceptWithError() {
		var captor = Captor.ofInt();
		try {
			CException.intercept(() -> throwIt(CException.of(CErrNo.EACCES, "test")), captor);
			fail();
		} catch (CException e) {
			captor.verifyInt(CErrNo.EACCES);
		}
	}

	@Test
	public void testCapture() {
		assertEquals(CException.capture(() -> {}), 0);
		assertEquals(CException.capture(() -> throwIt(CException.of(333, "test"))), 333);
	}

	@Test
	public void shouldCreateFromError() {
		assertThrowable(CException.full(-1, "test"), "\\Q[-1] test\\E");
		assertThrowable(CException.full(CErrNo.E2BIG, "test"), "\\Q[%d] test\\E", CErrNo.E2BIG);
	}

	@Test
	public void shouldConvertToRuntimeException() {
		try {
			throw CException.full(CErrNo.EACCES, "test").runtime();
		} catch (RuntimeException e) {
			assertThrowable(e, "\\Q[%d] test\\E", CErrNo.EACCES);
		}
	}

}
