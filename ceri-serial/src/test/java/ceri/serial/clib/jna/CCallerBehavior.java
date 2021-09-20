package ceri.serial.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.util.function.IntPredicate;
import java.util.function.ToIntFunction;
import org.junit.Test;
import com.sun.jna.LastErrorException;

public class CCallerBehavior {
	private static final CCaller<CException> caller = CCaller.of();

	@Test
	public void shouldCallAndWrapLastException() {
		assertThrown(CException.class, () -> caller.call(this::voidError, "test"));
	}

	@Test
	public void shouldCallInt() throws CException {
		assertEquals(caller.callInt(() -> 0, "test"), 0);
		assertEquals(caller.callInt(() -> 1, "test"), 1);
		assertEquals(caller.callInt(() -> -1, "test"), -1);
		assertThrown(CException.class, () -> caller.callInt(this::intError, "test"));
	}

	@Test
	public void shouldCallType() throws CException {
		assertEquals(caller.callType(() -> "x", "test"), "x");
		assertEquals(caller.callType(() -> null, "test"), null);
		assertThrown(CException.class, () -> caller.callType(this::typeError, "test"));
	}

	@Test
	public void shouldVerifyZeroResultValue() throws CException {
		caller.verify(0, "test");
		assertThrown(CException.class, () -> caller.verify(1, "test"));
		assertThrown(CException.class, () -> caller.verify(-1, "test"));
	}

	@Test
	public void shouldVerifyIntResultValue() throws CException {
		caller.verifyInt(0, "test");
		caller.verifyInt(1, "test");
		assertThrown(CException.class, () -> caller.verifyInt(-1, "test"));
	}

	@Test
	public void shouldVerifyZeroResult() throws CException {
		caller.verify(() -> 0, "test");
		assertThrown(CException.class, () -> caller.verify(() -> 1, "test"));
		assertThrown(CException.class, () -> caller.verify(() -> -1, "test"));
	}

	@Test
	public void shouldVerifyIntResult() throws CException {
		caller.verifyInt(() -> 0, "test");
		caller.verifyInt(() -> 1, "test");
		assertThrown(CException.class, () -> caller.verifyInt(() -> -1, "test"));
	}

	@Test
	public void shouldVerifyIntResultWithPredicate() throws CException {
		IntPredicate verifyFn = i -> Math.abs(i) <= 1;
		caller.verifyInt(() -> 0, verifyFn, "test");
		caller.verifyInt(() -> -1, verifyFn, "test");
		assertThrown(CException.class, () -> caller.verifyInt(() -> 2, verifyFn, "test"));
	}

	@Test
	public void shouldVerifyTypeResult() throws CException {
		caller.verifyType(() -> "x", -1, "test");
		assertEquals(CException.capture(() -> caller.verifyType(() -> null, -2, "test")), -2);
	}

	@Test
	public void shouldVerifyTypeResultWithPredicate() throws CException {
		ToIntFunction<String> verifyFn = String::length;
		caller.verifyType(() -> "", verifyFn, "test");
		assertEquals(CException.capture(() -> caller.verifyType(() -> "abc", verifyFn, "test")), 3);
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
