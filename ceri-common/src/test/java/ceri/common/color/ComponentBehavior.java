package ceri.common.color;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class ComponentBehavior {

	@Test
	public void testFromByteIndex() {
		assertEquals(Component.from(-1), null);
		assertEquals(Component.from(0), Component.b);
		assertEquals(Component.from(1), Component.g);
		assertEquals(Component.from(2), Component.r);
		assertEquals(Component.from(3), Component.a);
		assertEquals(Component.from(4), Component.x0);
		assertEquals(Component.from(5), Component.x1);
		assertEquals(Component.from(6), Component.x2);
		assertEquals(Component.from(7), Component.x3);
		assertEquals(Component.from(8), null);
	}

	@Test
	public void testComponentCount() {
		assertEquals(Component.count(0L), 0);
		assertEquals(Component.count(0x1), 1);
		assertEquals(Component.count(0xf00000000L), 5);
		assertEquals(Component.count(0xff000000000000L), 7);
		assertEquals(Component.count(-1L), 8);
	}

	@Test
	public void testGetXByIndex() {
		assertEquals(Component.x(0), Component.x0);
		assertEquals(Component.x(3), Component.x3);
		assertEquals(Component.x(-1), null);
		assertEquals(Component.x(4), null);
	}

	@Test
	public void testGetAll() {
		assertArray(Component.getAll(0x87654321, Component.g, Component.a), 0x43, 0x87);
		assertArray(Component.getAll(0x87654321, Component.r, Component.x0), 0x65, 0);
	}

	@Test
	public void testLimit() {
		assertEquals(Component.limit(0), 0);
		assertEquals(Component.limit(100), 100);
		assertEquals(Component.limit(Colors.MAX_VALUE), Colors.MAX_VALUE);
		assertEquals(Component.limit(-1), 0);
		assertEquals(Component.limit(Colors.MAX_VALUE + 1), Colors.MAX_VALUE);
		assertEquals(Component.limit(Integer.MAX_VALUE), Colors.MAX_VALUE);
		assertEquals(Component.limit(Integer.MIN_VALUE), 0);
	}

	@Test
	public void shouldProvideIntValueFromRatio() {
		assertEquals(Component.a.intValue(0.5), 0x80000000);
		assertEquals(Component.x0.intValue(0.5), 0);
	}

	@Test
	public void shouldProvideLongValueFromRatio() {
		assertEquals(Component.a.longValue(0.5), 0x80000000L);
		assertEquals(Component.x3.longValue(0.5), 0x8000000000000000L);
	}

	@Test
	public void shouldProvideRatioFromValue() {
		assertApprox(Component.a.ratio(0xfedcba9876543210L), 0.463);
		assertApprox(Component.x2.ratio(0xfedcba9876543210L), 0.863);
	}

}
