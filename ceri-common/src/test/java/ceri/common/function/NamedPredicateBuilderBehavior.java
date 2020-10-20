package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.function.Predicate;
import org.junit.Test;

public class NamedPredicateBuilderBehavior {

	@Test
	public void shouldReturnNullForEmptyPredicate() {
		NamedPredicateBuilder<?, Integer> b = NamedPredicateBuilder.of();
		assertNull(b.build());
	}

	@Test
	public void shouldCombinePredicates() throws Exception {
		NamedPredicateBuilder<?, Integer> b = NamedPredicateBuilder.of(i -> i > 1, ">1");
		b.and(i -> i < 3, "<3").or(i -> i == 0, "=0");
		ExceptionPredicate<?, Integer> px = b.buildEx();
		assertEquals(px.toString(), "(>1&<3|=0)");
		assertTrue(px.test(0));
		assertFalse(px.test(1));
		assertTrue(px.test(2));
		assertFalse(px.test(3));
		Predicate<Integer> p = b.negate().build();
		assertEquals(p.toString(), "!(>1&<3|=0)");
		assertFalse(p.test(0));
		assertTrue(p.test(1));
		assertFalse(p.test(2));
		assertTrue(p.test(3));
		assertNull(NamedPredicateBuilder.of().buildEx());
		assertNull(NamedPredicateBuilder.of().negate().buildEx());
		assertEquals(NamedPredicateBuilder.of().negate().or(i -> true, ">1").build().toString(),
			"(>1)");
	}

}
