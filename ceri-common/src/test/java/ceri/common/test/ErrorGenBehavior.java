package ceri.common.test;

import static ceri.common.test.TestUtil.assertThrown;
import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Function;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.test.ErrorGen.Mode;

public class ErrorGenBehavior {
	private static final Function<String, SQLException> sqlFn = SQLException::new;

	@Test
	public void shouldGenerateNoException() throws InterruptedException, IOException {
		var eg = ErrorGen.of();
		eg.generateRt();
		eg.generateWithInterrupt();
		eg.generateIo();
		eg.generateIoWithInterrupt();
		eg.generate(IOException::new);
		eg.generateWithInterrupt(IOException::new);
	}

	@Test
	public void shouldReset() {
		var eg = ErrorGen.of().mode(Mode.rt);
		assertThrown(RuntimeException.class, eg::generateRt);
		eg.reset();
		eg.generateRt();
	}

	@Test
	public void shouldGenerateRuntimeException() {
		var eg = ErrorGen.of().mode(Mode.rt);
		assertThrown(RuntimeException.class, eg::generateRt);
		assertThrown(RuntimeException.class, eg::generateWithInterrupt);
		assertThrown(RuntimeException.class, eg::generateIo);
		assertThrown(RuntimeException.class, eg::generateIoWithInterrupt);
		assertThrown(RuntimeException.class, () -> eg.generate(sqlFn));
		assertThrown(RuntimeException.class, () -> eg.generateWithInterrupt(sqlFn));
	}

	@Test
	public void shouldGenerateRuntimeInterruptedException() {
		var eg = ErrorGen.of().mode(Mode.rtInterrupted);
		assertThrown(RuntimeInterruptedException.class, eg::generateRt);
		assertThrown(RuntimeInterruptedException.class, eg::generateWithInterrupt);
		assertThrown(RuntimeInterruptedException.class, eg::generateIo);
		assertThrown(RuntimeInterruptedException.class, eg::generateIoWithInterrupt);
		assertThrown(RuntimeInterruptedException.class, () -> eg.generate(sqlFn));
		assertThrown(RuntimeInterruptedException.class, () -> eg.generateWithInterrupt(sqlFn));
	}

	@Test
	public void shouldGenerateInterruptedException() {
		var eg = ErrorGen.of().mode(Mode.interrupted);
		assertThrown(RuntimeException.class, eg::generateRt); // catch-all
		assertThrown(InterruptedException.class, eg::generateWithInterrupt);
		assertThrown(RuntimeException.class, eg::generateIo);
		assertThrown(InterruptedException.class, eg::generateIoWithInterrupt);
		assertThrown(RuntimeException.class, () -> eg.generate(sqlFn));
		assertThrown(InterruptedException.class, () -> eg.generateWithInterrupt(sqlFn));
	}

	@Test
	public void shouldGenerateCheckedException() {
		var eg = ErrorGen.of().mode(Mode.checked);
		assertThrown(RuntimeException.class, eg::generateRt);
		assertThrown(RuntimeException.class, eg::generateWithInterrupt);
		assertThrown(IOException.class, eg::generateIo);
		assertThrown(IOException.class, eg::generateIoWithInterrupt);
		assertThrown(SQLException.class, () -> eg.generate(sqlFn));
		assertThrown(SQLException.class, () -> eg.generateWithInterrupt(sqlFn));
	}

}
