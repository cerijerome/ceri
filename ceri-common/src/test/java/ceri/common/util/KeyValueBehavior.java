package ceri.common.util;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.common.util.KeyValue.Named;

public class KeyValueBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		KeyValue<String, String> t = new KeyValue<>("key", "value");
		KeyValue<String, String> eq0 = new KeyValue<>("key", "value");
		KeyValue<String, Object> eq1 = new KeyValue<>("key", "value");
		KeyValue<Object, String> eq2 = new KeyValue<>("key", "value");
		KeyValue<Object, Object> eq3 = new KeyValue<>("key", "value");
		KeyValue<String, String> ne0 = new KeyValue<>(null, "value");
		KeyValue<String, String> ne1 = new KeyValue<>("key", null);
		KeyValue<String, String> ne2 = new KeyValue<>(null, null);
		KeyValue<String, String> ne3 = new KeyValue<>("key0", "value");
		KeyValue<String, String> ne4 = new KeyValue<>("key", "value0");
		KeyValue<String, String> ne5 = new KeyValue<>("key0", "value0");
		exerciseEquals(t, eq0, eq1, eq2, eq3);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldConstructNameValue() {
		Named<Integer> nv = KeyValue.named("test", 1);
		assertEquals(nv.key, "test");
		assertEquals(nv.value, 1);
		assertEquals(nv, KeyValue.of("test", 1));
	}
}
