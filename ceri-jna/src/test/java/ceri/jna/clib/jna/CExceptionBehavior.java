package ceri.jna.clib.jna;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
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
			CException.intercept(() -> Assert.throwIt(CException.of(CErrNo.EACCES, "test")),
				captor);
			Assert.fail();
		} catch (CException e) {
			captor.verifyInt(CErrNo.EACCES);
		}
	}

	@Test
	public void testCapture() {
		Assert.equal(CException.capture(() -> {}), 0);
		Assert.equal(CException.capture(() -> Assert.throwIt(CException.of(333, "test"))), 333);
	}

	@Test
	public void shouldCreateFromError() {
		Assert.throwable(CException.full(-1, "test"), "\\Q[-1] test\\E");
		Assert.throwable(CException.full(CErrNo.E2BIG, "test"), "\\Q[%d] E2BIG test\\E",
			CErrNo.E2BIG);
	}

	@Test
	public void shouldConvertToRuntimeException() {
		try {
			throw CException.full(CErrNo.EACCES, "test").runtime();
		} catch (RuntimeException e) {
			Assert.throwable(e, "\\Q[%d] EACCES test\\E", CErrNo.EACCES);
		}
	}

	@Test
	public void shouldAdaptError() {
		var ioe = new IOException("io");
		var ce = CException.ADAPTER.apply(ioe);
		Assert.equal(ce.getMessage(), "io");
		Assert.equal(ce.getCause(), ioe);
		Assert.equal(ce.code, CException.GENERAL_ERROR_CODE);
	}

	@Test
	public void shouldAdaptErrorWithoutMessage() {
		var ioe = new IOException();
		Assert.equal(CException.ADAPTER.apply(ioe).getMessage(), "Error");
	}
}
