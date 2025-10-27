package ceri.common.test;

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
		err.setFrom(ErrorGen.IOX);
		Assert.thrown(err::call);
		err.setFrom();
		err.call();
	}

	@Test
	public void shouldConvertToRuntimeException() {
		var err = ErrorGen.of();
		err.call();
		err.set(rtx, rix, inx, iox, sqx);
		Assert.equal(TestUtil.thrown(err::call), rtx);
		Assert.equal(TestUtil.thrown(err::call), rix);
		Assert.thrown(RuntimeInterruptedException.class, err::call);
		Assert.runtime(err::call);
		Assert.runtime(err::call);
	}

	@Test
	public void shouldConvertToTypedException() throws IOException {
		var err = ErrorGen.of();
		err.call(ExceptionAdapter.io);
		err.set(rtx, rix, inx, iox, sqx);
		Assert.equal(TestUtil.thrown(() -> err.call(ExceptionAdapter.io)), rtx);
		Assert.equal(TestUtil.thrown(() -> err.call(ExceptionAdapter.io)), rix);
		Assert.thrown(RuntimeInterruptedException.class, () -> err.call(ExceptionAdapter.io));
		Assert.equal(TestUtil.thrown(() -> err.call(ExceptionAdapter.io)), iox);
		Assert.io(() -> err.call(ExceptionAdapter.io));
	}

	@Test
	public void shouldConvertToRuntimeWithInterruptedException() throws InterruptedException {
		var err = ErrorGen.of();
		err.callWithInterrupt();
		err.set(rtx, rix, inx, iox, sqx);
		Assert.equal(TestUtil.thrown(err::callWithInterrupt), rtx);
		Assert.equal(TestUtil.thrown(err::callWithInterrupt), rix);
		Assert.thrown(InterruptedException.class, err::callWithInterrupt);
		Assert.runtime(err::callWithInterrupt);
		Assert.runtime(err::callWithInterrupt);
	}

	@Test
	public void shouldConvertToTypedWithInterruptedException()
		throws InterruptedException, IOException {
		var err = ErrorGen.of();
		err.callWithInterrupt(ExceptionAdapter.io);
		err.set(rtx, rix, inx, iox, sqx);
		Assert.equal(TestUtil.thrown(() -> err.callWithInterrupt(ExceptionAdapter.io)), rtx);
		Assert.equal(TestUtil.thrown(() -> err.callWithInterrupt(ExceptionAdapter.io)), rix);
		Assert.thrown(InterruptedException.class, () -> err.callWithInterrupt(ExceptionAdapter.io));
		Assert.equal(TestUtil.thrown(() -> err.callWithInterrupt(ExceptionAdapter.io)), iox);
		Assert.io(() -> err.callWithInterrupt(ExceptionAdapter.io));
	}

	@Test
	public void shouldSetExceptionFunctionFromMessage() {
		var err = ErrorGen.of();
		err.setFrom(IOException::new, SQLException::new);
		Assert.io(() -> err.callWithInterrupt(ExceptionAdapter.io));
		var t = TestUtil.thrown(() -> err.callWithInterrupt(ExceptionAdapter.io));
		Assert.throwable(t.getCause(), SQLException.class);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		var err = ErrorGen.of();
		Assert.string(err, "none");
		err.set(iox);
		Assert.string(err, "[IO]");
		err.set(iox, sqx, iox, null, rix);
		Assert.string(err, "[IO,SQL,IO,null,RuntimeInterrupted]");
		err.setFrom(ErrorGen.IOX, ErrorGen.RIX, ErrorGen.RTX, ErrorGen.INX, null,
			SQLException::new);
		Assert.string(err, "[IOX,RIX,RTX,INX,null,\u03bb]");
	}
}
