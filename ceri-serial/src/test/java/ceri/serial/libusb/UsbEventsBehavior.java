package ceri.serial.libusb;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertUnordered;
import static ceri.common.test.TestUtil.threadCall;
import java.time.Duration;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.test.Captor;
import ceri.common.util.Enclosure;
import ceri.serial.libusb.UsbEvents.Completed;
import ceri.serial.libusb.UsbEvents.PollFd;
import ceri.serial.libusb.jna.LibUsb.libusb_poll_event;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.test.TestLibUsbNative;

public class UsbEventsBehavior {
	private TestLibUsbNative lib;
	private Enclosure<TestLibUsbNative> enc;
	private Usb usb;
	private UsbEvents events;

	@Before
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		usb = Usb.of();
		events = usb.events();
	}

	@After
	public void after() {
		usb.close();
		enc.close();
	}

	@Test
	public void shouldManageLocks() throws LibUsbException {
		assertEquals(events.handlingOk(), true);
		lib.data.context(usb.context()).eventHandling = false;
		assertEquals(events.handlingOk(), false);
		assertEquals(events.handlerActive(), false);
		try (var _ = events.lockWaiters(); var _ = events.lock()) {
			assertEquals(events.handlerActive(), true);
			assertEquals(tryLock(), true);
			try (var thread = threadCall(() -> tryLock())) {
				assertEquals(thread.get(), false);
			}
		}
	}

	@Test
	public void shouldEncapsulateCompletion() {
		var completed = Completed.of();
		assertEquals(completed.completed(), false);
		assertEquals(completed.value(), 0);
		completed.complete(1);
		assertEquals(completed.completed(), true);
		assertEquals(completed.value(), 1);
	}

	@Test
	public void shouldHandleEvents() throws LibUsbException {
		events.interruptHandler();
		lib.generalSync.autoResponses(12345);
		assertEquals(events.nextTimeout(), Duration.ofMillis(12345));
		events.await(Duration.ZERO);
		events.handleCompleted(null);
		events.handleCompleted(Completed.of());
		events.handleTimeout(Duration.ZERO);
		events.handleTimeoutCompleted(Duration.ZERO, null);
		events.handleTimeoutCompleted(Duration.ZERO, Completed.of());
		assertThrown(() -> events.handleLocked(Duration.ZERO));
		try (var _ = events.lock()) {
			events.handleLocked(Duration.ZERO);
		}
	}

	@Test
	public void shouldPollFds() throws LibUsbException {
		assertOrdered(events.pollFds());
		lib.pollFds.autoResponses(List.of(new PollFd(7, 0x5), new PollFd(8, 0x4)));
		assertOrdered(events.pollFds(), new PollFd(7, 0x5), new PollFd(8, 0x4));
		assertEquals(events.pollHandleTimeouts(), false);
	}

	@Test
	public void shouldInvokePollFdCallbacks() throws LibUsbException {
		Captor<PollFd> addedCaptor = Captor.of();
		Captor.OfInt removedCaptor = Captor.ofInt();
		lib.handlePollFdEvent.autoResponses( //
			new TestLibUsbNative.PollFdEvent(5, 0x5, true),
			new TestLibUsbNative.PollFdEvent(5, 0x5, true),
			new TestLibUsbNative.PollFdEvent(6, 0x5, false));
		events.pollNotifiers(null, null);
		events.handle();
		events.pollNotifiers(addedCaptor::accept, removedCaptor::accept);
		events.handle();
		addedCaptor.verify(new PollFd(5, 0x5));
		assertUnordered(addedCaptor.values.get(0).pollEvents(), libusb_poll_event.POLLIN,
			libusb_poll_event.POLLOUT);
		events.handle();
		removedCaptor.verify(6);
	}

	private boolean tryLock() throws LibUsbException {
		try (var x = events.tryLock()) {
			return x.ref;
		}
	}
}
