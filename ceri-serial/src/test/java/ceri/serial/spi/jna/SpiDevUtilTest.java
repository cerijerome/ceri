package ceri.serial.spi.jna;

import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.io.Direction;
import ceri.common.test.Assert;
import ceri.jna.util.GcMemory;

public class SpiDevUtilTest {

	@Test
	public void testDirectionFromTransfer() {
		var m = GcMemory.malloc(1);
		var xfer = new SpiDev.spi_ioc_transfer();
		Assert.equal(SpiDevUtil.direction(xfer), Direction.out);
		xfer.len = 1;
		Assert.thrown(() -> SpiDevUtil.direction(xfer));
		xfer.rx_buf = Pointer.nativeValue(m.m);
		Assert.equal(SpiDevUtil.direction(xfer), Direction.in);
		xfer.tx_buf = Pointer.nativeValue(m.m);
		Assert.equal(SpiDevUtil.direction(xfer), Direction.duplex);
	}

	@Test
	public void testTransferTime() {
		var xfer = new SpiDev.spi_ioc_transfer();
		xfer.len = 10;
		xfer.delay_usecs = 33;
		Assert.equal(SpiDevUtil.transferTimeMicros(xfer, 0), 33L);
		Assert.equal(SpiDevUtil.transferTimeMicros(xfer, 1000), 80033L);
		xfer.speed_hz = 2000;
		Assert.equal(SpiDevUtil.transferTimeMicros(xfer, 1000), 40033L);
	}
}
