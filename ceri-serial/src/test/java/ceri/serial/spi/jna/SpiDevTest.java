package ceri.serial.spi.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.util.Enclosed;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.test.TestCLibNative;
import ceri.serial.spi.SpiDevice;

public class SpiDevTest {
	private TestSpiCLibNative lib;
	private Enclosed<RuntimeException, ?> enc;
	private FileDescriptor fd;

	@Before
	public void before() throws IOException {
		lib = TestSpiCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = SpiDevice.Config.of(0, 0).open();
	}

	@After
	public void after() throws IOException {
		fd.close();
		enc.close();
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(SpiDev.class);
	}

	@Test
	public void testGetMode() throws IOException {
		lib.ioctlSpiInt.autoResponses(0xde);
		fd.accept(f -> assertEquals(SpiDev.getMode(f), 0xde));
	}

	@Test
	public void testMacros() {
		SpiDev.SPI_IOC_MESSAGE(0); // no error
		SpiDev.SPI_IOC_MESSAGE(1 << 15); // no error
	}

}
