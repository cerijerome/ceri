package ceri.serial.ftdi;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import java.io.IOException;
import java.time.Duration;
import org.junit.Test;
import com.sun.jna.Memory;

public class FtdiBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		assertEquals(Ftdi.NULL.descriptor().manufacturer(), "");
		assertEquals(Ftdi.NULL.descriptor().description(), "");
		assertEquals(Ftdi.NULL.descriptor().serial(), "");
		Ftdi.NULL.usbReset();
		Ftdi.NULL.bitMode(FtdiBitMode.OFF);
		Ftdi.NULL.baud(12345);
		Ftdi.NULL.line(FtdiLineParams.DEFAULT);
		Ftdi.NULL.flowControl(FtdiFlowControl.disabled);
		Ftdi.NULL.dtr(true);
		Ftdi.NULL.rts(false);
		assertEquals(Ftdi.NULL.readPins(), 0);
		assertEquals(Ftdi.NULL.pollModemStatus(), 0);
		Ftdi.NULL.latencyTimer(111);
		assertEquals(Ftdi.NULL.latencyTimer(), 0);
		Ftdi.NULL.readChunkSize(222);
		assertEquals(Ftdi.NULL.readChunkSize(), 0);
		Ftdi.NULL.writeChunkSize(333);
		assertEquals(Ftdi.NULL.writeChunkSize(), 0);
		Ftdi.NULL.purgeBuffers();
		@SuppressWarnings("resource")
		var m = new Memory(3);
		Ftdi.NULL.readSubmit(m, 3).dataCancel(Duration.ZERO);
		assertEquals(Ftdi.NULL.readSubmit(m, 3).dataDone(), 0);
		assertEquals(Ftdi.NULL.writeSubmit(m, 3).dataDone(), 0);
		Ftdi.NULL.readStream((_, _) -> true, 1, 1);
		assertFind(Ftdi.NULL, ".*NULL$");
	}
}
