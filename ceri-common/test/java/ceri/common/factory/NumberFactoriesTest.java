package ceri.common.factory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class NumberFactoriesTest {

	@Test
	public void testByte() {
		assertThat(NumberFactories.TO_BYTE.create(Long.MAX_VALUE), is((byte)Long.MAX_VALUE));
		assertThat(NumberFactories.TO_BYTE.create(Long.MIN_VALUE), is((byte)Long.MIN_VALUE));
		assertThat(NumberFactories.TO_BYTE.create(Integer.MAX_VALUE), is((byte)Integer.MAX_VALUE));
		assertThat(NumberFactories.TO_BYTE.create(Integer.MIN_VALUE), is((byte)Integer.MIN_VALUE));
		assertThat(NumberFactories.TO_BYTE.create(Short.MAX_VALUE), is((byte)Short.MAX_VALUE));
		assertThat(NumberFactories.TO_BYTE.create(Short.MIN_VALUE), is((byte)Short.MIN_VALUE));
		assertThat(NumberFactories.TO_BYTE.create(Byte.MAX_VALUE), is(Byte.MAX_VALUE));
		assertThat(NumberFactories.TO_BYTE.create(Byte.MIN_VALUE), is(Byte.MIN_VALUE));
		assertThat(NumberFactories.TO_BYTE.create(Double.MAX_VALUE), is((byte)Double.MAX_VALUE));
		assertThat(NumberFactories.TO_BYTE.create(Double.MIN_VALUE), is((byte)Double.MIN_VALUE));
		assertThat(NumberFactories.TO_BYTE.create(Float.MAX_VALUE), is((byte)Float.MAX_VALUE));
		assertThat(NumberFactories.TO_BYTE.create(Float.MIN_VALUE), is((byte)Float.MIN_VALUE));
		assertThat(NumberFactories.TO_BYTE.create(0), is((byte)0));
		assertNull(NumberFactories.TO_BYTE.create(null));
	}
	
	@Test
	public void testShort() {
		assertThat(NumberFactories.TO_SHORT.create(Long.MAX_VALUE), is((short)Long.MAX_VALUE));
		assertThat(NumberFactories.TO_SHORT.create(Long.MIN_VALUE), is((short)Long.MIN_VALUE));
		assertThat(NumberFactories.TO_SHORT.create(Integer.MAX_VALUE), is((short)Integer.MAX_VALUE));
		assertThat(NumberFactories.TO_SHORT.create(Integer.MIN_VALUE), is((short)Integer.MIN_VALUE));
		assertThat(NumberFactories.TO_SHORT.create(Short.MAX_VALUE), is(Short.MAX_VALUE));
		assertThat(NumberFactories.TO_SHORT.create(Short.MIN_VALUE), is(Short.MIN_VALUE));
		assertThat(NumberFactories.TO_SHORT.create(Byte.MAX_VALUE), is((short)Byte.MAX_VALUE));
		assertThat(NumberFactories.TO_SHORT.create(Byte.MIN_VALUE), is((short)Byte.MIN_VALUE));
		assertThat(NumberFactories.TO_SHORT.create(Double.MAX_VALUE), is((short)Double.MAX_VALUE));
		assertThat(NumberFactories.TO_SHORT.create(Double.MIN_VALUE), is((short)Double.MIN_VALUE));
		assertThat(NumberFactories.TO_SHORT.create(Float.MAX_VALUE), is((short)Float.MAX_VALUE));
		assertThat(NumberFactories.TO_SHORT.create(Float.MIN_VALUE), is((short)Float.MIN_VALUE));
		assertThat(NumberFactories.TO_SHORT.create(0), is((short)0));
		assertNull(NumberFactories.TO_SHORT.create(null));
	}
	
	@Test
	public void testInteger() {
		assertThat(NumberFactories.TO_INTEGER.create(Long.MAX_VALUE), is((int)Long.MAX_VALUE));
		assertThat(NumberFactories.TO_INTEGER.create(Long.MIN_VALUE), is((int)Long.MIN_VALUE));
		assertThat(NumberFactories.TO_INTEGER.create(Integer.MAX_VALUE), is(Integer.MAX_VALUE));
		assertThat(NumberFactories.TO_INTEGER.create(Integer.MIN_VALUE), is(Integer.MIN_VALUE));
		assertThat(NumberFactories.TO_INTEGER.create(Short.MAX_VALUE), is((int)Short.MAX_VALUE));
		assertThat(NumberFactories.TO_INTEGER.create(Short.MIN_VALUE), is((int)Short.MIN_VALUE));
		assertThat(NumberFactories.TO_INTEGER.create(Byte.MAX_VALUE), is((int)Byte.MAX_VALUE));
		assertThat(NumberFactories.TO_INTEGER.create(Byte.MIN_VALUE), is((int)Byte.MIN_VALUE));
		assertThat(NumberFactories.TO_INTEGER.create(Double.MAX_VALUE), is((int)Double.MAX_VALUE));
		assertThat(NumberFactories.TO_INTEGER.create(Double.MIN_VALUE), is((int)Double.MIN_VALUE));
		assertThat(NumberFactories.TO_INTEGER.create(Float.MAX_VALUE), is((int)Float.MAX_VALUE));
		assertThat(NumberFactories.TO_INTEGER.create(Float.MIN_VALUE), is((int)Float.MIN_VALUE));
		assertThat(NumberFactories.TO_INTEGER.create(0), is(0));
		assertNull(NumberFactories.TO_INTEGER.create(null));
	}
	
	@Test
	public void testLong() {
		assertThat(NumberFactories.TO_LONG.create(Long.MAX_VALUE), is(Long.MAX_VALUE));
		assertThat(NumberFactories.TO_LONG.create(Long.MIN_VALUE), is(Long.MIN_VALUE));
		assertThat(NumberFactories.TO_LONG.create(Integer.MAX_VALUE), is((long)Integer.MAX_VALUE));
		assertThat(NumberFactories.TO_LONG.create(Integer.MIN_VALUE), is((long)Integer.MIN_VALUE));
		assertThat(NumberFactories.TO_LONG.create(Short.MAX_VALUE), is((long)Short.MAX_VALUE));
		assertThat(NumberFactories.TO_LONG.create(Short.MIN_VALUE), is((long)Short.MIN_VALUE));
		assertThat(NumberFactories.TO_LONG.create(Byte.MAX_VALUE), is((long)Byte.MAX_VALUE));
		assertThat(NumberFactories.TO_LONG.create(Byte.MIN_VALUE), is((long)Byte.MIN_VALUE));
		assertThat(NumberFactories.TO_LONG.create(Double.MAX_VALUE), is((long)Double.MAX_VALUE));
		assertThat(NumberFactories.TO_LONG.create(Double.MIN_VALUE), is((long)Double.MIN_VALUE));
		assertThat(NumberFactories.TO_LONG.create(Float.MAX_VALUE), is((long)Float.MAX_VALUE));
		assertThat(NumberFactories.TO_LONG.create(Float.MIN_VALUE), is((long)Float.MIN_VALUE));
		assertThat(NumberFactories.TO_LONG.create(0), is(0L));
		assertNull(NumberFactories.TO_LONG.create(null));
	}
	
	@Test
	public void testDouble() {
		assertThat(NumberFactories.TO_DOUBLE.create(Long.MAX_VALUE), is((double)Long.MAX_VALUE));
		assertThat(NumberFactories.TO_DOUBLE.create(Long.MIN_VALUE), is((double)Long.MIN_VALUE));
		assertThat(NumberFactories.TO_DOUBLE.create(Integer.MAX_VALUE), is((double)Integer.MAX_VALUE));
		assertThat(NumberFactories.TO_DOUBLE.create(Integer.MIN_VALUE), is((double)Integer.MIN_VALUE));
		assertThat(NumberFactories.TO_DOUBLE.create(Short.MAX_VALUE), is((double)Short.MAX_VALUE));
		assertThat(NumberFactories.TO_DOUBLE.create(Short.MIN_VALUE), is((double)Short.MIN_VALUE));
		assertThat(NumberFactories.TO_DOUBLE.create(Byte.MAX_VALUE), is((double)Byte.MAX_VALUE));
		assertThat(NumberFactories.TO_DOUBLE.create(Byte.MIN_VALUE), is((double)Byte.MIN_VALUE));
		assertThat(NumberFactories.TO_DOUBLE.create(Double.MAX_VALUE), is(Double.MAX_VALUE));
		assertThat(NumberFactories.TO_DOUBLE.create(Double.MIN_VALUE), is(Double.MIN_VALUE));
		assertThat(NumberFactories.TO_DOUBLE.create(Float.MAX_VALUE), is((double)Float.MAX_VALUE));
		assertThat(NumberFactories.TO_DOUBLE.create(Float.MIN_VALUE), is((double)Float.MIN_VALUE));
		assertThat(NumberFactories.TO_DOUBLE.create(0), is(0d));
		assertNull(NumberFactories.TO_DOUBLE.create(null));
	}
	
	@Test
	public void testFloat() {
		assertThat(NumberFactories.TO_FLOAT.create(Long.MAX_VALUE), is((float)Long.MAX_VALUE));
		assertThat(NumberFactories.TO_FLOAT.create(Long.MIN_VALUE), is((float)Long.MIN_VALUE));
		assertThat(NumberFactories.TO_FLOAT.create(Integer.MAX_VALUE), is((float)Integer.MAX_VALUE));
		assertThat(NumberFactories.TO_FLOAT.create(Integer.MIN_VALUE), is((float)Integer.MIN_VALUE));
		assertThat(NumberFactories.TO_FLOAT.create(Short.MAX_VALUE), is((float)Short.MAX_VALUE));
		assertThat(NumberFactories.TO_FLOAT.create(Short.MIN_VALUE), is((float)Short.MIN_VALUE));
		assertThat(NumberFactories.TO_FLOAT.create(Byte.MAX_VALUE), is((float)Byte.MAX_VALUE));
		assertThat(NumberFactories.TO_FLOAT.create(Byte.MIN_VALUE), is((float)Byte.MIN_VALUE));
		assertThat(NumberFactories.TO_FLOAT.create(Double.MAX_VALUE), is((float)Double.MAX_VALUE));
		assertThat(NumberFactories.TO_FLOAT.create(Double.MIN_VALUE), is((float)Double.MIN_VALUE));
		assertThat(NumberFactories.TO_FLOAT.create(Float.MAX_VALUE), is(Float.MAX_VALUE));
		assertThat(NumberFactories.TO_FLOAT.create(Float.MIN_VALUE), is(Float.MIN_VALUE));
		assertThat(NumberFactories.TO_FLOAT.create(0), is(0f));
		assertNull(NumberFactories.TO_FLOAT.create(null));
	}
	
}
