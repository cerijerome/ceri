package ceri.common.color;

import static ceri.common.color.ColorUtil.color;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class ColorxableTest {

	@Test
	public void testNullInstance() {
		Colorxable.NULL.colorx(Colorx.full);
		assertEquals(Colorxable.NULL.colorx(), Colorx.clear);
	}

	@Test
	public void testFromColorable() {
		Colorable c = new ColorableTest.TestColorable();
		Colorxable cx = Colorxable.from(c, color(0x800000), color(0x8000), color(0x80));
		cx.xargb(0x808080ff223344L);
		assertEquals(c.argb(), 0xff627384);
		c.argb(0xffaabbcc);
		assertEquals(cx.xargb(), 0xffffffff2a3b4cL);
	}

	@Test
	public void testOpaqueXargbAccess() {
		var cx = new TestColorxable();
		cx.xrgb(0x123456789abcdef0L);
		assertEquals(cx.xargb, 0x12345678ffbcdef0L);
		cx.xargb(0x123456789abcdef0L);
		assertEquals(cx.xrgb(), 0x12345678ffbcdef0L);
	}

	@Test
	public void testArgbAccess() {
		var cx = new TestColorxable();
		cx.argb(0x12345678);
		assertEquals(cx.xargb, 0x12345678L);
		cx.xargb(0x123456789abcdef0L);
		assertEquals(cx.argb(), 0x9abcdef0);
	}

	@Test
	public void testMultiSetColorxForEmptyCollection() {
		Colorxable cx = Colorxable.multi();
		cx.colorx(Colorx.full);
		cx.colorx(Colorx.clear);
	}

	@Test
	public void testMultiSetColorx() {
		Colorxable cx0 = new TestColorxable();
		Colorxable cx1 = new TestColorxable();
		Colorxable cx2 = new TestColorxable();
		Colorxable cx = Colorxable.multi(cx0, cx1, cx2);
		cx.colorx(Colorx.fullX012);
		assertEquals(cx0.colorx(), Colorx.fullX012);
		assertEquals(cx1.colorx(), Colorx.fullX012);
		assertEquals(cx2.colorx(), Colorx.fullX012);
	}

	@Test
	public void testMultiGetColorxForEmptyCollection() {
		Colorxable cx = Colorxable.multi();
		assertEquals(cx.xargb(), 0L);
	}

	@Test
	public void testMultiGetColorx() {
		Colorxable cx0 = new TestColorxable();
		Colorxable cx1 = new TestColorxable();
		Colorxable cx2 = new TestColorxable();
		Colorxable cx = Colorxable.multi(cx0, cx1, cx2);
		cx0.colorx(Colorx.fullX0);
		cx1.colorx(Colorx.black);
		cx2.xargb(0x123456789abcdefL);
		assertEquals(cx.colorx(), Colorx.fullX0);
	}

	static class TestColorxable implements Colorxable {
		public long xargb;

		@Override
		public long xargb() {
			return xargb;
		}

		@Override
		public void xargb(long xargb) {
			this.xargb = xargb;
		}
	}
}
