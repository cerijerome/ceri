package ceri.common.test;

import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Function;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.test.ErrorGen.Mode;

public class ErrorGenBehavior {
	private static final Function<String, SQLException> sqlFn = SQLException::new;

	@Test
	public void shouldGenerateNoException() throws Exception {
		var eg = ErrorGen.of();
		eg.generate();
		eg.generateWithInterrupt();
		eg.generateIo();
		eg.generateIoWithInterrupt();
		eg.generate(sqlFn);
		eg.generateWithInterrupt(sqlFn);
	}

	@Test
	public void shouldTreatNullModeAsNone() throws Exception {
		var eg = ErrorGen.of().mode(() -> null);
		eg.generate();
		eg.generateWithInterrupt();
		eg.generateIo();
		eg.generateIoWithInterrupt();
		eg.generate(sqlFn);
		eg.generateWithInterrupt(sqlFn);
	}

	@Test
	public void shouldSetModes() {
		var eg = ErrorGen.of().modes(Mode.checked, Mode.none, Mode.rt, Mode.checked);
		assertThrown(() -> eg.generate());
		eg.generate();
		assertThrown(() -> eg.generate());
		assertThrown(() -> eg.generate());
		eg.generate();
	}

	@Test
	public void shouldReset() {
		var eg = ErrorGen.of().mode(Mode.rt);
		assertThrown(RuntimeException.class, eg::generate);
		eg.reset();
		eg.generate();
	}

	@Test
	public void shouldGenerateRuntimeException() {
		var eg = ErrorGen.of().mode(Mode.rt);
		assertThrown(RuntimeException.class, eg::generate);
		assertThrown(RuntimeException.class, eg::generateWithInterrupt);
		assertThrown(RuntimeException.class, eg::generateIo);
		assertThrown(RuntimeException.class, eg::generateIoWithInterrupt);
		assertThrown(RuntimeException.class, () -> eg.generate(sqlFn));
		assertThrown(RuntimeException.class, () -> eg.generateWithInterrupt(sqlFn));
	}

	@Test
	public void shouldGenerateRuntimeInterruptedException() {
		var eg = ErrorGen.of().mode(Mode.rtInterrupted);
		assertThrown(RuntimeInterruptedException.class, eg::generate);
		assertThrown(RuntimeInterruptedException.class, eg::generateWithInterrupt);
		assertThrown(RuntimeInterruptedException.class, eg::generateIo);
		assertThrown(RuntimeInterruptedException.class, eg::generateIoWithInterrupt);
		assertThrown(RuntimeInterruptedException.class, () -> eg.generate(sqlFn));
		assertThrown(RuntimeInterruptedException.class, () -> eg.generateWithInterrupt(sqlFn));
	}

	@Test
	public void shouldGenerateInterruptedException() {
		var eg = ErrorGen.of().mode(Mode.interrupted);
		assertThrown(RuntimeException.class, eg::generate); // catch-all
		assertThrown(InterruptedException.class, eg::generateWithInterrupt);
		assertThrown(RuntimeException.class, eg::generateIo);
		assertThrown(InterruptedException.class, eg::generateIoWithInterrupt);
		assertThrown(RuntimeException.class, () -> eg.generate(sqlFn));
		assertThrown(InterruptedException.class, () -> eg.generateWithInterrupt(sqlFn));
	}

	@Test
	public void shouldGenerateCheckedException() {
		var eg = ErrorGen.of().mode(Mode.checked);
		assertThrown(RuntimeException.class, eg::generate);
		assertThrown(RuntimeException.class, eg::generateWithInterrupt);
		assertThrown(IOException.class, eg::generateIo);
		assertThrown(IOException.class, eg::generateIoWithInterrupt);
		assertThrown(SQLException.class, () -> eg.generate(sqlFn));
		assertThrown(SQLException.class, () -> eg.generateWithInterrupt(sqlFn));
	}

}
