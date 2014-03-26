package ceri.common.factory;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class StringFactoriesTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(StringFactories.class);
	}

	@Test
	public void testFromCharArray() {
		assertThat(StringFactories.FROM_CHAR_ARRAY.create(new char[] {}), is(""));
		assertThat(StringFactories.FROM_CHAR_ARRAY.create(new char[] { 'a' }), is("a"));
		assertThat(StringFactories.FROM_CHAR_ARRAY.create(new char[] { 'a', '\0', 'b' }),
			is("a\0b"));
		assertNull(StringFactories.FROM_CHAR_ARRAY.create(null));
	}

	@Test
	public void testToCharArray() {
		assertThat(StringFactories.TO_CHAR_ARRAY.create(""), is(new char[] {}));
		assertThat(StringFactories.TO_CHAR_ARRAY.create("a\0b"), is(new char[] { 'a', '\0', 'b' }));
		assertNull(StringFactories.TO_CHAR_ARRAY.create(null));
	}

	@Test
	public void testObject() {
		assertThat(StringFactories.FROM_OBJECT.create("test"), is("test"));
		assertThat(StringFactories.FROM_OBJECT.create(1), is("1"));
		assertNull(StringFactories.FROM_OBJECT.create(null));
	}

	@Test
	public void testBoolean() {
		assertThat(StringFactories.TO_BOOLEAN.create(""), is(false));
		assertThat(StringFactories.TO_BOOLEAN.create("1"), is(false));
		assertThat(StringFactories.TO_BOOLEAN.create("fALSE"), is(false));
		assertThat(StringFactories.TO_BOOLEAN.create("TrUe"), is(true));
		assertNull(StringFactories.TO_BOOLEAN.create(null));
	}

	@Test
	public void testByte() {
		assertThat(StringFactories.TO_BYTE.create("0"), is((byte) 0));
		assertThat(StringFactories.TO_BYTE.create("127"), is((byte) 127));
		assertThat(StringFactories.TO_BYTE.create("-128"), is((byte) -128));
		assertNull(StringFactories.TO_BYTE.create(null));
		assertFail(StringFactories.TO_BYTE, "128"); // Max + 1
		assertFail(StringFactories.TO_BYTE, "x");
		assertFail(StringFactories.TO_BYTE, "");
	}

	@Test
	public void testShort() {
		assertThat(StringFactories.TO_SHORT.create("0"), is((short) 0));
		assertThat(StringFactories.TO_SHORT.create("32767"), is((short) 32767));
		assertThat(StringFactories.TO_SHORT.create("-32768"), is((short) -32768));
		assertNull(StringFactories.TO_SHORT.create(null));
		assertFail(StringFactories.TO_SHORT, "32768"); // Max + 1
		assertFail(StringFactories.TO_SHORT, "x");
		assertFail(StringFactories.TO_SHORT, "");
	}

	@Test
	public void testInteger() {
		assertThat(StringFactories.TO_INTEGER.create("0"), is(0));
		assertThat(StringFactories.TO_INTEGER.create("2147483647"), is(2147483647));
		assertThat(StringFactories.TO_INTEGER.create("-2147483648"), is(-2147483648));
		assertNull(StringFactories.TO_INTEGER.create(null));
		assertFail(StringFactories.TO_INTEGER, "2147483648"); // Max + 1
		assertFail(StringFactories.TO_INTEGER, "x");
		assertFail(StringFactories.TO_INTEGER, "");
	}

	@Test
	public void testLong() {
		assertThat(StringFactories.TO_LONG.create("0"), is(0L));
		assertThat(StringFactories.TO_LONG.create("9223372036854775807"), is(9223372036854775807L));
		assertThat(StringFactories.TO_LONG.create("-9223372036854775808"),
			is(-9223372036854775808L));
		assertNull(StringFactories.TO_LONG.create(null));
		assertFail(StringFactories.TO_LONG, "9223372036854775808"); // Max + 1
		assertFail(StringFactories.TO_LONG, "x");
		assertFail(StringFactories.TO_LONG, "");
	}

	@Test
	public void testDouble() {
		assertThat(StringFactories.TO_DOUBLE.create("0"), is(0d));
		assertThat(StringFactories.TO_DOUBLE.create("1.7976931348623157E308"), is(1.7976931348623157E308)); // MAX_VALUE
		assertThat(StringFactories.TO_DOUBLE.create("-1.7976931348623157E308"), is(-1.7976931348623157E308)); // -MAX_VALUE
		assertThat(StringFactories.TO_DOUBLE.create("4.9E-324"), is(4.9E-324)); // MIN_VALUE
		assertThat(StringFactories.TO_DOUBLE.create("-4.9E-324"), is(-4.9E-324)); // -MIN_VALUE
		assertThat(StringFactories.TO_DOUBLE.create("2.2250738585072014E-308"), is(2.2250738585072014E-308)); // MIN_NORMAL
		assertThat(StringFactories.TO_DOUBLE.create("-2.2250738585072014E-308"), is(-2.2250738585072014E-308)); // MIN_NORMAL
		assertThat(StringFactories.TO_DOUBLE.create("Infinity"), is(Double.POSITIVE_INFINITY));
		assertThat(StringFactories.TO_DOUBLE.create("-Infinity"), is(Double.NEGATIVE_INFINITY));
		assertThat(StringFactories.TO_DOUBLE.create("NaN"), is(Double.NaN));
		assertNull(StringFactories.TO_DOUBLE.create(null));
		assertFail(StringFactories.TO_DOUBLE, "x");
		assertFail(StringFactories.TO_DOUBLE, "");
	}

	@Test
	public void testFloat() {
		assertThat(StringFactories.TO_FLOAT.create("0"), is(0f));
		assertThat(StringFactories.TO_FLOAT.create("3.4028235E38"), is(3.4028235E38f)); // MAX_VALUE
		assertThat(StringFactories.TO_FLOAT.create("-3.4028235E38"), is(-3.4028235E38f)); // -MAX_VALUE
		assertThat(StringFactories.TO_FLOAT.create("1.4E-45"), is(1.4E-45f)); // MIN_VALUE
		assertThat(StringFactories.TO_FLOAT.create("1.4E-45"), is(1.4E-45f)); // -MIN_VALUE
		assertThat(StringFactories.TO_FLOAT.create("1.17549435E-38"), is(1.17549435E-38f)); // MIN_NORMAL
		assertThat(StringFactories.TO_FLOAT.create("-1.17549435E-38"), is(-1.17549435E-38f)); // MIN_NORMAL
		assertThat(StringFactories.TO_FLOAT.create("Infinity"), is(Float.POSITIVE_INFINITY));
		assertThat(StringFactories.TO_FLOAT.create("-Infinity"), is(Float.NEGATIVE_INFINITY));
		assertThat(StringFactories.TO_FLOAT.create("NaN"), is(Float.NaN));
		assertNull(StringFactories.TO_FLOAT.create(null));
		assertFail(StringFactories.TO_FLOAT, "x");
		assertFail(StringFactories.TO_FLOAT, "");
	}

	private static <T> void assertFail(final Factory<T, String> factory, final String from) {
		TestUtil.assertException(NumberFormatException.class, () -> factory.create(from));
	}

}
