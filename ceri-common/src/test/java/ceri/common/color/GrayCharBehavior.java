package ceri.common.color;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.awt.Color;
import org.junit.Test;

public class GrayCharBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		GrayChar t = GrayChar.of("@0QOo*. ");
		GrayChar eq0 = GrayChar.of("@0QOo*. ");
		GrayChar ne0 = GrayChar.of("@0QOo*.");
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0);
	}

	@Test
	public void shouldReturnCharacterForRatio() {
		GrayChar cg = GrayChar.of("@0QOo*. ");
		assertEquals(cg.charOf(-1.0), '@');
		assertEquals(cg.charOf(0.0), '@');
		assertEquals(cg.charOf(0.5), 'o');
		assertEquals(cg.charOf(0.874), '.');
		assertEquals(cg.charOf(0.999), ' ');
		assertEquals(cg.charOf(1.0), ' ');
		assertEquals(cg.charOf(2.0), ' ');
	}

	@Test
	public void shouldReturnCharacterForAdjustedColor() {
		GrayChar cg = GrayChar.of("@0QOo*. ");
		assertEquals(cg.charOf(Color.WHITE), ' ');
		assertEquals(cg.charOf(Color.BLACK), '@');
		assertEquals(cg.charOf(0x88bb44), '*');
		assertEquals(cg.charOf(0x4488bb), 'o');
		assertEquals(cg.charOf(0xbb4488), 'O');
	}

	@Test
	public void shouldReverse() {
		GrayChar cg = GrayChar.of("@0QOo*. ").reverse();
		assertEquals(cg.charOf(0.0), ' ');
		assertEquals(cg.charOf(0.5), 'O');
		assertEquals(cg.charOf(0.999), '@');
		assertEquals(cg.charOf(1.0), '@');
	}

}
