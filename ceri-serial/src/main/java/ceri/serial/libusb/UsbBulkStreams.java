package ceri.serial.libusb;

import java.io.Closeable;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.text.ToString;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * Encapsulates allocation and freeing of bulk streams for device end-points.
 */
public class UsbBulkStreams implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final Supplier<libusb_device_handle> handleSupplier;
	private final byte[] endPointBytes;
	public final ByteProvider endPoints;
	public final int count;

	static UsbBulkStreams allocate(Supplier<libusb_device_handle> handleSupplier, int count,
		byte[] endPoints) throws LibUsbException {
		int n = LibUsb.libusb_alloc_streams(handleSupplier.get(), count, endPoints);
		return new UsbBulkStreams(handleSupplier, n, endPoints);
	}

	private UsbBulkStreams(Supplier<libusb_device_handle> handleSupplier, int count,
		byte[] endPoints) {
		this.handleSupplier = handleSupplier;
		this.count = count;
		this.endPointBytes = endPoints.clone();
		this.endPoints = ByteArray.Immutable.wrap(this.endPointBytes);
	}

	@Override
	public void close() {
		LogUtil.close(logger, handleSupplier.get(),
			h -> LibUsb.libusb_free_streams(h, endPointBytes));
	}

	@Override
	public String toString() {
		return ToString.forClass(this, count <= 1 ? String.valueOf(count) : "1-" + count,
			ArrayUtil.toHex(endPointBytes));
	}
}
