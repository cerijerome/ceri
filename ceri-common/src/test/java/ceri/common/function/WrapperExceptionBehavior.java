package ceri.common.function;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.WrapperException.Agent;

public class WrapperExceptionBehavior {
	private final Agent<IOException> agent = new Agent<>();

	@Test
	public void shouldWrapBiFunctions() throws IOException {
		assertThat(agent.wrap((s, i) -> s + "=" + i, "value", 3), is("value=3"));
		assertThat(agent.handle((s, i) -> s + "=" + i, "x", -1), is("x=-1"));
	}

	@Test
	public void shouldWrapBiFunctionCheckedExceptions() {
		try {
			agent.wrap((s, i) -> {
				throw new IOException();
			}, "value", 3);
			fail();
		} catch (WrapperException e) {
			assertSame(e.getCause().getClass(), IOException.class);
		}
	}

	@Test
	public void shouldNotWrapBiFunctionRuntimeExceptions() {
		try {
			agent.wrap((s, i) -> {
				throw new IllegalArgumentException();
			}, "value", 3);
			fail();
		} catch (IllegalArgumentException e) {
			//
		}
	}

	@Test
	public void shouldRethrowBiFunctionWrappedExceptions() {
		try {
			agent.handle((s, i) -> {
				throw new WrapperException(agent, new IOException());
			}, "value", 3);
			fail();
		} catch (IOException e) {
			//
		}
	}

	@Test
	public void shouldFailRethrowWithMismatchedAgent() throws IOException {
		try {
			agent.handle((s, i) -> {
				throw new WrapperException(new Agent<>(), new IOException());
			}, "value", 3);
			fail();
		} catch (RuntimeException e) {
			//
		}
	}

}
