package ceri.serial.spi.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.util.CloseableUtil;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.util.JnaLibrary;
import ceri.serial.spi.SpiDevice;

public class SpiDevTest {
	private final JnaLibrary.Ref<TestSpiCLibNative> ref = TestSpiCLibNative.spiRef();
	private FileDescriptor fd;

	@After
	public void after() {
		CloseableUtil.close(fd, ref);
		fd = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(SpiDev.class);
	}

	@Test
	public void testGetMode() throws IOException {
		var lib = initSpi();
		lib.ioctlSpiInt.autoResponses(0xde);
		fd.accept(f -> assertEquals(SpiDev.getMode(f), 0xde));
	}

	@Test
	public void testMacros() {
		SpiDev.SPI_IOC_MESSAGE(0); // no error
		SpiDev.SPI_IOC_MESSAGE(1 << 15); // no error
	}

	private TestSpiCLibNative initSpi() throws IOException {
		ref.init();
		fd = SpiDevice.Config.of(0, 0).open();
		return ref.get();
	}
}
