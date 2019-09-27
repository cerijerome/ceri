package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.intPredicate;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.function.IntPredicate;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionIntPredicateBehavior {

	@Test
	public void shouldAllowNaming() throws IOException {
		ExceptionIntPredicate<IOException> p = intPredicate().name("name");
		assertThat(p.toString(), is("name"));
		assertThat(p.test(2), is(true));
		assertThat(p.test(-1), is(false));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldConvertToPredicate() {
		IntPredicate p = intPredicate().asPredicate();
		assertThat(p.test(2), is(true));
		assertThrown(RuntimeException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldConvertFromPredicate() {
		ExceptionIntPredicate<RuntimeException> p = ExceptionIntPredicate.of(Std.intPredicate());
		assertThat(p.test(1), is(true));
		assertThat(p.test(-1), is(false));
		assertThrown(() -> p.test(0));
	}

	@Test
	public void shouldNegateTest() throws IOException {
		ExceptionIntPredicate<IOException> p0 = intPredicate();
		ExceptionIntPredicate<IOException> p = p0.negate();
		assertThat(p0.test(2), is(true));
		assertThat(p.test(2), is(false));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldLogicallyOrTests() throws IOException {
		ExceptionIntPredicate<IOException> p = intPredicate().or(i -> i < -1);
		assertTrue(p.test(2));
		assertTrue(p.test(-2));
		assertFalse(p.test(-1));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldLogicallyAndTests() throws IOException {
		ExceptionIntPredicate<IOException> p = intPredicate().and(i -> i < 3);
		assertTrue(p.test(2));
		assertFalse(p.test(3));
		assertFalse(p.test(-1));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

}
