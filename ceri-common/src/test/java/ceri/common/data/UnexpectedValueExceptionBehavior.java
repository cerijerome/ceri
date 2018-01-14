package ceri.common.data;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class UnexpectedValueExceptionBehavior {

	@Test
	public void shouldFormatHexValuesCreateWithValues() {
		assertThat(UnexpectedValueException.forByte(0xffff).getMessage(), containsString("0xff"));
		assertThat(UnexpectedValueException.forByte(0xffff, 0xeee).getMessage(),
			containsString("0xee"));
		assertThat(UnexpectedValueException.forShort(0xffff).getMessage(),
			containsString("0xffff"));
		assertThat(UnexpectedValueException.forShort(0xffff, 0xeee).getMessage(),
			containsString("0xeee"));
		assertThat(UnexpectedValueException.forInt(0xffff).getMessage(), containsString("0xffff"));
		assertThat(UnexpectedValueException.forInt(0xffff, 0xeee).getMessage(),
			containsString("0xeee"));
	}

}
