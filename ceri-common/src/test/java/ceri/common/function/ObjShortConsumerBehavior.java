package ceri.common.function;

import static ceri.common.test.TestUtil.assertArray;
import org.junit.Test;

public class ObjShortConsumerBehavior {

	@Test
	public void shouldConvertToIntConsumer() {
		String[] ss = new String[1];
		ObjShortConsumer<String[]> c = (s, b) -> s[0] = String.valueOf(b);
		c.accept(ss, (short) 0xffff);
		assertArray(ss, "-1");
		ObjShortConsumer.toInt(c).accept(ss, 0xfffe);
		assertArray(ss, "-2");
	}

}
