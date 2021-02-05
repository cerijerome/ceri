package ceri.common.color;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.awt.Color;
import org.junit.Test;

public class CharGrayscaleBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		CharGrayscale t = CharGrayscale.of("@0QOo*. ");
		CharGrayscale eq0 = CharGrayscale.of("@0QOo*. ");
		CharGrayscale ne0 = CharGrayscale.of("@0QOo*.");
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0);
	}

	@Test
	public void shouldReturnCharacterForRatio() {
		CharGrayscale cg = CharGrayscale.of("@0QOo*. ");
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
		CharGrayscale cg = CharGrayscale.of("@0QOo*. ");
		assertEquals(cg.charOf(Color.WHITE, 0, 0, 0), '@');
		assertEquals(cg.charOf(Color.BLACK, 1, 1, 1), '@');
		assertEquals(cg.charOf(new Color(0x88bb44), 0.5, 0.8, 0.2), 'o');
		assertEquals(cg.charOf(new Color(0x4488bb), 0.5, 0.8, 0.2), 'O');
		assertEquals(cg.charOf(new Color(0x4488bb), 1.0, 1.6, 0.4), 'O');
	}

	@Test
	public void shouldReverse() {
		CharGrayscale cg = CharGrayscale.of("@0QOo*. ").reverse();
		assertEquals(cg.charOf(0.0), ' ');
		assertEquals(cg.charOf(0.5), 'O');
		assertEquals(cg.charOf(0.999), '@');
		assertEquals(cg.charOf(1.0), '@');
	}

}
