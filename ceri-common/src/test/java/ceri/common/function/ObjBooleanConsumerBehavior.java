package ceri.common.function;

import static ceri.common.test.TestUtil.assertArray;
import org.junit.Test;

public class ObjBooleanConsumerBehavior {

	@Test
	public void shouldConvertToIntConsumer() {
		String[] ss = new String[1];
		ObjBooleanConsumer<String[]> c = (s, b) -> s[0] = String.valueOf(b);
		c.accept(ss, false);
		assertArray(ss, "false");
		ObjBooleanConsumer.toInt(c).accept(ss, -1);
		assertArray(ss, "true");
		ObjBooleanConsumer.toInt(c).accept(ss, 0);
		assertArray(ss, "false");
	}

}
