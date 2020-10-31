package ceri.common.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorProducer.MESSAGE;
import static ceri.common.test.TestUtil.thrown;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;

public class ErrorProducerBehavior {
	private final Exception rtx = new RuntimeException("test");
	private final Exception rix = new RuntimeInterruptedException("test");
	private final Exception inx = new InterruptedException("test");
	private final Exception iox = new IOException("test");
	private final Exception sqx = new SQLException("test");

	@Test
	public void shouldConvertToRuntimeException() {
		var err = ErrorProducer.of();
		err.call();
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(err::call), rtx);
		assertEquals(thrown(err::call), rix);
		assertThrown(RuntimeInterruptedException.class, err::call);
		assertThrown(RuntimeException.class, err::call);
		assertThrown(RuntimeException.class, err::call);
	}

	@Test
	public void shouldConvertToTypedException() throws IOException {
		var err = ErrorProducer.of();
		err.call(IO_ADAPTER);
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(() -> err.call(IO_ADAPTER)), rtx);
		assertEquals(thrown(() -> err.call(IO_ADAPTER)), rix);
		assertThrown(RuntimeInterruptedException.class, () -> err.call(IO_ADAPTER));
		assertEquals(thrown(() -> err.call(IO_ADAPTER)), iox);
		assertThrown(IOException.class, () -> err.call(IO_ADAPTER));
	}

	@Test
	public void shouldConvertToRuntimeWithInterruptedException() throws InterruptedException {
		var err = ErrorProducer.of();
		err.callWithInterrupt();
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(err::callWithInterrupt), rtx);
		assertEquals(thrown(err::callWithInterrupt), rix);
		assertThrown(InterruptedException.class, err::callWithInterrupt);
		assertThrown(RuntimeException.class, err::callWithInterrupt);
		assertThrown(RuntimeException.class, err::callWithInterrupt);
	}

	@Test
	public void shouldConvertToTypedWithInterruptedException()
		throws InterruptedException, IOException {
		var err = ErrorProducer.of();
		err.callWithInterrupt(IO_ADAPTER);
		err.set(rtx, rix, inx, iox, sqx);
		assertEquals(thrown(() -> err.callWithInterrupt(IO_ADAPTER)), rtx);
		assertEquals(thrown(() -> err.callWithInterrupt(IO_ADAPTER)), rix);
		assertThrown(InterruptedException.class, () -> err.callWithInterrupt(IO_ADAPTER));
		assertEquals(thrown(() -> err.callWithInterrupt(IO_ADAPTER)), iox);
		assertThrown(IOException.class, () -> err.callWithInterrupt(IO_ADAPTER));
	}

	@Test
	public void shouldSetExceptionFunctionFromMessage() {
		var err = ErrorProducer.of();
		err.setFrom(IOException::new, SQLException::new);
		assertThrown(IOException.class, MESSAGE, () -> err.callWithInterrupt(IO_ADAPTER));
		Throwable t = thrown(() -> err.callWithInterrupt(IO_ADAPTER));
		assertThrowable(t.getCause(), SQLException.class, MESSAGE);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		var err = ErrorProducer.of();
		assertFind(err.toString(), "none");
		err.set(iox);
		assertFind(err.toString(), "IOException");
	}

}
