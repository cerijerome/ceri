package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.assertIoEx;
import static ceri.common.function.FunctionTestUtil.assertRtEx;
import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.function.FunctionTestUtil.runnable;
import static ceri.common.function.FunctionTestUtil.supplier;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.Test;

public class FunctionUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(FunctionUtil.class);
	}

	@Test
	public void testSafe() {
		Function<String, String> fn = FunctionUtil.safe(s -> s.trim());
		assertThat(fn.apply(" "), is(""));
		assertNull(fn.apply(null));
	}

	@Test
	public void testRecurse() {
		assertThat(FunctionUtil.recurse("test", s -> s.replaceFirst("[a-z]", "X")), is("XXXX"));
		assertThat(FunctionUtil.recurse("hello", s -> s.substring(1), 3), is("lo"));
		assertException(() -> FunctionUtil.recurse("hello", s -> s.substring(1)));
	}

	@Test
	public void testAsFunction() throws IOException {
		assertThat(FunctionUtil.asFunction(consumer(2)).apply(1), is(Boolean.TRUE));
		assertThat(FunctionUtil.asFunction(supplier(1, 2)).apply(1), is(1));
		assertThat(FunctionUtil.asFunction(runnable(1, 2)).apply(1), is(Boolean.TRUE));
	}

	@Test
	public void testForEach() throws IOException {
		assertIoEx("2", () -> FunctionUtil.forEach(Stream.of(1, 2, 3), consumer(2)));
		assertRtEx("0", () -> FunctionUtil.forEach(Stream.of(1, 0, 3), consumer(2)));
		FunctionUtil.forEach(Stream.of(1, 2, 3), consumer(4));
		assertIoEx("2", () -> FunctionUtil.forEach(Arrays.asList(1, 2, 3), consumer(2)));
		assertRtEx("0", () -> FunctionUtil.forEach(Arrays.asList(1, 0, 3), consumer(2)));
		FunctionUtil.forEach(Arrays.asList(1, 2, 3), consumer(4));
	}

}
