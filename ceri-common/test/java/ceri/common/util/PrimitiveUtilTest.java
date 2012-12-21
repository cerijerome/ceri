package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;

public class PrimitiveUtilTest {

	@Test
	public void testConvertArray() {
		boolean[] b = { true, false };
		Boolean[] array = PrimitiveUtil.convertBooleanArray(b);
		assertThat(array[0], is(Boolean.TRUE));
		assertThat(array[1], is(Boolean.FALSE));
		assertThat(array.length, is(2));
		b = PrimitiveUtil.convertBooleanArray(array);
		assertTrue(b[0]);
		assertFalse(b[1]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetClass() {
		assertSame(double.class, PrimitiveUtil.getPrimitiveClass(Double.class));
		assertSame(Double.class, PrimitiveUtil.getObjectClass(double.class));
		PrimitiveUtil.getObjectClass(Boolean.class);
	}

	@Test
	public void testAutoBoxAssignable() {
		assertThat(PrimitiveUtil.isAutoBoxAssignable(Long.class, long.class), is(true));
		assertThat(PrimitiveUtil.isAutoBoxAssignable(long.class, Long.class), is(true));
		assertThat(PrimitiveUtil.isAutoBoxAssignable(double.class, Number.class), is(true));
		assertThat(PrimitiveUtil.isAutoBoxAssignable(Number.class, double.class), is(false));
	}

	@Test
	public void testToArray() {
		Collection<Number> collection = Arrays.<Number>asList(1, 16, 256, 4096, 65536);
		assertThat(PrimitiveUtil.toByteArray(collection), is(new byte[] { 1, 16, 0, 0, 0 }));
		assertThat(PrimitiveUtil.toShortArray(collection), is(new short[] { 1, 16, 256, 4096, 0 }));
		assertThat(PrimitiveUtil.toFloatArray(collection),
			is(new float[] { 1, 16, 256, 4096, 65536 }));
	}

	@Test
	public void testValueOf() {
		assertThat(PrimitiveUtil.valueOf("FALSE", true), is(false));
		assertThat(PrimitiveUtil.valueOf(null, Byte.MIN_VALUE), is(Byte.MIN_VALUE));
		assertThat(PrimitiveUtil.valueOf("" + Double.MAX_VALUE, 0.0), is(Double.MAX_VALUE));
	}

}
