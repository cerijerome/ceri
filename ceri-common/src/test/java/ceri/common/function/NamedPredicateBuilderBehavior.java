package ceri.common.function;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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
		assertThat(px.toString(), is("(>1&<3|=0)"));
		assertThat(px.test(0), is(true));
		assertThat(px.test(1), is(false));
		assertThat(px.test(2), is(true));
		assertThat(px.test(3), is(false));
		Predicate<Integer> p = b.negate().build();
		assertThat(p.toString(), is("!(>1&<3|=0)"));
		assertThat(p.test(0), is(false));
		assertThat(p.test(1), is(true));
		assertThat(p.test(2), is(false));
		assertThat(p.test(3), is(true));
		assertNull(NamedPredicateBuilder.of().buildEx());
		assertNull(NamedPredicateBuilder.of().negate().buildEx());
		assertThat(NamedPredicateBuilder.of().negate().or(i -> true, ">1").build().toString(),
			is("(>1)"));
	}

}
