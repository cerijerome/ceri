package ceri.serial.ftdi.util;

import static ceri.common.function.FunctionUtil.execSilently;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.RIX;
import static ceri.common.test.ErrorGen.RTX;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_MEM;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_PIPE;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sun.jna.LastErrorException;
import ceri.common.concurrent.ValueCondition;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.common.util.Enclosed;
import ceri.log.test.LogModifier;
import ceri.serial.ftdi.FlowControl;
import ceri.serial.ftdi.Ftdi;
import ceri.serial.ftdi.FtdiBitmode;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.libusb.jna.LibUsbExampleData;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.TestLibUsbNative;

public class SelfHealingFtdiConnectorBehavior {
	private static final SelfHealingFtdiConfig config =
		SelfHealingFtdiConfig.builder().recoveryDelayMs(1).fixRetryDelayMs(1).find("0x403").build();
	private TestLibUsbNative lib;
	private Enclosed<TestLibUsbNative> enc;
	private Ftdi ftdi;
	private SelfHealingFtdiConnector con;

	@Before
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.subject;
		LibUsbExampleData.populate(lib.data);
		ftdi = Ftdi.of();
		con = SelfHealingFtdiConnector.of(config);
	}

	@After
	public void after() {
		con.close();
		ftdi.close();
		enc.close();
	}

	@Test
	public void shouldConnectToFtdi() throws LibUsbException {
		con.connect();
		assertIterable(lib.controlTransferOut.values(),
			// Ftdi.open -> ftdi_usb_open_dev -> ftdi_usb_reset
			List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()),
			// Ftdi.open -> ftdi_usb_open_dev -> ftdi_set_baudrate (default 9600)
			List.of(0x40, 0x03, 0x4138, 0, ByteProvider.empty()),
			// Ftdi.bitmode -> ftdi_set_bitmode (BITBANG)
			List.of(0x40, 0x0b, 0x01ff, 1, ByteProvider.empty()),
			// Ftdi.baudrate -> ftdi_set_baudrate (9600 with BITBANG)
			List.of(0x40, 0x03, 0xc04e, 0, ByteProvider.empty()),
			// Ftdi.lineParams -> ftdi_set_line_property (DEFAULT 8:1:none:off)
			List.of(0x40, 0x04, 0x0008, 1, ByteProvider.empty()));
		lib.controlTransferIn.assertNoCall();
	}

	@Test
	public void shouldConfigureFtdi() throws LibUsbException {
		connect();
		con.bitmode(FtdiBitmode.OFF);
		lib.controlTransferOut.assertAuto(List.of(0x40, 0x0b, 0x0000, 1, ByteProvider.empty()));
		con.bitmode(FtdiBitmode.of(ftdi_mpsse_mode.BITMODE_CBUS));
		lib.controlTransferOut.assertAuto(List.of(0x40, 0x0b, 0x20ff, 1, ByteProvider.empty()));
		con.flowControl(FlowControl.xonXoff);
		lib.controlTransferOut.assertAuto(List.of(0x40, 0x02, 0, 0x0401, ByteProvider.empty()));
	}

	@Test
	public void shouldSetDtrRts() throws LibUsbException {
		connect();
		con.rts(true);
		lib.controlTransferOut.assertAuto(List.of(0x40, 0x01, 0x0202, 1, ByteProvider.empty()));
		con.dtr(true);
		lib.controlTransferOut.assertAuto(List.of(0x40, 0x01, 0x0101, 1, ByteProvider.empty()));
	}

	@Test
	public void shouldReadPins() throws LibUsbException {
		connect();
		lib.controlTransferIn.autoResponses(ByteArray.Immutable.wrap(0xa5));
		assertEquals(con.readPins(), 0xa5);
		lib.controlTransferIn.assertAuto(List.of(0xc0, 0x0c, 0x0000, 1, 1));
	}

	@Test
	public void shouldReadBytes() throws IOException {
		connect();
		lib.bulkTransferIn.autoResponses(ByteArray.Immutable.wrap(1, 2, 3, 4, 5));
		assertArray(con.read(3), 3, 4, 5); // 2B status + 3B data
		lib.bulkTransferIn.assertAuto(List.of(0x81, 5));
		lib.bulkTransferIn.autoResponses(ByteArray.Immutable.wrap(6, 7, 8));
		assertEquals(con.read(), 8); // 2B status + 1B data
		lib.bulkTransferIn.assertAuto(List.of(0x81, 3));
	}

	@Test
	public void shouldWriteBytes() throws IOException {
		connect();
		con.write(1, 2, 3);
		lib.bulkTransferOut.assertAuto(List.of(0x02, ByteArray.Immutable.wrap(1, 2, 3)));
	}

	@Test
	public void shouldRetryOnFailure() throws InterruptedException {
		LogModifier.run(() -> {
			ValueCondition<StateChange> sync = ValueCondition.of();
			try (var enc = con.listeners().enclose(sync::signal)) {
				lib.controlTransferOut.error.setFrom(LastErrorException::new);
				assertThrown(con::connect);
				sync.await(StateChange.broken);
				lib.controlTransferOut.awaitAuto();
				lib.controlTransferOut.awaitAuto();
				lib.controlTransferOut.awaitAuto();
				lib.controlTransferOut.error.clear();
				sync.await(StateChange.fixed);
			}
		}, Level.OFF, SelfHealingFtdiConnector.class);
	}

	@Test
	public void shouldFailIfNotConnected() {
		assertThrown(() -> con.read()); // not connected, set broken, then fix
		execSilently(() -> con.read()); // may have been fixed
		execSilently(() -> con.readPins()); // may have been fixed
	}

	@Test
	public void shouldDetermineIfBroken() {
		assertFalse(SelfHealingFtdiConnector.isBroken(null));
		assertFalse(SelfHealingFtdiConnector.isBroken(new IOException("test")));
		assertFalse(
			SelfHealingFtdiConnector.isBroken(LibUsbException.of(LIBUSB_ERROR_PIPE, "test")));
		assertTrue(
			SelfHealingFtdiConnector.isBroken(LibUsbException.of(LIBUSB_ERROR_NO_DEVICE, "test")));
		assertTrue(
			SelfHealingFtdiConnector.isBroken(LibUsbException.of(LIBUSB_ERROR_NOT_FOUND, "test")));
		assertTrue(
			SelfHealingFtdiConnector.isBroken(LibUsbException.of(LIBUSB_ERROR_NO_MEM, "test")));
	}

	@Test
	public void shouldHandleListenerErrors() throws IOException {
		LogModifier.run(() -> {
			connect();
			CallSync.Accept<StateChange> sync = CallSync.consumer(null, true);
			try (var enc = con.listeners().enclose(sync::accept)) {
				sync.error.setFrom(RTX);
				con.broken(); // logged
				sync.error.setFrom(RIX);
				assertThrown(con::broken);
			}
		}, Level.OFF, SelfHealingFtdiConnector.class);
	}

	/**
	 * Connect and reset state.
	 */
	private void connect() throws LibUsbException {
		con.connect();
		lib.controlTransferOut.reset();
	}
}
