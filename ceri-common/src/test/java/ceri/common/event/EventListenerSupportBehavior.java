package ceri.common.event;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class EventListenerSupportBehavior {

	static class TestListener<T> implements EventListener<T> {
		List<T> events = new ArrayList<>();

		@Override
		public void event(T event) {
			events.add(event);
		}
	}

	@Test
	public void shouldNotifyListenersOfEvents() {
		TestListener<Integer> listener1 = new TestListener<>();
		TestListener<Integer> listener2 = new TestListener<>();
		EventListenerSupport<Integer> support = EventListenerSupport.create(listener1, listener2);
		support.event(1);
		support.event(2);
		assertThat(listener1.events, is(Arrays.asList(1, 2)));
		assertThat(listener2.events, is(Arrays.asList(1, 2)));
	}

}
