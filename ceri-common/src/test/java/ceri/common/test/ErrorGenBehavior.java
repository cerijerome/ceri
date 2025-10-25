package ceri.common.test;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertString;
import static ceri.common.test.Assert.io;
import static ceri.common.test.Assert.runtime;
import static ceri.common.test.Assert.throwable;
import static ceri.common.test.ErrorGen.INX;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RIX;
import static ceri.common.test.ErrorGen.RTX;
import static ceri.common.test.TestUtil.thrown;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.except.ExceptionAdapter;

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
		Assert.thrown(err::call);
		err.set();
		err.call();
	}

	@Test
	public void shouldClearFromErrors() {
		var err = ErrorGen.of();
		err.setFrom(IOX);
		Assert.thrown(err::call);
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
		Assert.thrown(RuntimeInterruptedException.class, err::call);
		runtime(err::call);
		runtime(err::call);
	}

	@Test
	public void shouldConvertToTypedException() throws IOException {
		var err = ErrorGen.of();
		err.call(ExceptionAdapter.io);
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(() -> err.call(ExceptionAdapter.io)), rtx);
		assertEquals(thrown(() -> err.call(ExceptionAdapter.io)), rix);
		Assert.thrown(RuntimeInterruptedException.class, () -> err.call(ExceptionAdapter.io));
		assertEquals(thrown(() -> err.call(ExceptionAdapter.io)), iox);
		io(() -> err.call(ExceptionAdapter.io));
	}

	@Test
	public void shouldConvertToRuntimeWithInterruptedException() throws InterruptedException {
		var err = ErrorGen.of();
		err.callWithInterrupt();
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(err::callWithInterrupt), rtx);
		assertEquals(thrown(err::callWithInterrupt), rix);
		Assert.thrown(InterruptedException.class, err::callWithInterrupt);
		runtime(err::callWithInterrupt);
		runtime(err::callWithInterrupt);
	}

	@Test
	public void shouldConvertToTypedWithInterruptedException()
		throws InterruptedException, IOException {
		var err = ErrorGen.of();
		err.callWithInterrupt(ExceptionAdapter.io);
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(() -> err.callWithInterrupt(ExceptionAdapter.io)), rtx);
		assertEquals(thrown(() -> err.callWithInterrupt(ExceptionAdapter.io)), rix);
		Assert.thrown(InterruptedException.class, () -> err.callWithInterrupt(ExceptionAdapter.io));
		assertEquals(thrown(() -> err.callWithInterrupt(ExceptionAdapter.io)), iox);
		io(() -> err.callWithInterrupt(ExceptionAdapter.io));
	}

	@Test
	public void shouldSetExceptionFunctionFromMessage() {
		var err = ErrorGen.of();
		err.setFrom(IOException::new, SQLException::new);
		io(() -> err.callWithInterrupt(ExceptionAdapter.io));
		Throwable t = thrown(() -> err.callWithInterrupt(ExceptionAdapter.io));
		throwable(t.getCause(), SQLException.class);
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
