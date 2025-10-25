package ceri.common.color;

import static ceri.common.test.Assert.assertEquals;
import java.awt.Color;
import org.junit.Test;

public class GrayCharBehavior {

	@Test
	public void shouldReturnCharacterForRatio() {
		var cg = new GrayChar("@0QOo*. ");
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
		var cg = new GrayChar("@0QOo*. ");
		assertEquals(cg.charOf(Color.WHITE), ' ');
		assertEquals(cg.charOf(Color.BLACK), '@');
		assertEquals(cg.charOf(0x88bb44), '*');
		assertEquals(cg.charOf(0x4488bb), 'o');
		assertEquals(cg.charOf(0xbb4488), 'O');
	}

	@Test
	public void shouldReverse() {
		var cg = new GrayChar("@0QOo*. ").reverse();
		assertEquals(cg.charOf(0.0), ' ');
		assertEquals(cg.charOf(0.5), 'O');
		assertEquals(cg.charOf(0.999), '@');
		assertEquals(cg.charOf(1.0), '@');
	}

}
