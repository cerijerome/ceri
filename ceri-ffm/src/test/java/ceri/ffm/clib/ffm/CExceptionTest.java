package ceri.ffm.clib.ffm;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.ffm.core.LastError;

public class CExceptionTest {

	@Test
	public void testAdapter() {
		Assert.thrown(CException.class, "throwIo",
			() -> CException.ADAPTER.run(() -> Assert.throwIo()));
		Assert.thrown(CException.class, "Error",
			() -> CException.ADAPTER.run(() -> Assert.throwIt(new Exception((String) null))));
	}

	@Test
	public void testRuntimeException() {
		Assert.throwable(CErrNo.EINVAL.error().runtime(), CException.Runtime.class,
			e -> Assert.equal(e.code, CErrNo.EINVAL.code));
	}

	@Test
	public void testIntercept() throws CException {
		var captor = Captor.ofInt();
		CException.intercept(() -> {}, captor);
		Assert.thrown(
			() -> CException.intercept(() -> Assert.throwIt(CErrNo.EINVAL.error()), captor));
		captor.verifyInt(0, CErrNo.EINVAL.code);
	}

	@Test
	public void testCapture() {
		Assert.equal(CException.capture(() -> {}), 0);
		Assert.equal(CException.capture(() -> Assert.throwIt(CErrNo.EINVAL.error())),
			CErrNo.EINVAL.code);
	}

	@Test
	public void testLastError() throws CException {
		LastError.set(0);
		CException.lastError();
		Assert.thrown(CException.class, e -> Assert.equal(e.code, CErrNo.EAGAIN.code), () -> {
			LastError.set(CErrNo.EAGAIN.code);
			CException.lastError();
		});
	}

	@Test
	public void testGeneralError() {
		Assert.throwable(CException.general("%s", "test"), CException.class, e -> {
			Assert.equal(e.code, CException.GENERAL_ERROR_CODE);
			Assert.match(e.getMessage(), "test");
		});
	}

	@Test
	public void testFullMessage() {
		Assert.find(CException.full(-999, null).getMessage(), "\\[-999\\]");
		Assert.find(CException.full(-999, "test").getMessage(), "\\[-999\\] test");
		Assert.find(CException.full(1, "test").getMessage(), "\\[1] EPERM .*; test");
	}

}
