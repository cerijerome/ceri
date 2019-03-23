package ceri.common.function;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.function.Predicate;
import org.junit.Test;

public class PredicateBuilderBehavior {

	@Test
	public void shouldJoinPredicates() {
		PredicateBuilder<Integer> b = PredicateBuilder.of();
		assertNull(b.build());
		Predicate<Integer> p = b.add(i -> i > -1, ">-1").add(i -> i < 1, "<1").build();
		assertThat(p.toString(), is("[>-1, <1]"));
		assertThat(p.test(-1), is(false));
		assertThat(p.test(0), is(true));
		assertThat(p.test(1), is(false));
	}

}
