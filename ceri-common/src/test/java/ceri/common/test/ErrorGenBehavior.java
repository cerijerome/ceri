package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.INX;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RIX;
import static ceri.common.test.ErrorGen.RTX;
import static ceri.common.test.TestUtil.thrown;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.exception.ExceptionAdapter;

public class ErrorGenBehavior {
	private final Exception rtx = new RuntimeException("test");
	private final Exception rix = new RuntimeInterruptedException("test");
	private final Exception inx = new InterruptedException("test");
	private final Exception iox = new IOException("test");
	private final Exception sqx = new SQLException("test");

	@Test
	public void shouldClearErrors() {
		var err = ErrorGen.of();
		err.set(new IOException());
		assertThrown(err::call);
		err.set();
		err.call();
	}

	@Test
	public void shouldClearFromErrors() {
		var err = ErrorGen.of();
		err.setFrom(IOX);
		assertThrown(err::call);
		err.setFrom();
		err.call();
	}

	@Test
	public void shouldConvertToRuntimeException() {
		var err = ErrorGen.of();
		err.call();
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(err::call), rtx);
		assertEquals(thrown(err::call), rix);
		assertThrown(RuntimeInterruptedException.class, err::call);
		assertRte(err::call);
		assertRte(err::call);
	}

	@Test
	public void shouldConvertToTypedException() throws IOException {
		var err = ErrorGen.of();
		err.call(ExceptionAdapter.io);
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(() -> err.call(ExceptionAdapter.io)), rtx);
		assertEquals(thrown(() -> err.call(ExceptionAdapter.io)), rix);
		assertThrown(RuntimeInterruptedException.class, () -> err.call(ExceptionAdapter.io));
		assertEquals(thrown(() -> err.call(ExceptionAdapter.io)), iox);
		assertIoe(() -> err.call(ExceptionAdapter.io));
	}

	@Test
	public void shouldConvertToRuntimeWithInterruptedException() throws InterruptedException {
		var err = ErrorGen.of();
		err.callWithInterrupt();
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(err::callWithInterrupt), rtx);
		assertEquals(thrown(err::callWithInterrupt), rix);
		assertThrown(InterruptedException.class, err::callWithInterrupt);
		assertRte(err::callWithInterrupt);
		assertRte(err::callWithInterrupt);
	}

	@Test
	public void shouldConvertToTypedWithInterruptedException()
		throws InterruptedException, IOException {
		var err = ErrorGen.of();
		err.callWithInterrupt(ExceptionAdapter.io);
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(() -> err.callWithInterrupt(ExceptionAdapter.io)), rtx);
		assertEquals(thrown(() -> err.callWithInterrupt(ExceptionAdapter.io)), rix);
		assertThrown(InterruptedException.class, () -> err.callWithInterrupt(ExceptionAdapter.io));
		assertEquals(thrown(() -> err.callWithInterrupt(ExceptionAdapter.io)), iox);
		assertIoe(() -> err.callWithInterrupt(ExceptionAdapter.io));
	}

	@Test
	public void shouldSetExceptionFunctionFromMessage() {
		var err = ErrorGen.of();
		err.setFrom(IOException::new, SQLException::new);
		assertIoe(() -> err.callWithInterrupt(ExceptionAdapter.io));
		Throwable t = thrown(() -> err.callWithInterrupt(ExceptionAdapter.io));
		assertThrowable(t.getCause(), SQLException.class);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		var err = ErrorGen.of();
		assertString(err, "none");
		err.set(iox);
		assertString(err, "[IO]");
		err.set(iox, sqx, iox, null, rix);
		assertString(err, "[IO,SQL,IO,null,RuntimeInterrupted]");
		err.setFrom(IOX, RIX, RTX, INX, null, SQLException::new);
		assertString(err, "[IOX,RIX,RTX,INX,null,\u03bb]");
	}

}
