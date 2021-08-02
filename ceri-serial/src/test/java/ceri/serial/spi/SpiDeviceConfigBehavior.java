package ceri.serial.spi;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.util.Enclosed;
import ceri.serial.clib.jna.TestCLibNative;
import ceri.serial.spi.Spi.Direction;
import ceri.serial.spi.jna.TestSpiCLibNative;

public class SpiDeviceConfigBehavior {
	private TestSpiCLibNative lib;
	private Enclosed<RuntimeException, ?> enc;

	@Before
	public void before() {
		lib = TestSpiCLibNative.of();
		enc = TestCLibNative.register(lib);
	}

	@After
	public void after() {
		enc.close();
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		SpiDeviceConfig t = SpiDeviceConfig.of(1, 1);
		SpiDeviceConfig eq0 = SpiDeviceConfig.of(1, 1);
		SpiDeviceConfig eq1 = SpiDeviceConfig.builder().bus(1).chip(1).build();
		SpiDeviceConfig ne0 = SpiDeviceConfig.of(0, 1);
		SpiDeviceConfig ne1 = SpiDeviceConfig.of(1, 0);
		SpiDeviceConfig ne2 = SpiDeviceConfig.of(1, 1, Direction.in);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldOpenDevice() throws IOException {
		assertThrown(() -> SpiDeviceConfig.of(0, 0, null));
		try (var fd = SpiDeviceConfig.of(0, 1, Direction.in).open()) {}
		try (var fd = SpiDeviceConfig.of(1, 1, Direction.out).open()) {}
		try (var fd = SpiDeviceConfig.of(1, 0).open()) {}
		lib.open.assertValues( //
			List.of("/dev/spidev0.1", 0, 0), //
			List.of("/dev/spidev1.1", 1, 0), //
			List.of("/dev/spidev1.0", 2, 0));
	}

}
