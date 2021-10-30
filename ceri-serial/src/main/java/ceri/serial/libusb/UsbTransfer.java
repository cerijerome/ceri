package ceri.serial.libusb;

import static ceri.common.collection.ArrayUtil.validateIndex;
import static ceri.common.exception.ExceptionUtil.exceptionf;
import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.ushort;
import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateRange;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_CONTROL_SETUP_SIZE;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import ceri.common.data.ByteProvider;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.RuntimeCloseable;
import ceri.common.util.BasicUtil;
import ceri.log.util.LogUtil;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_control_setup;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction;
import ceri.serial.libusb.jna.LibUsb.libusb_request_recipient;
import ceri.serial.libusb.jna.LibUsb.libusb_request_type;
import ceri.serial.libusb.jna.LibUsb.libusb_standard_request;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_cb_fn;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_flags;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_type;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * Encapsulates async transfers and associated types.
 */
public class UsbTransfer<T extends UsbTransfer<T>> implements RuntimeCloseable {
	private static final Logger logger = LogManager.getLogger();
	private static final int DIRECTION_MASK = LibUsb.LIBUSB_ENDPOINT_DIR_MASK;
	private static final int RECIPIENT_MASK = 0x1f;
	private static final int TYPE_MASK = 0x60;
	private final UsbDeviceHandle handle;
	private ByteBuffer buffer;
	private libusb_transfer transfer;

	public static class ControlSetup {
		private static final int SIZE = LIBUSB_CONTROL_SETUP_SIZE;
		private final libusb_control_setup cs;

		private ControlSetup(libusb_control_setup cs) {
			this.cs = cs;
		}

		public libusb_request_recipient recipient() {
			return libusb_request_recipient.xcoder.decode(cs.bmRequestType & RECIPIENT_MASK);
		}

		public ControlSetup recipient(libusb_request_recipient recipient) {
			cs.bmRequestType = (byte) ((cs.bmRequestType | ~RECIPIENT_MASK) & recipient.value);
			return this;
		}

		public libusb_request_type type() {
			return libusb_request_type.xcoder.decode(cs.bmRequestType & TYPE_MASK);
		}

		public ControlSetup type(libusb_request_type type) {
			cs.bmRequestType = (byte) ((cs.bmRequestType | ~TYPE_MASK) & type.value);
			return this;
		}

		public libusb_endpoint_direction direction() {
			return libusb_endpoint_direction.xcoder.decode(cs.bmRequestType & DIRECTION_MASK);
		}

		public ControlSetup direction(libusb_endpoint_direction direction) {
			cs.bmRequestType = (byte) ((cs.bmRequestType | ~DIRECTION_MASK) & direction.value);
			return this;
		}

		public int request() {
			return ubyte(cs.bRequest);
		}

		public ControlSetup request(int request) {
			cs.bRequest = (byte) request;
			return this;
		}

		public libusb_standard_request standard() {
			return libusb_standard_request.xcoder.decode(request());
		}

		public ControlSetup standard(libusb_standard_request standard) {
			return request(standard.value);
		}

		public int value() {
			return ushort(LibUsb.libusb_le16_to_cpu(cs.wValue));
		}

		public ControlSetup value(int value) {
			cs.wValue = LibUsb.libusb_cpu_to_le16((short) value);
			return this;
		}

		public int index() {
			return ushort(LibUsb.libusb_le16_to_cpu(cs.wIndex));
		}

		public ControlSetup index(int index) {
			cs.wIndex = LibUsb.libusb_cpu_to_le16((short) index);
			return this;
		}

		public int length() {
			return ushort(LibUsb.libusb_le16_to_cpu(cs.wLength));
		}

		public ControlSetup length(int length) {
			validateMin(length, 0);
			cs.wLength = LibUsb.libusb_cpu_to_le16((short) length);
			return this;
		}
	}

	public static class Control extends UsbTransfer<Control> {
		private ControlSetup setup = null;

		static Control alloc(UsbDeviceHandle handle, Consumer<? super Control> callback)
			throws LibUsbException {
			return allocTransfer(0, transfer -> {
				LibUsb.libusb_fill_control_transfer(transfer, handle.handle(), null, null, null, 0);
				var control = new Control(handle, transfer, callback);
				return control;
			});
		}

		private Control(UsbDeviceHandle handle, libusb_transfer transfer,
			Consumer<? super Control> callback) {
			super(handle, transfer, callback);
		}

		public ControlSetup setup() {
			if (setup == null)
				setup = new ControlSetup(new libusb_control_setup(transfer().buffer));
			return setup;
		}

		@Override
		public void submit() throws LibUsbException {
			Objects.requireNonNull(buffer());
			validateRange(setup.length(), 0, buffer().capacity() - ControlSetup.SIZE);
			transfer().length = length();
			Struct.write(setup.cs);
			super.submit();
		}

		@Override
		public int length() {
			return setup().length() + ControlSetup.SIZE;
		}

		public Control length(int length) {
			validateMin(length, ControlSetup.SIZE);
			setup().length(length - ControlSetup.SIZE);
			return this;
		}

