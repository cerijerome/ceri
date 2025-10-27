package ceri.common.function;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class FluentBehavior {

	@Test
	public void shouldApplyConsumer() {
		Captor.OfInt capturer = Captor.ofInt();
		FluentTest test = new FluentTest(3);
		test.apply(t -> capturer.accept(t.id)).apply(null).apply(t -> capturer.accept(-t.id));
		capturer.verifyInt(3, -3);
	}

	@Test
	public void shouldMapToObject() {
		FluentTest test = new FluentTest(3);
		Assert.equal(test.map(t -> String.valueOf(t.id)), "3");
		Assert.thrown(() -> test.map(null));
	}

	@Test
	public void shouldMapToInt() {
		FluentTest test = new FluentTest(3);
		Assert.equal(test.mapToInt(t -> t.id), 3);
		Assert.thrown(() -> test.mapToInt(null));
	}

	static class FluentTest implements Fluent<FluentTest> {
		final int id;

		FluentTest(int id) {
			this.id = id;
		}
	}
}
