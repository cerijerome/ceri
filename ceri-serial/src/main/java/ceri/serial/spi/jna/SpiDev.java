package ceri.serial.spi.jna;

import static ceri.serial.jna.JnaUtil.verify;
import static ceri.serial.jna.clib.Ioctl._IOC_SIZEBITS;
import static ceri.serial.jna.clib.Ioctl._IOR;
import static ceri.serial.jna.clib.Ioctl._IOW;
import java.io.IOException;
import java.util.List;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import ceri.serial.jna.clib.CException;
import ceri.serial.jna.clib.CLib;
import ceri.serial.jna.clib.TermiosUtil;

public class SpiDev {
	private static final int SPI_CPHA = 0x01;
	private static final int SPI_CPOL = 0x02;

	public static final int SPI_MODE_0 = (0 | 0);
	public static final int SPI_MODE_1 = (0 | SPI_CPHA);
	public static final int SPI_MODE_2 = (SPI_CPOL | 0);
	public static final int SPI_MODE_3 = (SPI_CPOL | SPI_CPHA);

	public static final int SPI_CS_HIGH = 0x04;
	public static final int SPI_LSB_FIRST = 0x08;
	public static final int SPI_3WIRE = 0x10;
	public static final int SPI_LOOP = 0x20;
	public static final int SPI_NO_CS = 0x40;
	public static final int SPI_READY = 0x80;

	private static final int SPI_IOC_MAGIC = 'k';
	private static final int SIZEOF_SPI_IOC_TRANSFER = new spi_ioc_transfer().size();

	public static class spi_ioc_transfer extends Structure {
		private static final List<String> FIELDS = List.of( //
			"tx_buf", "rx_buf", "len", "speed_hz", "delay_usecs", "bits_per_word", "cs_change",
			"tx_nbits", "rx_nbits", "pad");

		public static class ByValue extends spi_ioc_transfer //
			implements Structure.ByValue {}

		public static class ByReference extends spi_ioc_transfer //
			implements Structure.ByReference {}

		public long tx_buf;
		public long rx_buf;
		public int len;
		public int speed_hz;
		public short delay_usecs;
		public byte bits_per_word;
		public byte cs_change;
		public byte tx_nbits;
		public byte rx_nbits;
		public short pad;

		public spi_ioc_transfer() {}

		public spi_ioc_transfer(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	private static int SPI_MSGSIZE(int n) {
		int size = n * SIZEOF_SPI_IOC_TRANSFER;
		return size < (1 << _IOC_SIZEBITS) ? size : 0;
	}

	private static final int SPI_IOC_MESSAGE(int n) {
		return _IOW(SPI_IOC_MAGIC, 0, SPI_MSGSIZE(n));
	}

	/* Read / Write of SPI mode (SPI_MODE_0..SPI_MODE_3) */
	private static final int SPI_IOC_RD_MODE = _IOR(SPI_IOC_MAGIC, 1, Byte.BYTES);
	private static final int SPI_IOC_WR_MODE = _IOW(SPI_IOC_MAGIC, 1, Byte.BYTES);

	/* Read / Write SPI bit justification */
	private static final int SPI_IOC_RD_LSB_FIRST = _IOR(SPI_IOC_MAGIC, 2, Byte.BYTES);
	private static final int SPI_IOC_WR_LSB_FIRST = _IOW(SPI_IOC_MAGIC, 2, Byte.BYTES);

	/* Read / Write SPI device word length (1..N) */
	private static final int SPI_IOC_RD_BITS_PER_WORD = _IOR(SPI_IOC_MAGIC, 3, Byte.BYTES);
	private static final int SPI_IOC_WR_BITS_PER_WORD = _IOW(SPI_IOC_MAGIC, 3, Byte.BYTES);

	/* Read / Write SPI device default max speed hz */
	private static final int SPI_IOC_RD_MAX_SPEED_HZ = _IOR(SPI_IOC_MAGIC, 4, Integer.BYTES);
	private static final int SPI_IOC_WR_MAX_SPEED_HZ = _IOW(SPI_IOC_MAGIC, 4, Integer.BYTES);

	/* Wrappers for ioctl calls */
	
	private static final String PATH_FORMAT = "/dev/spidev%d.%d";
	
	public int open(int chip, int bus, int flags) throws CException {
		String path = String.format(PATH_FORMAT, bus, chip);
		return TermiosUtil.open(path, flags);
	}
	
	public int getMode(int fd) throws IOException {
		IntByReference p = new IntByReference();
		verify(CLib.ioctl(fd, SPI_IOC_RD_MODE, p), "ioctl::SPI_IOC_RD_MODE");
		return p.getValue();
	}
	
	public void setMode(int fd, int mode) throws IOException {
		IntByReference p = new IntByReference(mode);
		verify(CLib.ioctl(fd, SPI_IOC_WR_MODE, p), "ioctl::SPI_IOC_WR_MODE");
	}
	
}
