package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import org.junit.Test;

public class MarshallerBehavior {

	@Test
	public void shouldConvertBetweenStringsAndObjects() {
		Marshaller<Double> m = Marshaller.of(String::valueOf, Double::valueOf);
		assertNull(m.to(null));
		assertEquals(m.to(.123456), "0.123456");
		assertNull(m.from(null));
		assertEquals(m.from(".123456"), 0.123456);
	}

}
