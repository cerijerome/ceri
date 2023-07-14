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
import static ceri.common.test.TestUtil.provider;
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
import ceri.common.data.ByteProvider;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.common.util.Enclosed;
import ceri.log.test.LogModifier;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbSampleData;
import ceri.serial.libusb.jna.TestLibUsbNative;

public class SelfHealingFtdiConnectorBehavior {
	private static final SelfHealingFtdiConfig config =
		SelfHealingFtdiConfig.builder().recoveryDelayMs(1).fixRetryDelayMs(1).build();
	private TestLibUsbNative lib;
	private Enclosed<RuntimeException, TestLibUsbNative> enc;
	private SelfHealingFtdi con;

	@Before
	public void before() {
		enc = TestLibUsbNative.register();
		lib = enc.subject;
		LibUsbSampleData.populate(lib.data);
		con = SelfHealingFtdi.of(config);
	}

	@After
	public void after() {
		con.close();
		enc.close();
	}

	@Test
	public void shouldConnectToFtdi() throws LibUsbException {
		con.connect();
		assertIterable(lib.syncTransferOut.values(),
			List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()), // open:ftdi_usb_reset()
			List.of(0x40, 0x03, 0x4138, 0, ByteProvider.empty()), // open:ftdi_set_baudrate()
			List.of(0x40, 0x0b, 0x01ff, 1, ByteProvider.empty()), // bitMode()
			List.of(0x40, 0x03, 0xc04e, 0, ByteProvider.empty()), // baudRate()
			List.of(0x40, 0x04, 0x0008, 1, ByteProvider.empty())); // lineParams()
		lib.syncTransferIn.assertCalls(0);
	}

	@Test
	public void shouldConfigureFtdi() throws LibUsbException {
		connect();
		con.bitmode(FtdiBitMode.OFF);
		lib.syncTransferOut.assertAuto(List.of(0x40, 0x0b, 0x0000, 1, ByteProvider.empty()));
		con.bitmode(FtdiBitMode.of(ftdi_mpsse_mode.BITMODE_CBUS));
		lib.syncTransferOut.assertAuto(List.of(0x40, 0x0b, 0x20ff, 1, ByteProvider.empty()));
		con.flowControl(FtdiFlowControl.xonXoff);
		lib.syncTransferOut.assertAuto(List.of(0x40, 0x02, 0, 0x0401, ByteProvider.empty()));
	}

	@Test
	public void shouldSetDtrRts() throws LibUsbException {
		connect();
		con.rts(true);
		lib.syncTransferOut.assertAuto(List.of(0x40, 0x01, 0x0202, 1, ByteProvider.empty()));
		con.dtr(true);
		lib.syncTransferOut.assertAuto(List.of(0x40, 0x01, 0x0101, 1, ByteProvider.empty()));
	}

	@Test
	public void shouldReadPins() throws LibUsbException {
		connect();
		lib.syncTransferIn.autoResponses(provider(0xa5));
		assertEquals(con.readPins(), 0xa5);
		lib.syncTransferIn.assertAuto(List.of(0xc0, 0x0c, 0x0000, 1, 1));
	}

	@Test
	public void shouldReadBytes() throws IOException {
		connect();
		lib.syncTransferIn.autoResponses(provider(1, 2, 3, 4, 5));
		assertArray(con.read(3), 3, 4, 5); // 2B status + 3B data
		lib.syncTransferIn.assertAuto(List.of(0x81, 5));
		lib.syncTransferIn.autoResponses(provider(6, 7, 8));
		assertEquals(con.read(), 8); // 2B status + 1B data
		lib.syncTransferIn.assertAuto(List.of(0x81, 3));
	}

	@Test
	public void shouldWriteBytes() throws IOException {
		connect();
		con.write(1, 2, 3);
		lib.syncTransferOut.assertAuto(List.of(0x02, provider(1, 2, 3)));
	}

	@Test
	public void shouldRetryOnFailure() throws InterruptedException {
		LogModifier.run(() -> {
			ValueCondition<StateChange> sync = ValueCondition.of();
			try (var enc = con.listeners().enclose(sync::signal)) {
				lib.syncTransferOut.error.setFrom(s -> new LastErrorException(s));
				assertThrown(con::connect);
				sync.await(StateChange.broken);
				lib.syncTransferOut.awaitAuto();
				lib.syncTransferOut.awaitAuto();
				lib.syncTransferOut.awaitAuto();
				lib.syncTransferOut.error.clear();
				sync.await(StateChange.fixed);
			}
		}, Level.OFF, SelfHealingFtdi.class);
	}

	@Test
	public void shouldFailIfNotConnected() {
		assertThrown(() -> con.read()); // not connected, set broken, then fix
		execSilently(() -> con.read()); // may have been fixed
		execSilently(() -> con.readPins()); // may have been fixed
	}

	@Test
	public void shouldDetermineIfBroken() {
		assertFalse(SelfHealingFtdi.isBroken(null));
		assertFalse(SelfHealingFtdi.isBroken(new IOException("test")));
		assertFalse(
			SelfHealingFtdi.isBroken(LibUsbException.of(LIBUSB_ERROR_PIPE, "test")));
		assertTrue(
			SelfHealingFtdi.isBroken(LibUsbException.of(LIBUSB_ERROR_NO_DEVICE, "test")));
		assertTrue(
			SelfHealingFtdi.isBroken(LibUsbException.of(LIBUSB_ERROR_NOT_FOUND, "test")));
		assertTrue(
			SelfHealingFtdi.isBroken(LibUsbException.of(LIBUSB_ERROR_NO_MEM, "test")));
	}

	@Test
	public void shouldHandleListenerErrors() throws IOException {
		LogModifier.run(() -> {
			connect();
			CallSync.Consumer<StateChange> sync = CallSync.consumer(null, true);
			try (var enc = con.listeners().enclose(sync::accept)) {
				sync.error.setFrom(RTX);
				con.broken(); // logged
				sync.error.setFrom(RIX);
				assertThrown(con::broken);
			}
		}, Level.OFF, SelfHealingFtdi.class);
	}

	/**
	 * Connect and reset state.
	 */
	private void connect() throws LibUsbException {
		con.connect();
		lib.syncTransferOut.reset();
	}
}
