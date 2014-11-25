package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class KeyValueBehavior {

	@Test
	public void shouldObeyEqualsContractForEqualKeysAndValues() {
		KeyValue<String, String> kv = new KeyValue<>("key", "value");
		KeyValue<String, String> kv1 = new KeyValue<>("key", "value");
		KeyValue<String, Object> kv2 = new KeyValue<>("key", "value");
		KeyValue<Object, String> kv3 = new KeyValue<>("key", "value");
		KeyValue<Object, Object> kv4 = new KeyValue<>("key", "value");
		assertThat(kv, is(kv));
		assertThat(kv, is(kv1));
		assertThat(kv, is(kv2));
		assertThat(kv, is(kv3));
		assertThat(kv, is(kv4));
		assertThat(kv.hashCode(), is(kv4.hashCode()));
		assertThat(kv.toString(), is(kv4.toString()));
	}

	@Test
	public void shouldObeyEqualsContractForUnequalsKeysOrValues() {
		KeyValue<String, String> kv = new KeyValue<>("key", "value");
		KeyValue<String, String> kv1 = new KeyValue<>(null, "value");
		KeyValue<String, String> kv2 = new KeyValue<>("key", null);
		KeyValue<String, String> kv3 = new KeyValue<>(null, null);
		KeyValue<String, String> kv4 = new KeyValue<>("key0", "value");
		KeyValue<String, String> kv5 = new KeyValue<>("key", "value0");
		KeyValue<String, String> kv6 = new KeyValue<>("key0", "value0");
		assertFalse(kv.equals(null));
		assertThat(kv, is(not("")));
		assertThat(kv, is(not(kv1)));
		assertThat(kv, is(not(kv2)));
		assertThat(kv, is(not(kv3)));
		assertThat(kv, is(not(kv4)));
		assertThat(kv, is(not(kv5)));
		assertThat(kv, is(not(kv6)));
	}

}
