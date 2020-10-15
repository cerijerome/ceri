package ceri.common.validation;

import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.util.BasicUtil;

public class DisplayDoubleBehavior {

	@Test
	public void shouldDisplayMultipleFormats() {
		assertThat(DisplayDouble.format(1.5555, BasicUtil.enums(DisplayDouble.class)),
			is("(1.5555, 2, 1.6, 1.56, 1.556)"));
	}

}