		public ByteBuffer data() {
			ByteBuffer buffer = buffer();
			if (buffer == null) return null;
			return buffer.slice(ControlSetup.SIZE, setup().length());
		}

		@SuppressWarnings("resource")
		@Override
		public Control buffer(ByteBuffer buffer) {
			Objects.requireNonNull(buffer);
			validateMin(buffer.capacity(), ControlSetup.SIZE);
			super.buffer(buffer);
			setup = copy(setup, transfer().buffer); // relocate setup to new buffer
			return this;
		}
	}

	/**
	 * Encapsulates allocation and freeing of bulk streams for device end-points.
	 */
	public static class BulkStreams implements RuntimeCloseable {
		private static final Logger logger = LogManager.getLogger();
		private final UsbDeviceHandle handle;
		private final byte[] endPointBytes;
		public final ByteProvider endPoints;
		public final int count;

		BulkStreams(UsbDeviceHandle handle, int count, byte[] endPoints) {
			this.handle = handle;
			this.count = count;
			this.endPointBytes = endPoints.clone();
			this.endPoints = ByteProvider.of(this.endPointBytes);
		}

		/**
		 * Allocates an async bulk stream transfer.
		 */
		public BulkStream bulkTransfer(int endPoint, int streamId,
			Consumer<? super BulkStream> callback) throws LibUsbException {
			validateRange(streamId, 1, count, "Stream id");
			validateEndPoint(endPoint);
			return BulkStream.alloc(handle, endPoint, streamId, callback);
		}

		@Override
		public void close() {
			LogUtil.close(logger, () -> LibUsb.libusb_free_streams(handle.handle(), endPointBytes));
		}

		private void validateEndPoint(int endPoint) {
			if (endPoints.indexOf(0, endPoint) >= 0) return;
			throw exceptionf("End point not in %s: %d", ByteProvider.toHex(endPoints), endPoint);
		}
	}

	public static class BulkStream extends UsbTransfer<BulkStream> {
		private final int streamId;

		private static BulkStream alloc(UsbDeviceHandle handle, int endPoint, int streamId,
			Consumer<? super BulkStream> callback) throws LibUsbException {
			return allocTransfer(0, transfer -> {
				LibUsb.libusb_fill_bulk_stream_transfer(transfer, handle.handle(), endPoint,
					streamId, null, 0, null, null, 0);
				return new BulkStream(handle, transfer, streamId, callback);
			});
		}

		private BulkStream(UsbDeviceHandle handle, libusb_transfer transfer, int streamId,
			Consumer<? super BulkStream> callback) {
			super(handle, transfer, callback);
			this.streamId = streamId;
		}

		public int streamId() {
			return streamId;
		}

		public BulkStream length(int length) {
			return super.length(length);
		}
	}

	public static class Bulk extends UsbTransfer<Bulk> {
		static Bulk alloc(UsbDeviceHandle handle, Consumer<? super Bulk> callback)
			throws LibUsbException {
			return allocTransfer(0, transfer -> {
				LibUsb.libusb_fill_bulk_transfer(transfer, handle.handle(), 0, null, 0, null, null,
					0);
				return new Bulk(handle, transfer, callback);
			});
		}

		private Bulk(UsbDeviceHandle handle, libusb_transfer transfer,
			Consumer<? super Bulk> callback) {
			super(handle, transfer, callback);
		}

		public Bulk endPoint(int endPoint) {
			return super.endPoint(endPoint);
		}

		public Bulk length(int length) {
			return super.length(length);
		}
	}

	public static class Interrupt extends UsbTransfer<Interrupt> {
		static Interrupt alloc(UsbDeviceHandle handle, Consumer<? super Interrupt> callback)
			throws LibUsbException {
			return allocTransfer(0, transfer -> {
				LibUsb.libusb_fill_interrupt_transfer(transfer, handle.handle(), 0, null, 0, null,
					null, 0);
				return new Interrupt(handle, transfer, callback);
			});
		}

		private Interrupt(UsbDeviceHandle handle, libusb_transfer transfer,
			Consumer<? super Interrupt> callback) {
			super(handle, transfer, callback);
		}

		public Interrupt endPoint(int endPoint) {
			return super.endPoint(endPoint);
		}

		public Interrupt length(int length) {
			return super.length(length);
		}
	}

	public static class Iso extends UsbTransfer<Iso> {
		static Iso alloc(UsbDeviceHandle handle, int packets, Consumer<? super Iso> callback)
			throws LibUsbException {
			return allocTransfer(packets, transfer -> {
				LibUsb.libusb_fill_iso_transfer(transfer, handle.handle(), 0, null, 0, packets,
					null, null, 0);
				return new Iso(handle, transfer, callback);
			});
		}

		private Iso(UsbDeviceHandle handle, libusb_transfer transfer,
			Consumer<? super Iso> callback) {
			super(handle, transfer, callback);
		}

		public Iso endPoint(int endPoint) {
			return super.endPoint(endPoint);
		}

		public int maxPackets() {
			return transfer().iso_packet_desc.length;
		}

