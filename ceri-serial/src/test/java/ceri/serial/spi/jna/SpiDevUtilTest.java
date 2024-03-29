package ceri.serial.spi.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.io.Direction;
import ceri.jna.util.GcMemory;

public class SpiDevUtilTest {

	@Test
	public void testDirectionFromTransfer() {
		var m = GcMemory.malloc(1);
		var xfer = new SpiDev.spi_ioc_transfer();
		assertEquals(SpiDevUtil.direction(xfer), Direction.out);
		xfer.len = 1;
		assertThrown(() -> SpiDevUtil.direction(xfer));
		xfer.rx_buf = Pointer.nativeValue(m.m);
		assertEquals(SpiDevUtil.direction(xfer), Direction.in);
		xfer.tx_buf = Pointer.nativeValue(m.m);
		assertEquals(SpiDevUtil.direction(xfer), Direction.duplex);
	}

	@Test
	public void testTransferTime() {
		var xfer = new SpiDev.spi_ioc_transfer();
		xfer.len = 10;
		xfer.delay_usecs = 33;
		assertEquals(SpiDevUtil.transferTimeMicros(xfer, 0), 33L);
		assertEquals(SpiDevUtil.transferTimeMicros(xfer, 1000), 80033L);
		xfer.speed_hz = 2000;
		assertEquals(SpiDevUtil.transferTimeMicros(xfer, 1000), 40033L);
	}

}
