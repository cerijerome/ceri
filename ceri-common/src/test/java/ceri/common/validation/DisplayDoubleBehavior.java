package ceri.common.validation;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.util.BasicUtil;

public class DisplayDoubleBehavior {

	@Test
	public void shouldDisplayMultipleFormats() {
		assertEquals(DisplayDouble.format(1.5555, BasicUtil.enums(DisplayDouble.class)),
			"(1.5555, 2, 1.6, 1.56, 1.556)");
	}

}
