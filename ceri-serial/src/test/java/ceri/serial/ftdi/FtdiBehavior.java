package ceri.serial.ftdi;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.provider;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type.BREAK_ON;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type.BITS_7;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type.ODD;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type.STOP_BIT_2;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteProvider;
import ceri.common.util.Enclosed;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbSampleData;
import ceri.serial.libusb.jna.TestLibUsbNative;

public class FtdiBehavior {
	private TestLibUsbNative lib;
	private Enclosed<TestLibUsbNative> enc;
	private Ftdi ftdi;

	@Before
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.subject;
		LibUsbSampleData.populate(lib.data);
		ftdi = Ftdi.open();
	}

	@After
	public void after() {
		ftdi.close();
		enc.close();
	}

	@Test
	public void shouldFailIfClosed() {
		ftdi.close();
		assertThrown(() -> ftdi.read());
		lib.controlTransferOut.assertValues( // open() leftovers:
			List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()), // ftdi_usb_reset
			List.of(0x40, 0x03, 0x4138, 0, ByteProvider.empty())); // ftdi_set_baudrate 9600
		lib.controlTransferIn.assertNoCall();
	}

	@Test
	public void shouldConfigureFtdi() throws LibUsbException {
		lib.controlTransferOut.reset(); // clear original open()
		ftdi.usbReset();
		ftdi.bitBang(false);
		ftdi.bitBang(true);
		ftdi.baudRate(250000);
		ftdi.lineParams(FtdiLineParams.builder().dataBits(BITS_7).stopBits(STOP_BIT_2).parity(ODD)
			.breakType(BREAK_ON).build());
		lib.controlTransferOut.assertValues(List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()),
			List.of(0x40, 0x0b, 0, 1, ByteProvider.empty()),
			List.of(0x40, 0x0b, 0x01ff, 1, ByteProvider.empty()),
			List.of(0x40, 0x03, 0x3, 0, ByteProvider.empty()),
			List.of(0x40, 0x04, 0x5107, 1, ByteProvider.empty()));
		lib.controlTransferIn.assertNoCall();
	}

	@Test
	public void shouldControlDtrRts() throws LibUsbException {
		lib.controlTransferOut.reset(); // clear original open()
		ftdi.rts(true);
		ftdi.dtr(true);
		ftdi.dtrRts(false, false);
		lib.controlTransferOut.assertValues( //
			List.of(0x40, 0x01, 0x202, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x101, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x300, 1, ByteProvider.empty()));
	}

	@Test
	public void shouldWriteData() throws LibUsbException {
		ftdi.write(1, 2, 3);
		ftdi.write(ByteBuffer.wrap(ArrayUtil.bytes(4, 5, 6)));
		lib.bulkTransferOut.assertValues( //
			List.of(0x02, provider(1, 2, 3)), //
			List.of(0x02, provider(4, 5, 6)));
	}

	@Test
	public void shouldReadData() throws LibUsbException {
		lib.bulkTransferIn.autoResponses( //
			provider(0, 0, 0xab), // read() => 2B status + 1B data
			provider(0, 0), // read() => 2B status + 0B data
			provider(0, 0, 1, 2, 3), // read(3) => 2B status + 3B data
			provider(0, 0, 4, 5), provider(), // read(3) => 2B status + 2B data
			provider(0, 0, 6, 7), provider()); // read(buffer[3]) => 2B status + 2B data
		assertEquals(ftdi.read(), 0xab);
		assertEquals(ftdi.read(), -1);
		assertArray(ftdi.read(3), 1, 2, 3);
		assertArray(ftdi.read(3), 4, 5);
		ByteBuffer bb = ByteBuffer.allocate(3);
		assertEquals(ftdi.read(bb), 2);
		assertArray(bb.array(), 6, 7, 0);
		lib.bulkTransferIn.assertValues( //
			List.of(0x81, 3), //
			List.of(0x81, 3), //
			List.of(0x81, 5), //
			List.of(0x81, 5), List.of(0x81, 3), //
			List.of(0x81, 5), List.of(0x81, 3));
	}

	@Test
	public void shouldWriteAsync() throws LibUsbException {
		var control = ftdi.writeSubmit(1, 2, 3, 4, 5);
		assertEquals(control.dataDone(), 5);
	}

}
