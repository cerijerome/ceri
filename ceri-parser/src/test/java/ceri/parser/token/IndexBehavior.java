package ceri.parser.token;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;


public class IndexBehavior {

	@Test
	public void shouldSetValue() {
		Index i = new Index();
		i.set(Integer.MAX_VALUE);
		assertThat(i.value(), is(Integer.MAX_VALUE));
		i.set(Integer.MIN_VALUE);
		assertThat(i.value(), is(Integer.MIN_VALUE));
	}
	
	@Test
	public void shouldIncrementAndReturnCurrentValue() {
		Index i = new Index();
		int value = i.inc();
		assertThat(value, is(1));
		assertThat(i.value(), is(1));
		i.set(Integer.MIN_VALUE);
		value = i.inc();
		assertThat(value, is(Integer.MIN_VALUE + 1));
		assertThat(i.value(), is(Integer.MIN_VALUE + 1));
	}

}
