package ceri.common.except;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.test.Assert;

public class ExceptionAdapterBehavior {

	@Test
	public void shouldAdaptNoExceptionsIfNull() {
		Exception t = new FileNotFoundException("test");
		Assert.equal(ExceptionAdapter.none.apply(t), t);
		t = new IOException("test");
		Assert.equal(ExceptionAdapter.none.apply(t), t);
		t = new InterruptedException("test");
		Assert.equal(ExceptionAdapter.none.apply(t), t);
		t = new RuntimeException("test");
		Assert.equal(ExceptionAdapter.none.apply(t), t);
		t = new Exception("test");
		Assert.equal(ExceptionAdapter.none.apply(t), t);
	}

	@Test
	public void shouldNotThrow() {
		Assert.illegalState(() -> ExceptionAdapter.shouldNotThrow.get(() -> Assert.throwIo()));
	}

	@Test
	public void shouldThrowErrors() {
		Assert.assertion(() -> ExceptionAdapter.runtime.apply(new AssertionError()));
	}

	@Test
	public void shouldNotAdaptMatchingTypes() {
		var ad = ExceptionAdapter.of(IOException.class, IOException::new);
		Exception t = new FileNotFoundException("test");
		Assert.equal(ad.apply(t), t);
		t = new IOException("test");
		Assert.equal(ad.apply(t), t);
	}

	@Test
	public void shouldRun() {
		Assert.runtime(() -> ExceptionAdapter.runtime.run(() -> Assert.throwIo()));
		Assert.thrown(RuntimeInterruptedException.class,
			() -> ExceptionAdapter.runtime.run(() -> Assert.throwInterrupted()));
		Assert.runtime(() -> ExceptionAdapter.io.run(() -> Assert.throwRuntime()));
	}

	@Test
	public void shouldGet() {
		Assert.equal(ExceptionAdapter.runtime.get(() -> null), null);
		Assert.equal(ExceptionAdapter.runtime.get(() -> "test"), "test");
		Assert.runtime(() -> ExceptionAdapter.runtime.get(() -> Assert.throwIo()));
		Assert.thrown(RuntimeInterruptedException.class,
			() -> ExceptionAdapter.runtime.get(() -> Assert.throwInterrupted()));
		Assert.runtime(() -> ExceptionAdapter.io.get(() -> Assert.throwRuntime()));
	}

	@Test
	public void shouldGetBool() {
		Assert.no(ExceptionAdapter.runtime.getBool(() -> false));
		Assert.yes(ExceptionAdapter.runtime.getBool(() -> true));
		Assert.runtime(() -> ExceptionAdapter.runtime.getBool(() -> Assert.throwIo()));
		Assert.thrown(RuntimeInterruptedException.class,
			() -> ExceptionAdapter.runtime.getBool(() -> Assert.throwInterrupted()));
		Assert.runtime(() -> ExceptionAdapter.io.getBool(() -> Assert.throwRuntime()));
	}

	@Test
	public void shouldGetByte() {
		Assert.equal(ExceptionAdapter.runtime.getByte(() -> Byte.MIN_VALUE), Byte.MIN_VALUE);
		Assert.equal(ExceptionAdapter.runtime.getByte(() -> Byte.MAX_VALUE), Byte.MAX_VALUE);
		Assert.runtime(() -> ExceptionAdapter.runtime.getByte(() -> Assert.throwIo()));
		Assert.thrown(RuntimeInterruptedException.class,
			() -> ExceptionAdapter.runtime.getByte(() -> Assert.throwInterrupted()));
		Assert.runtime(() -> ExceptionAdapter.io.getByte(() -> Assert.throwRuntime()));
	}

	@Test
	public void shouldGetInt() {
		Assert.equal(ExceptionAdapter.runtime.getInt(() -> Integer.MIN_VALUE), Integer.MIN_VALUE);
		Assert.equal(ExceptionAdapter.runtime.getInt(() -> Integer.MAX_VALUE), Integer.MAX_VALUE);
		Assert.runtime(() -> ExceptionAdapter.runtime.getInt(() -> Assert.throwIo()));
		Assert.thrown(RuntimeInterruptedException.class,
			() -> ExceptionAdapter.runtime.getInt(() -> Assert.throwInterrupted()));
		Assert.runtime(() -> ExceptionAdapter.io.getInt(() -> Assert.throwRuntime()));
	}

	@Test
	public void shouldGetLong() {
		Assert.equal(ExceptionAdapter.runtime.getLong(() -> Long.MIN_VALUE), Long.MIN_VALUE);
		Assert.equal(ExceptionAdapter.runtime.getLong(() -> Long.MAX_VALUE), Long.MAX_VALUE);
		Assert.runtime(() -> ExceptionAdapter.runtime.getLong(() -> Assert.throwIo()));
		Assert.thrown(RuntimeInterruptedException.class,
			() -> ExceptionAdapter.runtime.getLong(() -> Assert.throwInterrupted()));
		Assert.runtime(() -> ExceptionAdapter.io.getLong(() -> Assert.throwRuntime()));
	}

	@Test
	public void shouldGetDouble() {
		Assert.equal(ExceptionAdapter.runtime.getDouble(() -> Double.MIN_VALUE), Double.MIN_VALUE);
		Assert.equal(ExceptionAdapter.runtime.getDouble(() -> Double.MAX_VALUE), Double.MAX_VALUE);
		Assert.runtime(() -> ExceptionAdapter.runtime.getDouble(() -> Assert.throwIo()));
		Assert.thrown(RuntimeInterruptedException.class,
			() -> ExceptionAdapter.runtime.getDouble(() -> Assert.throwInterrupted()));
		Assert.runtime(() -> ExceptionAdapter.io.getDouble(() -> Assert.throwRuntime()));
	}

	@Test
	public void shouldAdaptNonMatchingTypes() {
		var ad = ExceptionAdapter.of(IOException.class, IOException::new);
		Exception t = new InterruptedException("test");
		IOException e = ad.apply(t);
		Assert.notEqual(e, t);
		t = new Exception("test");
		e = ad.apply(t);
		Assert.notEqual(e, t);
	}

	@Test
	public void shouldCreateAdapterFunctionFromClass() {
		Assert.thrown(() -> ExceptionAdapter.of(FileNotFoundException.class));
		var ad = ExceptionAdapter.of(IOException.class);
		Exception t = new FileNotFoundException("test");
		Assert.equal(ad.apply(t), t);
		t = new IOException("test");
		Assert.equal(ad.apply(t), t);
		t = new InterruptedException("test");
		IOException e = ad.apply(t);
		Assert.notEqual(e, t);
		t = new Exception("test");
		e = ad.apply(t);
		Assert.notEqual(e, t);
	}

	@Test
	public void shouldAdaptRuntimeExceptions() {
		Exception t = new IOException("test");
		RuntimeException e = ExceptionAdapter.runtime.apply(t);
		Assert.notEqual(e, t);
		t = new InterruptedException("test");
		e = ExceptionAdapter.runtime.apply(t);
		Assert.notEqual(e, t);
		t = new Exception("test");
		e = ExceptionAdapter.runtime.apply(t);
		Assert.notEqual(e, t);
		t = new IllegalArgumentException("test");
		e = ExceptionAdapter.runtime.apply(t);
		Assert.equal(e, t);
		t = new RuntimeException("test");
		e = ExceptionAdapter.runtime.apply(t);
		Assert.equal(e, t);
	}
}
