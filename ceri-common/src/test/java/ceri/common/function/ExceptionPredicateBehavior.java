package ceri.common.function;

import static ceri.common.test.TestUtil.assertException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;

public class ExceptionPredicateBehavior {
	private static final ExceptionPredicate<IOException, String> predicate = s -> {
		if (s == null) throw new IOException();
		return s.length() == 1;
	};
	private static final ExceptionPredicate<FileNotFoundException, String> predicate2 = s -> {
		if (s == null) throw new FileNotFoundException();
		return s.startsWith("a");
	};

	@Test
	public void shouldTestValues() throws IOException {
		assertTrue(predicate.test("a"));
		assertException(() -> predicate.test(null));
		assertFalse(predicate.test(""));
	}

	@Test
	public void shouldNegateTest() throws IOException {
		ExceptionPredicate<IOException, String> p = predicate.negate();
		assertFalse(p.test("a"));
		assertException(() -> p.test(null));
		assertTrue(p.test(""));
	}

	@Test
	public void shouldLogicallyOrTests() throws IOException {
		ExceptionPredicate<IOException, String> p = predicate.or(predicate2);
		assertTrue(p.test("a"));
		assertException(() -> p.test(null));
		assertFalse(p.test(""));
		assertTrue(p.test("abc"));
	}

	@Test
	public void shouldLogicallyAndTests() throws IOException {
		ExceptionPredicate<IOException, String> p = predicate.and(predicate2);
		assertTrue(p.test("a"));
		assertException(() -> p.test(null));
		assertFalse(p.test("b"));
		assertFalse(p.test("abc"));
	}

}