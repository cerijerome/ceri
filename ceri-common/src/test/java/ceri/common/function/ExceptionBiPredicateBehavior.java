package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.biPredicate;
import static ceri.common.test.TestUtil.assertFalse;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import java.io.IOException;
import java.util.function.BiPredicate;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionBiPredicateBehavior {

	@Test
	public void shouldAllowNaming() throws IOException {
		ExceptionBiPredicate<IOException, Integer, Integer> p = biPredicate().name("name");
		assertThat(p.toString(), is("name"));
		assertThat(p.test(2, 2), is(true));
		assertThat(p.test(-1, 2), is(false));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldConvertToBiPredicate() {
		BiPredicate<Integer, Integer> p = biPredicate().asBiPredicate();
		assertThat(p.test(2, 2), is(true));
		assertThrown(RuntimeException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldConvertFromBiPredicate() {
		ExceptionBiPredicate<RuntimeException, Integer, Integer> p =
			ExceptionBiPredicate.of(Std.biPredicate());
		assertThat(p.test(1, 2), is(true));
		assertThat(p.test(-1, 2), is(false));
		assertThrown(() -> p.test(0, 2));
	}

	@Test
	public void shouldNegateTest() throws IOException {
		ExceptionBiPredicate<IOException, Integer, Integer> p0 = biPredicate();
		ExceptionBiPredicate<IOException, Integer, Integer> p = p0.negate();
		assertThat(p0.test(2, 2), is(true));
		assertThat(p.test(2, 2), is(false));
		assertThat(p.test(-2, 2), is(true));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyOrTests() throws IOException {
		ExceptionBiPredicate<IOException, Integer, Integer> p = biPredicate().or((i, j) -> i < -1);
		assertTrue(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyAndTests() throws IOException {
		ExceptionBiPredicate<IOException, Integer, Integer> p = biPredicate().and((i, j) -> i < 3);
		assertTrue(p.test(2, 2));
		assertFalse(p.test(3, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

}