		public int packets() {
			return transfer().num_iso_packets;
		}

		public Iso packets(int packets) {
			validateRange(packets, 0, maxPackets());
			var transfer = transfer();
			transfer.length = offset(packets);
			transfer.num_iso_packets = packets;
			return this;
		}

		public Iso packetLength(int packet, int length) {
			validateIndex(packets(), packet);
			var transfer = transfer();
			transfer.length += length - transfer.iso_packet_desc[packet].length;
			transfer.iso_packet_desc[packet].length = length;
			return this;
		}

		public Iso packetLengths(int length) {
			var transfer = transfer();
			transfer.length = length * packets();
			LibUsb.libusb_set_iso_packet_lengths(transfer, length);
			return this;
		}

		public ByteBuffer packetBuffer(int packet) {
			validateIndex(packets(), packet);
			return packetBuffer(packet, offset(packet));
		}

		public ByteBuffer packetBufferSimple(int packet) {
			validateIndex(packets(), packet);
			return packetBuffer(packet, packet * transfer().iso_packet_desc[0].length);
		}

		private int offset(int packet) {
			var transfer = transfer();
			int offset = 0;
			for (int i = 0; i < packet; i++)
				offset += transfer.iso_packet_desc[i].length;
			return offset;
		}
		
		private ByteBuffer packetBuffer(int packet, int offset) {
			var buffer = buffer();
			if (buffer == null) return null;
			return buffer.slice(offset, transfer().iso_packet_desc[packet].length);
		}
	}

	private UsbTransfer(UsbDeviceHandle handle, libusb_transfer transfer,
		Consumer<? super T> callback) {
		this.handle = handle;
		this.transfer = transfer;
		transfer.callback = adaptCallback(callback);
	}

	public UsbDeviceHandle handle() {
		return handle;
	}

	public void submit() throws LibUsbException {
		var transfer = transfer();
		validateRange(transfer.length, 0, buffer == null ? 0 : buffer.capacity());
		LibUsb.libusb_submit_transfer(Struct.write(transfer));
	}

	public void cancel() throws LibUsbException {
		LibUsb.libusb_cancel_transfer(transfer());
	}

	public libusb_transfer_type type() {
		return transfer.type();
	}

	public int endPoint() {
		return ubyte(transfer().endpoint);
	}

	public Set<libusb_transfer_flags> flags() {
		return transfer().flags();
	}

	public T flags(libusb_transfer_flags... flags) {
		transfer().flags = (byte) libusb_transfer_flags.xcoder.encode(flags);
		return typedThis();
	}

	public int timeoutMs() {
		return transfer().timeout;
	}

	public T timeoutMs(int timeoutMs) {
		transfer().timeout = timeoutMs;
		return typedThis();
	}

	public int length() {
		return transfer().length;
	}

	public ByteBuffer buffer() {
		return buffer;
	}

	public T buffer(ByteBuffer buffer) {
		transfer().buffer = JnaUtil.pointer(buffer);
		this.buffer = buffer;
		return typedThis();
	}

	/**
	 * Status set by libusb.
	 */
	public libusb_transfer_status status() {
		return transfer().status();
	}

	/**
	 * Actual transferred data length set by libusb.
	 */
	public int actualLength() {
		return transfer().actual_length;
	}

	@Override
	public void close() {
		LogUtil.execute(logger, () -> LibUsb.libusb_free_transfer(transfer));
		transfer = null;
	}

	libusb_transfer transfer() {
		if (transfer != null) return transfer;
		throw new IllegalStateException("Transfer has been closed");
	}

	private T endPoint(int endPoint) {
		transfer().endpoint = (byte) endPoint;
		return typedThis();
	}

	private T length(int length) {
		validateMin(length, 0);
		transfer().length = length;
		return typedThis();
	}

	private static ControlSetup copy(ControlSetup from, Pointer p) {
		var setup = new libusb_control_setup(p);
		if (from != null) {
			setup.bmRequestType = from.cs.bmRequestType;
			setup.bRequest = from.cs.bRequest;
			setup.wIndex = from.cs.wIndex;
			setup.wValue = from.cs.wValue;
			setup.wLength = from.cs.wLength;
		}
		return new ControlSetup(setup);
	}

	private libusb_transfer_cb_fn adaptCallback(Consumer<? super T> callback) {
		return callback == null ? null : p -> {
			libusb_transfer_cb_fn.read(transfer());
			LogUtil.execute(logger, () -> callback.accept(typedThis()));
		};
	}

	private static <T> T allocTransfer(int isoPackets,
		ExceptionFunction<LibUsbException, libusb_transfer, T> function) throws LibUsbException {
		var transfer = LibUsb.libusb_alloc_transfer(isoPackets);
		try {
			return function.apply(transfer);
		} catch (LibUsbException | RuntimeException e) {
			LogUtil.execute(logger, () -> LibUsb.libusb_free_transfer(transfer));
			throw e;
		}
	}

	private T typedThis() {
		return BasicUtil.uncheckedCast(this);
	}
}
