package ceri.serial.ftdi.test;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.io.Direction;
import ceri.serial.ftdi.Ftdi;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_strings;

public class TestFtdiBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldProvidePair() throws IOException {
		var ftdis = TestFtdi.pairOf();
		ftdis[0].open();
		ftdis[1].open();
		ftdis[0].out().write(bytes(1, 2, 3));
		assertRead(ftdis[1].in(), 1, 2, 3);
		ftdis[1].out().write(bytes(4, 5, 6));
		assertRead(ftdis[0].in(), 4, 5, 6);
	}
	
	@SuppressWarnings("resource")
	@Test
	public void shouldEchoInputToPins() throws IOException {
		var ftdi = TestFtdi.ofPinEcho();
		ftdi.open();
		ftdi.out().write(0xa5);
		assertEquals(ftdi.readPins(), 0xa5);
		ftdi.out().write(bytes());
		assertEquals(ftdi.readPins(), 0xa5);
		ftdi.out().write(0xfde);
		assertEquals(ftdi.readPins(), 0xde);
	}
	
	@Test
	public void shouldResetOpenState() throws IOException {
		try (var ftdi = TestFtdi.of()) {
			ftdi.open();
			ftdi.modem.autoResponses(0xaa);
			assertEquals(ftdi.pollModemStatus(), 0xaa);
			ftdi.reset();
			assertThrown(ftdi::pollModemStatus); // not connected
		}
	}

	@Test
	public void shouldProvideDescriptor() throws IOException {
		try (var ftdi = TestFtdi.of()) {
			assertEquals(ftdi.descriptor(), new ftdi_usb_strings("test", "test", "test"));
			ftdi.descriptor.assertCalls(1);
		}
	}
	
	@Test
	public void shouldResetUsb() throws IOException {
		try (var ftdi = TestFtdi.of()) {
			ftdi.usbReset();
			ftdi.usbReset.assertCalls(1);
		}
	}
	
	@Test
	public void shouldSetDtrAndRts() throws IOException {
		try (var ftdi = TestFtdi.of()) {
			ftdi.open();
			ftdi.dtr(true);
			ftdi.rts(false);
			ftdi.dtr.assertAuto(true);
			ftdi.rts.assertAuto(false);
		}
	}
	
	@Test
	public void shouldConfigureFtdi() throws IOException {
		try (var ftdi = TestFtdi.of()) {
			ftdi.latencyTimer(111);
			assertEquals(ftdi.latencyTimer(), 111);
			ftdi.readChunkSize(222);
			assertEquals(ftdi.readChunkSize(), 222);
			ftdi.writeChunkSize(333);
			assertEquals(ftdi.writeChunkSize(), 333);
		}
	}
	
	@Test
	public void shouldPurgeBuffers() throws IOException {
		try (var ftdi = TestFtdi.of()) {
			ftdi.purgeReadBuffer();
			ftdi.purgeBuffer.assertCalls(1);
			ftdi.purgeWriteBuffer();
			ftdi.purgeBuffer.assertCalls(2);
		}
	}
	
	@Test
	public void shouldSubmitTransfer() throws IOException {
		try (var ftdi = TestFtdi.of(); var m = new Memory(3)) {
			ftdi.open();
			ftdi.readSubmit(m, 2);
			ftdi.writeSubmit(m, 3);
			ftdi.submit.assertValues(List.of(Direction.in, m, 2), List.of(Direction.out, m, 3));
		}
	}
	
	@Test
	public void shouldReadStream() throws IOException {
		try (var ftdi = TestFtdi.of()) {
			ftdi.open();
			Ftdi.StreamCallback cb = (i, b) -> true;
			ftdi.readStream(cb, 2, 1);
			ftdi.stream.assertAuto(List.of(cb, 2, 1, 1.0));
		}
	}
	
}
