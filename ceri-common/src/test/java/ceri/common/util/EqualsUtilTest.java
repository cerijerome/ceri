package ceri.common.util;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Test;

public class EqualsUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(EqualsUtil.class);
	}

	@Test
	public void testFloatEquals() {
		assertTrue(EqualsUtil.equals(Float.MAX_VALUE, Float.MAX_VALUE));
		assertTrue(EqualsUtil.equals(Float.MIN_NORMAL, Float.MIN_NORMAL));
		assertTrue(EqualsUtil.equals(Float.MIN_VALUE, Float.MIN_VALUE));
		assertTrue(EqualsUtil.equals(Float.NaN, Float.NaN));
		assertTrue(EqualsUtil.equals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
		assertTrue(EqualsUtil.equals(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
		assertTrue(EqualsUtil.equals(0.0f, 0.0f));
		assertFalse(EqualsUtil.equals(0.0f, -0.0f));
		assertFalse(EqualsUtil.equals(0.0000_0000_0000_0000_1f, 0.0000_0000_0000_0001f));
	}

	@Test
	public void testDoubleEquals() {
		assertTrue(EqualsUtil.equals(Double.MAX_VALUE, Double.MAX_VALUE));
		assertTrue(EqualsUtil.equals(Double.MIN_NORMAL, Double.MIN_NORMAL));
		assertTrue(EqualsUtil.equals(Double.MIN_VALUE, Double.MIN_VALUE));
		assertTrue(EqualsUtil.equals(Double.NaN, Double.NaN));
		assertTrue(EqualsUtil.equals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
		assertTrue(EqualsUtil.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
		assertTrue(EqualsUtil.equals(0.0, 0.0));
		assertFalse(EqualsUtil.equals(0.0, -0.0));
		assertFalse(EqualsUtil.equals(0.0000_0000_0000_0000_1, 0.0000_0000_0000_0001));
	}

	@Test
	public void testObjectEquals() {
		assertTrue(EqualsUtil.equals(null, null));
		assertFalse(EqualsUtil.equals("", null));
		assertFalse(EqualsUtil.equals(null, ""));
		assertTrue(EqualsUtil.equals(new Float(Float.MAX_VALUE), new Float(Float.MAX_VALUE)));
		assertTrue(EqualsUtil.equals(new Double(Double.MIN_VALUE), new Double(Double.MIN_VALUE)));
		Date date = new Date(0);
		java.sql.Date sqlDate = new java.sql.Date(0);
		assertTrue(EqualsUtil.equals(date, sqlDate));
	}

	@Test
	public void testStringEquals() {
		assertTrue(EqualsUtil.stringEquals(null, null));
		assertFalse(EqualsUtil.stringEquals("null", null));
		assertTrue(EqualsUtil.stringEquals(new Date(0), new Date(0)));
	}

}
