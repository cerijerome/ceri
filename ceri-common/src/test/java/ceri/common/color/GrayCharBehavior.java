package ceri.common.color;

import java.awt.Color;
import org.junit.Test;
import ceri.common.test.Assert;

public class GrayCharBehavior {

	@Test
	public void shouldReturnCharacterForRatio() {
		var cg = new GrayChar("@0QOo*. ");
		Assert.equal(cg.charOf(-1.0), '@');
		Assert.equal(cg.charOf(0.0), '@');
		Assert.equal(cg.charOf(0.5), 'o');
		Assert.equal(cg.charOf(0.874), '.');
		Assert.equal(cg.charOf(0.999), ' ');
		Assert.equal(cg.charOf(1.0), ' ');
		Assert.equal(cg.charOf(2.0), ' ');
	}

	@Test
	public void shouldReturnCharacterForAdjustedColor() {
		var cg = new GrayChar("@0QOo*. ");
		Assert.equal(cg.charOf(Color.WHITE), ' ');
		Assert.equal(cg.charOf(Color.BLACK), '@');
		Assert.equal(cg.charOf(0x88bb44), '*');
		Assert.equal(cg.charOf(0x4488bb), 'o');
		Assert.equal(cg.charOf(0xbb4488), 'O');
	}

	@Test
	public void shouldReverse() {
		var cg = new GrayChar("@0QOo*. ").reverse();
		Assert.equal(cg.charOf(0.0), ' ');
		Assert.equal(cg.charOf(0.5), 'O');
		Assert.equal(cg.charOf(0.999), '@');
		Assert.equal(cg.charOf(1.0), '@');
	}

}
