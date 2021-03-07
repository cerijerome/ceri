package ceri.common.color;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.util.Comparator;
import org.junit.Test;

public class ColorxComparatorsTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ColorxComparators.class);
	}

	@Test
	public void testXargbComparator() {
		exerciseComparator(ColorxComparators.XARGB, 0x8000ff204060L, 0x40ff204060L,
			0x008000ff204060L, 0x100000ff204060L);
	}

	@Test
	public void testXrgbComparator() {
		exerciseComparator(ColorxComparators.XRGB, 0x8000ff204060L, 0x40ff204060L,
			0x0080007f204060L, 0x100000ff204060L);
	}

	@Test
	public void testAlphaComparator() {
		exerciseComparator(ColorxComparators.ALPHA, 0x800080204060L, 0xffff40204060L, 0x80ffffffL,
			0xcc000000L);
	}

	@Test
	public void testRedComparator() {
		exerciseComparator(ColorxComparators.RED, 0x800080204060L, 0xffff40104060L, 0x8020ffffL,
			0xcc400000L);
	}

	@Test
	public void testGreenComparator() {
		exerciseComparator(ColorxComparators.GREEN, 0x800080204060L, 0xffff40202060L, 0x80ff40ffL,
			0xcc005000L);
	}

	@Test
	public void testBlueComparator() {
		exerciseComparator(ColorxComparators.BLUE, 0x800080204060L, 0xffff40204050L, 0x80ffff60L,
			0xcc0000ffL);
	}

	@Test
	public void testXComparator() {
		exerciseComparator(ColorxComparators.x(0), 0x807080204060L, 0xff6040204050L, 0x7080ffff60L,
			0x80cc0000ffL);
	}

	@Test
	public void testColorComparator() {
		exerciseComparator(ColorxComparators.color(ColorComparators.HUE), 0xffff808060L,
			0xffffffff800000L, 0xffffffffff00L, 0xffff0060ffL);
	}

	private static void exerciseComparator(Comparator<Colorx> comparator, long cx, long lt, long eq,
		long gt) {
		Colorx cx0 = Colorx.of(cx);
		assertEquals(comparator.compare(cx0, Colorx.of(lt)), 1, "Compare");
		assertEquals(comparator.compare(cx0, Colorx.of(eq)), 0, "Compare");
		assertEquals(comparator.compare(cx0, Colorx.of(gt)), -1, "Compare");
	}

}
