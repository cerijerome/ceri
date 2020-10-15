package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.longPredicate;
import static ceri.common.test.TestUtil.assertFalse;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import java.io.IOException;
import java.util.function.LongPredicate;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionLongPredicateBehavior {

	@Test
	public void shouldAllowNaming() throws IOException {
		ExceptionLongPredicate<IOException> p = longPredicate().name("name");
		assertThat(p.toString(), is("name"));
		assertThat(p.test(2), is(true));
		assertThat(p.test(-1), is(false));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldConvertToPredicate() {
		LongPredicate p = longPredicate().asPredicate();
		assertThat(p.test(2), is(true));
		assertThrown(RuntimeException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldConvertFromPredicate() {
		ExceptionLongPredicate<RuntimeException> p = ExceptionLongPredicate.of(Std.longPredicate());
		assertThat(p.test(1), is(true));
		assertThat(p.test(-1), is(false));
		assertThrown(() -> p.test(0));
	}

	@Test
	public void shouldNegateTest() throws IOException {
		ExceptionLongPredicate<IOException> p0 = longPredicate();
		ExceptionLongPredicate<IOException> p = p0.negate();
		assertThat(p0.test(2), is(true));
		assertThat(p.test(2), is(false));
		assertThat(p.test(-1), is(true));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldLogicallyOrTests() throws IOException {
		ExceptionLongPredicate<IOException> p = longPredicate().or(i -> i < -1);
		assertTrue(p.test(2));
		assertTrue(p.test(-2));
		assertFalse(p.test(-1));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

	@Test
	public void shouldLogicallyAndTests() throws IOException {
		ExceptionLongPredicate<IOException> p = longPredicate().and(i -> i < 3);
		assertTrue(p.test(2));
		assertFalse(p.test(3));
		assertFalse(p.test(-1));
		assertThrown(IOException.class, () -> p.test(1));
		assertThrown(RuntimeException.class, () -> p.test(0));
	}

}
