package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertRgb;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import java.awt.Color;
import org.junit.Test;

public class RgbColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		RgbColor c0 = RgbColor.of(0.4, 0.8, 0.6);
		RgbColor c1 = RgbColor.of(0.4, 0.8, 0.6);
		RgbColor c2 = RgbColor.of(0.5, 0.8, 0.6);
		RgbColor c3 = RgbColor.of(0.4, 0.7, 0.6);
		RgbColor c4 = RgbColor.of(0.4, 0.8, 0.5);
		RgbColor c5 = RgbColor.of(0.4, 0.8, 0.6, 0.9);
		exerciseEquals(c0, c1);
		assertAllNotEqual(c0, c2, c3, c4, c5);
		assertNotEquals(c5.toString(), c0.toString());
	}

	@Test
	public void shouldCreateFromRgbValues() {
		assertRgb(RgbColor.from(Color.yellow.getRGB()), 1.0, 1.0, 0.0);
	}

	@Test
	public void shouldCreateColorFromRatios() {
		assertColor(RgbColor.toColor(1.0, 0.0, 1.0), Color.magenta);
		assertColor(RgbColor.toColor(0.0, 1.0, 1.0, 0.5), Color.cyan, 128);
	}

	@Test
	public void shouldDim() {
		RgbColor c = RgbColor.of(0.6, 0.8, 0.4, 0.5);
		assertThat(c.dim(1), is(c));
		assertRgb(c.dim(0.5), 0.3, 0.4, 0.2, 0.5);
		assertRgb(c.dim(0), 0.0, 0.0, 0.0, 0.5);
	}

	@Test
	public void shouldNormalizeValues() {
		assertRgb(RgbColor.of(0.5, 0.6, 0.3, 0.2).normalize(), 0.5, 0.6, 0.3, 0.2);
		assertRgb(RgbColor.of(-0.2, 0.6, 0.3, 0.2).normalize(), 0.0, 0.8, 0.5, 0.2);
		assertRgb(RgbColor.of(2.0, 0.0, 0.0, 0.2).normalize(), 1.0, 0.0, 0.0, 0.2);
		assertRgb(RgbColor.of(0.0, 2.0, 0.0, 0.2).normalize(), 0.0, 1.0, 0.0, 0.2);
		assertRgb(RgbColor.of(0.0, 0.0, 2.0, 0.2).normalize(), 0.0, 0.0, 1.0, 0.2);
		assertRgb(RgbColor.of(0.5, 0.6, 0.3, 1.2).normalize(), 0.5, 0.6, 0.3, 1.0);
	}

	@Test
	public void shouldLimitValues() {
		assertRgb(RgbColor.of(0.5, 0.6, 0.3, 0.2).limit(), 0.5, 0.6, 0.3, 0.2);
		assertRgb(RgbColor.of(1.1, 0.6, 0.3, 0.2).limit(), 1.0, 0.6, 0.3, 0.2);
		assertRgb(RgbColor.of(0.5, 1.1, 0.3, 0.2).limit(), 0.5, 1.0, 0.3, 0.2);
		assertRgb(RgbColor.of(0.5, 0.6, -0.1, 0.2).limit(), 0.5, 0.6, 0.0, 0.2);
		assertRgb(RgbColor.of(0.5, 0.6, 0.3, 5.0).limit(), 0.5, 0.6, 0.3, 1.0);
	}

	@Test
	public void shouldVerifyValues() {
		RgbColor.of(0.5, 0.6, 0.3, 0.2).verify();
		assertException(() -> RgbColor.of(1.1, 0.6, 0.3, 0.2).verify());
		assertException(() -> RgbColor.of(0.5, 1.1, 0.3, 0.2).verify());
		assertException(() -> RgbColor.of(0.5, 0.6, -0.1, 0.2).verify());
		assertException(() -> RgbColor.of(0.5, 0.6, 0.3, 5.0).verify());
	}

}
