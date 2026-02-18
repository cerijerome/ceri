package ceri.serial.ftdi.test;

import java.io.IOException;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.array.Array;
import ceri.common.io.Direction;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.serial.ftdi.Ftdi;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_strings;

public class TestFtdiBehavior {

	@Test
	public void shouldEncapsulateParameters() {
		Testing.exerciseRecord(new TestFtdi.Submit(Direction.duplex, Pointer.NULL, 0));
		Testing.exerciseRecord(new TestFtdi.Stream((_, _) -> true, 0, 0, 0));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvidePair() throws IOException {
		var ftdis = TestFtdi.pairOf();
		ftdis[0].open();
		ftdis[1].open();
		ftdis[0].out().write(Array.BYTE.of(1, 2, 3));
		Assert.read(ftdis[1].in(), 1, 2, 3);
		ftdis[1].out().write(Array.BYTE.of(4, 5, 6));
		Assert.read(ftdis[0].in(), 4, 5, 6);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldEchoInputToPins() throws IOException {
		var ftdi = TestFtdi.ofPinEcho();
		ftdi.open();
		ftdi.out().write(0xa5);
		Assert.equal(ftdi.readPins(), 0xa5);
		ftdi.out().write(Array.BYTE.of());
		Assert.equal(ftdi.readPins(), 0xa5);
		ftdi.out().write(0xfde);
		Assert.equal(ftdi.readPins(), 0xde);
	}

	@Test
	public void shouldResetOpenState() throws IOException {
		try (var ftdi = TestFtdi.of()) {
			ftdi.open();
			ftdi.modem.autoResponses(0xaa);
			Assert.equal(ftdi.pollModemStatus(), 0xaa);
			ftdi.reset();
			Assert.thrown(ftdi::pollModemStatus); // not connected
		}
	}

	@Test
	public void shouldProvideDescriptor() throws IOException {
		try (var ftdi = TestFtdi.of()) {
			Assert.equal(ftdi.descriptor(), new ftdi_usb_strings("test", "test", "test"));
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
			Assert.equal(ftdi.latencyTimer(), 111);
			ftdi.readChunkSize(222);
			Assert.equal(ftdi.readChunkSize(), 222);
			ftdi.writeChunkSize(333);
			Assert.equal(ftdi.writeChunkSize(), 333);
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
			ftdi.submit.assertValues(new TestFtdi.Submit(Direction.in, m, 2),
				new TestFtdi.Submit(Direction.out, m, 3));
		}
	}

	@Test
	public void shouldReadStream() throws IOException {
		try (var ftdi = TestFtdi.of()) {
			ftdi.open();
			Ftdi.StreamCallback cb = (_, _) -> true;
			ftdi.readStream(cb, 2, 1);
			ftdi.stream.assertAuto(new TestFtdi.Stream(cb, 2, 1, 1.0));
		}
	}

	@Test
	public void shouldProvideErrorConfig() {
		Assert.thrown("generated", TestFtdi.errorConfig()::ftdi);
	}

	@Test
	public void shouldProvideSelfHealingConfig() throws IOException {
		try (var ftdi = TestFtdi.of(); var selfHealing = ftdi.selfHealingConfig().ftdi()) {
			selfHealing.open();
			ftdi.open.awaitAuto();
		}
	}
}
