package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class NameValueBehavior {

	@Test
	public void shouldConstructNameValue() {
		NameValue<Integer> nv = new NameValue<>("test", 1);
		assertThat(nv.key, is("test"));
		assertThat(nv.value, is(1));
		assertThat(nv, is(new KeyValue<>("test", 1)));
	}

}
