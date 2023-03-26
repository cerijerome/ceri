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
			CException.intercept(() -> throwIt(CException.of(CError.EACCES, "test")), captor);
			fail();
		} catch (CException e) {
			captor.verifyInt(CError.EACCES.code);
		}
	}

	@Test
	public void testCapture() {
		assertEquals(CException.capture(() -> {}), 0);
		assertEquals(CException.capture(() -> throwIt(CException.of(333, "test"))), 333);
	}

	@Test
	public void shouldCreateFromError() {
		assertEquals(CException.full("test", null).getMessage(), "test: -1");
		assertEquals(CException.full("test", CError.E2BIG).getMessage(), "test: 7 (E2BIG)");
	}

	@Test
	public void shouldConvertToRuntimeException() {
		try {
			throw CException.full("test", CError.EACCES).runtime();
		} catch (RuntimeException e) {
			assertThrowable(e, "test: 13 (EACCES)");
		}
	}

}
