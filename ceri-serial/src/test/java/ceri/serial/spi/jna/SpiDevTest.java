package ceri.serial.spi.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.util.Enclosed;
import ceri.serial.clib.CFileDescriptor;
import ceri.serial.clib.jna.TestCLibNative;
import ceri.serial.spi.SpiDeviceConfig;

public class SpiDevTest {
	private TestSpiCLibNative lib;
	private Enclosed<?> enc;
	private CFileDescriptor f;
	private int fd;

	@Before
	public void before() throws IOException {
		lib = TestSpiCLibNative.of();
		enc = TestCLibNative.register(lib);
		f = SpiDeviceConfig.of(0, 0).open();
		fd = f.fd();
	}

	@After
	public void after() throws IOException {
		f.close();
		enc.close();
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(SpiDev.class);
	}

	@Test
	public void testGetMode() throws IOException {
		lib.ioctlSpiInt.autoResponses(0xde);
		assertEquals(SpiDev.getMode(fd), 0xde);
	}

	@Test
	public void testMacros() {
		SpiDev.SPI_IOC_MESSAGE(0); // no error
		SpiDev.SPI_IOC_MESSAGE(1 << 15); // no error
	}

}
