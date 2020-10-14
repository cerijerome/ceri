package ceri.common.function;

import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;
import ceri.common.test.Capturer;

public class FluentBehavior {

	@Test
	public void shouldApplyConsumer() {
		Capturer.Int capturer = Capturer.ofInt();
		FluentTest test = new FluentTest(3);
		test.apply(t -> capturer.accept(t.id)).apply(null).apply(t -> capturer.accept(-t.id));
		capturer.verifyInt(3, -3);
	}

	@Test
	public void shouldMapToObject() {
		FluentTest test = new FluentTest(3);
		assertThat(test.map(t -> String.valueOf(t.id)), is("3"));
		assertThrown(() -> test.map(null));
	}

	@Test
	public void shouldMapToInt() {
		FluentTest test = new FluentTest(3);
		assertThat(test.mapToInt(t -> t.id), is(3));
		assertThrown(() -> test.mapToInt(null));
	}

	static class FluentTest implements Fluent<FluentTest> {
		final int id;

		FluentTest(int id) {
			this.id = id;
		}
	}

}
