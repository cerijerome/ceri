package ceri.common.color;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
		assertThat(cg.charOf(-1.0), is('@'));
		assertThat(cg.charOf(0.0), is('@'));
		assertThat(cg.charOf(0.5), is('o'));
		assertThat(cg.charOf(0.874), is('.'));
		assertThat(cg.charOf(0.999), is(' '));
		assertThat(cg.charOf(1.0), is(' '));
		assertThat(cg.charOf(2.0), is(' '));
	}

	@Test
	public void shouldReverse() {
		CharGrayscale cg = CharGrayscale.of("@0QOo*. ").reverse();
		assertThat(cg.charOf(0.0), is(' '));
		assertThat(cg.charOf(0.5), is('O'));
		assertThat(cg.charOf(0.999), is('@'));
		assertThat(cg.charOf(1.0), is('@'));
	}

}
