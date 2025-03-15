package ceri.common.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
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
		err.call(IO_ADAPTER);
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(() -> err.call(IO_ADAPTER)), rtx);
		assertEquals(thrown(() -> err.call(IO_ADAPTER)), rix);
		assertThrown(RuntimeInterruptedException.class, () -> err.call(IO_ADAPTER));
		assertEquals(thrown(() -> err.call(IO_ADAPTER)), iox);
		assertIoe(() -> err.call(IO_ADAPTER));
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
		err.callWithInterrupt(IO_ADAPTER);
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(() -> err.callWithInterrupt(IO_ADAPTER)), rtx);
		assertEquals(thrown(() -> err.callWithInterrupt(IO_ADAPTER)), rix);
		assertThrown(InterruptedException.class, () -> err.callWithInterrupt(IO_ADAPTER));
		assertEquals(thrown(() -> err.callWithInterrupt(IO_ADAPTER)), iox);
		assertIoe(() -> err.callWithInterrupt(IO_ADAPTER));
	}

	@Test
	public void shouldSetExceptionFunctionFromMessage() {
		var err = ErrorGen.of();
		err.setFrom(IOException::new, SQLException::new);
		assertIoe(() -> err.callWithInterrupt(IO_ADAPTER));
		Throwable t = thrown(() -> err.callWithInterrupt(IO_ADAPTER));
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
