package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.objIntPredicate;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionObjIntPredicateBehavior {

	@Test
	public void shouldAllowNaming() throws IOException {
		ExceptionObjIntPredicate<IOException, Integer> p = objIntPredicate().name("name");
		assertThat(p.toString(), is("name"));
		assertThat(p.test(2, 2), is(true));
		assertThat(p.test(-1, 2), is(false));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldConvertToBiPredicate() {
		ObjIntPredicate<Integer> p = objIntPredicate().asPredicate();
		assertThat(p.test(2, 2), is(true));
		assertThrown(RuntimeException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldConvertFromBiPredicate() {
		ExceptionObjIntPredicate<RuntimeException, Integer> p =
			ExceptionObjIntPredicate.of(Std.objIntPredicate());
		assertThat(p.test(1, 2), is(true));
		assertThat(p.test(-1, 2), is(false));
		assertThrown(() -> p.test(0, 2));
	}

	@Test
	public void shouldNegateTest() throws IOException {
		ExceptionObjIntPredicate<IOException, Integer> p0 = objIntPredicate();
		ExceptionObjIntPredicate<IOException, Integer> p = p0.negate();
		assertThat(p0.test(2, 2), is(true));
		assertThat(p.test(2, 2), is(false));
		assertThat(p.test(-2, 2), is(true));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyOrTests() throws IOException {
		ExceptionObjIntPredicate<IOException, Integer> p = objIntPredicate().or((i, j) -> i < -1);
		assertTrue(p.test(2, 2));
		assertTrue(p.test(-2, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

	@Test
	public void shouldLogicallyAndTests() throws IOException {
		ExceptionObjIntPredicate<IOException, Integer> p = objIntPredicate().and((i, j) -> i < 3);
		assertTrue(p.test(2, 2));
		assertFalse(p.test(3, 2));
		assertFalse(p.test(-1, 2));
		assertThrown(IOException.class, () -> p.test(1, 2));
		assertThrown(RuntimeException.class, () -> p.test(0, 2));
	}

}
