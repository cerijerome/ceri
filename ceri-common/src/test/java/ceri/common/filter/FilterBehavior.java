package ceri.common.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.function.Predicate;
import org.junit.Test;

public class FilterBehavior {

	@Test
	public void shouldConvertPredicateToFilter() {
		Filter<String> f = Filter.from(Predicate.isEqual("x"));
		assertTrue(f.filter("x"));
		assertFalse(f.filter(null));
		assertFalse(f.filter(""));
		assertFalse(f.filter("X"));
	}

	@Test
	public void shouldUpcastFilter() {
		Filter<Number> f0 = n -> n.intValue() > 0;
		Filter<Integer> f1 = f0.up();
		assertTrue(f0.filter(1));
		assertTrue(f1.filter(1));
		assertFalse(f0.filter(0));
		assertFalse(f1.filter(0));
		assertFalse(f0.filter(-1));
		assertFalse(f1.filter(-1));
	}

}
