package ceri.common.io;

import static ceri.common.test.TestUtil.exerciseEnum;
import org.junit.Test;

public class DeviceModeBehavior {

	@Test
	public void testEnum() {
		exerciseEnum(DeviceMode.class);
	}

}
