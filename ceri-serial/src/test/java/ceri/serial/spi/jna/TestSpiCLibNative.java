package ceri.serial.spi.jna;

import static ceri.common.math.MathUtil.ushort;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_MESSAGE;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_RD_BITS_PER_WORD;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_RD_LSB_FIRST;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_RD_MAX_SPEED_HZ;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_RD_MODE;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_RD_MODE32;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_WR_BITS_PER_WORD;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_WR_LSB_FIRST;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_WR_MAX_SPEED_HZ;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_WR_MODE;
import static ceri.serial.spi.jna.SpiDev.SPI_IOC_WR_MODE32;
import java.util.Set;
import com.sun.jna.LastErrorException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import ceri.common.data.ByteProvider;
import ceri.common.test.CallSync;
import ceri.jna.clib.jna.CLib;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.type.CUlong;
import ceri.jna.util.JnaLibrary;
import ceri.jna.util.JnaUtil;
import ceri.serial.spi.jna.SpiDev.spi_ioc_transfer;

public class TestSpiCLibNative extends TestCLibNative {
	private static final Set<Integer> GET_BYTE =
		Set.of(SPI_IOC_RD_MODE, SPI_IOC_RD_LSB_FIRST, SPI_IOC_RD_BITS_PER_WORD);
	private static final Set<Integer> SET_BYTE =
		Set.of(SPI_IOC_WR_MODE, SPI_IOC_WR_LSB_FIRST, SPI_IOC_WR_BITS_PER_WORD);
	private static final Set<Integer> GET_INT = Set.of(SPI_IOC_RD_MAX_SPEED_HZ, SPI_IOC_RD_MODE32);
	private static final Set<Integer> SET_INT = Set.of(SPI_IOC_WR_MAX_SPEED_HZ, SPI_IOC_WR_MODE32);
	public final CallSync.Function<Int, Integer> ioctlSpiInt = CallSync.function(null, 0);
	public final CallSync.Function<Msg, ByteProvider> ioctlSpiMsg =
		CallSync.function(null, ByteProvider.empty());

	public record Int(int request, Integer value) {}

	public record Msg(int request, ByteProvider txBytes, int len, int speed_hz, int delay_usecs,
		int bits_per_word, int cs_change, int tx_nbits, int rx_nbits) {}

	/**
	 * A wrapper for repeatedly overriding the library in tests.
	 */
	public static JnaLibrary.Ref<TestSpiCLibNative> ref() {
		return CLib.library.ref(TestSpiCLibNative::of);
	}

	public static TestSpiCLibNative of() {
		return new TestSpiCLibNative();
	}

	private TestSpiCLibNative() {}

	@Override
	public int ioctl(int fd, CUlong req, Object... objs) throws LastErrorException {
		int request = req.intValue();
		if (request == SPI_IOC_MESSAGE(1)) return ioctlSpiMsg(request, (spi_ioc_transfer) objs[0]);
		if (GET_BYTE.contains(request)) return ioctlSpiGetByte(request, (ByteByReference) objs[0]);
		if (SET_BYTE.contains(request)) return ioctlSpiSetByte(request, (ByteByReference) objs[0]);
		if (GET_INT.contains(request)) return ioctlSpiGetInt(request, (IntByReference) objs[0]);
		if (SET_INT.contains(request)) return ioctlSpiSetInt(request, (IntByReference) objs[0]);
		return super.ioctl(fd, req, objs);
	}

	private int ioctlSpiMsg(int request, spi_ioc_transfer xfer) {
		ByteProvider txBytes = transmitBytes(xfer);
		ByteProvider rxBytes = ioctlSpiMsg.apply(new Msg(request, txBytes, xfer.len, xfer.speed_hz,
			ushort(xfer.delay_usecs), xfer.bits_per_word & 0xff, xfer.cs_change & 0xff,
			xfer.tx_nbits & 0xff, xfer.rx_nbits & 0xff));
		receiveBytes(xfer, rxBytes);
		return 0;
	}

	private ByteProvider transmitBytes(spi_ioc_transfer xfer) {
		if (xfer.tx_buf == 0L) return null;
		Pointer p = new Pointer(xfer.tx_buf);
		return ByteProvider.of(JnaUtil.bytes(p, 0, xfer.len));
	}

	private void receiveBytes(spi_ioc_transfer xfer, ByteProvider rxBytes) {
		if (xfer.rx_buf == 0L || rxBytes == null) return;
		Pointer p = new Pointer(xfer.rx_buf);
		JnaUtil.write(p, rxBytes.copy(0));
	}

	private int ioctlSpiSetByte(int request, ByteByReference ref) {
		ioctlSpiInt.apply(new Int(request, ref.getValue() & 0xff));
		return 0;
	}

	private int ioctlSpiGetByte(int request, ByteByReference ref) {
		ref.setValue(ioctlSpiInt.apply(new Int(request, null)).byteValue());
		return 0;
	}

	private int ioctlSpiSetInt(int request, IntByReference ref) {
		ioctlSpiInt.apply(new Int(request, ref.getValue()));
		return 0;
	}

	private int ioctlSpiGetInt(int request, IntByReference ref) {
		ref.setValue(ioctlSpiInt.apply(new Int(request, null)));
		return 0;
	}
}
