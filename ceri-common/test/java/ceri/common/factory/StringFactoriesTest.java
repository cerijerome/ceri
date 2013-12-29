package ceri.common.factory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class StringFactoriesTest {

	@Test
	public void testCharArray() {
		assertThat(StringFactories.FROM_CHAR_ARRAY.create(new char[] {}), is(""));
		assertThat(StringFactories.FROM_CHAR_ARRAY.create(new char[] { 'a' }), is("a"));
		assertThat(StringFactories.FROM_CHAR_ARRAY.create(new char[] { 'a', '\0', 'b' }),
			is("a\0b"));
		assertNull(StringFactories.FROM_CHAR_ARRAY.create(null));
	}

	@Test
	public void testObject() {
		assertThat(StringFactories.FROM_OBJECT.create("test"), is("test"));
		assertThat(StringFactories.FROM_OBJECT.create(1), is("1"));
		assertNull(StringFactories.FROM_OBJECT.create(null));
	}

	
	@Test
	public void test() {
		assertNull(StringFactories.TO_BOOLEAN.create(null));
	}


}
