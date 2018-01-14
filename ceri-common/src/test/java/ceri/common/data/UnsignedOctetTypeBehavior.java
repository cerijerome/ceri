package ceri.common.data;

import static ceri.common.test.TestUtil.exerciseEnum;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class UnsignedOctetTypeBehavior {

	@Test
	public void shouldFormatUnsignedValues() {
		exerciseEnum(UnsignedOctetType.class);
		assertThat(UnsignedOctetType._byte.format(0x7fffe), is("0xfe"));
		assertThat(UnsignedOctetType._short.format(0x7fffe), is("0xfffe"));
		assertThat(UnsignedOctetType._int.format(0x777fffffeL), is("0x77fffffe"));
		assertThat(UnsignedOctetType._long.format(0x7fffffffeL), is("0x00000007fffffffe"));
	}

}
