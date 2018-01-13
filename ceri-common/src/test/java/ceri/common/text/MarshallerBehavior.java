package ceri.common.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class MarshallerBehavior {

	@Test
	public void shouldConvertBetweenStringsAndObjects() {
		Marshaller<Double> m = Marshaller.of(String::valueOf, Double::valueOf);
		assertNull(m.to(null));
		assertThat(m.to(.123456), is("0.123456"));
		assertNull(m.from(null));
		assertThat(m.from(".123456"), is(0.123456));
	}

}
