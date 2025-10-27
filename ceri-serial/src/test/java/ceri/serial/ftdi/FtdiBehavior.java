package ceri.serial.ftdi;

import java.io.IOException;
import java.time.Duration;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.test.Assert;

public class FtdiBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		Assert.equal(Ftdi.NULL.descriptor().manufacturer(), "");
		Assert.equal(Ftdi.NULL.descriptor().description(), "");
		Assert.equal(Ftdi.NULL.descriptor().serial(), "");
		Ftdi.NULL.usbReset();
		Ftdi.NULL.bitMode(FtdiBitMode.OFF);
		Ftdi.NULL.baud(12345);
		Ftdi.NULL.line(FtdiLineParams.DEFAULT);
		Ftdi.NULL.flowControl(FtdiFlowControl.disabled);
		Ftdi.NULL.dtr(true);
		Ftdi.NULL.rts(false);
		Assert.equal(Ftdi.NULL.readPins(), 0);
		Assert.equal(Ftdi.NULL.pollModemStatus(), 0);
		Ftdi.NULL.latencyTimer(111);
		Assert.equal(Ftdi.NULL.latencyTimer(), 0);
		Ftdi.NULL.readChunkSize(222);
		Assert.equal(Ftdi.NULL.readChunkSize(), 0);
		Ftdi.NULL.writeChunkSize(333);
		Assert.equal(Ftdi.NULL.writeChunkSize(), 0);
		Ftdi.NULL.purgeBuffers();
		@SuppressWarnings("resource")
		var m = new Memory(3);
		Ftdi.NULL.readSubmit(m, 3).dataCancel(Duration.ZERO);
		Assert.equal(Ftdi.NULL.readSubmit(m, 3).dataDone(), 0);
		Assert.equal(Ftdi.NULL.writeSubmit(m, 3).dataDone(), 0);
		Ftdi.NULL.readStream((_, _) -> true, 1, 1);
		Assert.find(Ftdi.NULL, ".*NULL$");
	}
}
