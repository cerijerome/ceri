package ceri.jna.util;

import org.junit.Test;
import com.sun.jna.LastErrorException;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.jna.clib.jna.CException;
import ceri.jna.test.JnaAssert;

public class CallerBehavior {
	private static final Caller<CException> caller = Caller.of(CException::full);

	@Test
	public void shouldCaptureErrorCode() {
		Assert.equal(Caller.capture(() -> {}), 0);
		Assert.equal(Caller.capture(() -> Assert.throwIt(new LastErrorException(33))), 33);
	}

	@Test
	public void shouldCallAndWrapLastException() {
		JnaAssert.cexception(() -> caller.call(this::voidError, "test"));
	}

	@Test
	public void shouldCallInt() throws CException {
		Assert.equal(caller.callInt(() -> 0, "test"), 0);
		Assert.equal(caller.callInt(() -> 1, "test"), 1);
		Assert.equal(caller.callInt(() -> -1, "test"), -1);
		JnaAssert.cexception(() -> caller.callInt(this::intError, "test"));
	}

	@Test
	public void shouldCallType() throws CException {
		Assert.equal(caller.callType(() -> "x", "test"), "x");
		Assert.equal(caller.callType(() -> null, "test"), null);
		JnaAssert.cexception(() -> caller.callType(this::typeError, "test"));
	}

	@Test
	public void shouldVerifyZeroResultValue() throws CException {
		caller.verify(0, "test");
		JnaAssert.cexception(() -> caller.verify(1, "test"));
		JnaAssert.cexception(() -> caller.verify(-1, "test"));
	}

	@Test
	public void shouldVerifyIntResultValue() throws CException {
		caller.verifyInt(0, "test");
		caller.verifyInt(1, "test");
		JnaAssert.cexception(() -> caller.verifyInt(-1, "test"));
	}

	@Test
	public void shouldVerifyZeroResult() throws CException {
		caller.verify(() -> 0, "test");
		JnaAssert.cexception(() -> caller.verify(() -> 1, "test"));
		JnaAssert.cexception(() -> caller.verify(() -> -1, "test"));
	}

	@Test
	public void shouldVerifyIntResult() throws CException {
		caller.verifyInt(() -> 0, "test");
		caller.verifyInt(() -> 1, "test");
		JnaAssert.cexception(() -> caller.verifyInt(() -> -1, "test"));
	}

	@Test
	public void shouldVerifyIntResultWithPredicate() throws CException {
		Functions.IntPredicate verifyFn = i -> Math.abs(i) <= 1;
		caller.verifyInt(() -> 0, verifyFn, "test");
		caller.verifyInt(() -> -1, verifyFn, "test");
		JnaAssert.cexception(() -> caller.verifyInt(() -> 2, verifyFn, "test"));
	}

	@Test
	public void shouldVerifyTypeResult() throws CException {
		caller.verifyType(() -> "x", -1, "test");
		Assert.equal(CException.capture(() -> caller.verifyType(() -> null, -2, "test")), -2);
	}

	@Test
	public void shouldVerifyTypeResultWithPredicate() throws CException {
		Functions.ToIntFunction<String> verifyFn = String::length;
		caller.verifyType(() -> "", verifyFn, "test");
		Assert.equal(CException.capture(() -> caller.verifyType(() -> "abc", verifyFn, "test")), 3);
	}

	private void voidError() {
		throw new LastErrorException(-1);
	}

	private <T> T typeError() {
		throw new LastErrorException(-1);
	}

	private int intError() {
		throw new LastErrorException(-1);
	}
}
