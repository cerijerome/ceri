package ceri.common.concurrent;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ValueConditionBehavior {

	@Test
	public void shouldSetAndClearValues() throws InterruptedException {
		ValueCondition<Integer> flag = new ValueCondition<>();
		assertThat(flag.value(), is((Integer) null));
		flag.signal(1);
		assertThat(flag.value(), is(1));
		flag.await();
		assertThat(flag.value(), is((Integer) null));
	}

}
