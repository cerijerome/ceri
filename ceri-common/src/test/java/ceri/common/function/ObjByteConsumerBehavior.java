package ceri.common.function;

import static ceri.common.test.TestUtil.assertArray;
import org.junit.Test;

public class ObjByteConsumerBehavior {

	@Test
	public void shouldConvertToIntConsumer() {
		String[] ss = new String[1];
		ObjByteConsumer<String[]> c = (s, b) -> s[0] = String.valueOf(b);
		c.accept(ss, (byte) 0xff);
		assertArray(ss, "-1");
		ObjByteConsumer.toInt(c).accept(ss, 0xfe);
		assertArray(ss, "-2");
	}

}
