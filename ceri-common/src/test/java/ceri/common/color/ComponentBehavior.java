package ceri.common.color;

import org.junit.Test;
import ceri.common.test.Assert;

public class ComponentBehavior {

	@Test
	public void testFromByteIndex() {
		Assert.equal(Component.from(-1), null);
		Assert.equal(Component.from(0), Component.b);
		Assert.equal(Component.from(1), Component.g);
		Assert.equal(Component.from(2), Component.r);
		Assert.equal(Component.from(3), Component.a);
		Assert.equal(Component.from(4), Component.x0);
		Assert.equal(Component.from(5), Component.x1);
		Assert.equal(Component.from(6), Component.x2);
		Assert.equal(Component.from(7), Component.x3);
		Assert.equal(Component.from(8), null);
	}

	@Test
	public void testComponentCount() {
		Assert.equal(Component.count(0L), 0);
		Assert.equal(Component.count(0x1), 1);
		Assert.equal(Component.count(0xf00000000L), 5);
		Assert.equal(Component.count(0xff000000000000L), 7);
		Assert.equal(Component.count(-1L), 8);
	}

	@Test
	public void testGetXByIndex() {
		Assert.equal(Component.x(0), Component.x0);
		Assert.equal(Component.x(3), Component.x3);
		Assert.equal(Component.x(-1), null);
		Assert.equal(Component.x(4), null);
	}

	@Test
	public void testGetAll() {
		Assert.array(Component.getAll(0x87654321, Component.g, Component.a), 0x43, 0x87);
		Assert.array(Component.getAll(0x87654321, Component.r, Component.x0), 0x65, 0);
	}

	@Test
	public void testLimit() {
		Assert.equal(Component.limit(0), 0);
		Assert.equal(Component.limit(100), 100);
		Assert.equal(Component.limit(Colors.MAX_VALUE), Colors.MAX_VALUE);
		Assert.equal(Component.limit(-1), 0);
		Assert.equal(Component.limit(Colors.MAX_VALUE + 1), Colors.MAX_VALUE);
		Assert.equal(Component.limit(Integer.MAX_VALUE), Colors.MAX_VALUE);
		Assert.equal(Component.limit(Integer.MIN_VALUE), 0);
	}

	@Test
	public void shouldProvideIntValueFromRatio() {
		Assert.equal(Component.a.intValue(0.5), 0x80000000);
		Assert.equal(Component.x0.intValue(0.5), 0);
	}

	@Test
	public void shouldProvideLongValueFromRatio() {
		Assert.equal(Component.a.longValue(0.5), 0x80000000L);
		Assert.equal(Component.x3.longValue(0.5), 0x8000000000000000L);
	}

	@Test
	public void shouldProvideRatioFromValue() {
		Assert.approx(Component.a.ratio(0xfedcba9876543210L), 0.463);
		Assert.approx(Component.x2.ratio(0xfedcba9876543210L), 0.863);
	}

}
