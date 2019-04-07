package ceri.serial.spi.jna;

import static ceri.serial.jna.JnaUtil.ubyte;
import static ceri.serial.jna.clib.Ioctl._IOC_SIZEBITS;
import static ceri.serial.jna.clib.Ioctl._IOR;
import static ceri.serial.jna.clib.Ioctl._IOW;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import ceri.serial.jna.clib.CException;
import ceri.serial.jna.clib.CLib;
import ceri.serial.jna.clib.SizeOf;
import ceri.serial.jna.clib.TermiosUtil;

public class SpiDev {
	private static final Logger logger = LogManager.getLogger();
	
	/* mode flags */
	public static final int SPI_CPHA = 0x01;
	public static final int SPI_CPOL = 0x02;
	public static final int SPI_CS_HIGH = 0x04;
	public static final int SPI_LSB_FIRST = 0x08;
	public static final int SPI_3WIRE = 0x10;
	public static final int SPI_LOOP = 0x20;
	public static final int SPI_NO_CS = 0x40;
	public static final int SPI_READY = 0x80;
	/* 32-bit mode flags */
	public static final int SPI_TX_DUAL = 0x100;
	public static final int SPI_TX_QUAD = 0x200;
	public static final int SPI_RX_DUAL = 0x400;
	public static final int SPI_RX_QUAD = 0x800;

	/* preset modes */
	public static final int SPI_MODE_0 = (0 | 0);
	public static final int SPI_MODE_1 = (0 | SPI_CPHA);
	public static final int SPI_MODE_2 = (SPI_CPOL | 0);
	public static final int SPI_MODE_3 = (SPI_CPOL | SPI_CPHA);

	public static class spi_ioc_transfer extends Structure {
		private static final List<String> FIELDS = List.of("tx_buf", "rx_buf", "len", "speed_hz",
			"delay_usecs", "bits_per_word", "cs_change", "tx_nbits", "rx_nbits", "pad");

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

	private static final int SPI_IOC_MAGIC = 'k';
	private static final int SIZEOF_SPI_IOC_TRANSFER = new spi_ioc_transfer().size();

	private static int SPI_MSGSIZE(int n) {
		int size = n * SIZEOF_SPI_IOC_TRANSFER;
		return size < (1 << _IOC_SIZEBITS) ? size : 0;
	}

	private static final int SPI_IOC_MESSAGE(int n) {
		return _IOW(SPI_IOC_MAGIC, 0, SPI_MSGSIZE(n));
	}

	/* Read / Write of SPI mode (8 bits only) */
	public static final int SPI_IOC_RD_MODE = _IOR(SPI_IOC_MAGIC, 1, SizeOf.CHAR);
	public static final int SPI_IOC_WR_MODE = _IOW(SPI_IOC_MAGIC, 1, SizeOf.CHAR);

	/* Read / Write SPI bit justification */
	public static final int SPI_IOC_RD_LSB_FIRST = _IOR(SPI_IOC_MAGIC, 2, SizeOf.CHAR);
	public static final int SPI_IOC_WR_LSB_FIRST = _IOW(SPI_IOC_MAGIC, 2, SizeOf.CHAR);

	/* Read / Write SPI device word length (1..N) */
	public static final int SPI_IOC_RD_BITS_PER_WORD = _IOR(SPI_IOC_MAGIC, 3, SizeOf.CHAR);
	public static final int SPI_IOC_WR_BITS_PER_WORD = _IOW(SPI_IOC_MAGIC, 3, SizeOf.CHAR);

	/* Read / Write SPI device default max speed hz */
	public static final int SPI_IOC_RD_MAX_SPEED_HZ = _IOR(SPI_IOC_MAGIC, 4, SizeOf.INT);
	public static final int SPI_IOC_WR_MAX_SPEED_HZ = _IOW(SPI_IOC_MAGIC, 4, SizeOf.INT);

	/* Read / Write of the SPI mode field */
	public static final int SPI_IOC_RD_MODE32 = _IOR(SPI_IOC_MAGIC, 5, SizeOf.INT);
	public static final int SPI_IOC_WR_MODE32 = _IOW(SPI_IOC_MAGIC, 5, SizeOf.INT);

	/* Wrappers for ioctl calls */

	private static final String PATH_FORMAT = "/dev/spidev%d.%d";

	public static int open(int bus, int chip, int flags) throws CException {
		String path = String.format(PATH_FORMAT, bus, chip);
		logger.debug("Opening {}", path);
		return TermiosUtil.open(path, flags);
	}

	public static void message(int fd, spi_ioc_transfer transfer) throws CException {
		// if (transfers == null || transfers.length == 0) return;
		CLib.ioctl("SPI_IOC_MESSAGE(1)", fd, SPI_IOC_MESSAGE(1), transfer);
	}

	public static int getMode(int fd) throws CException {
		ByteByReference p = new ByteByReference();
		CLib.ioctl("SPI_IOC_RD_MODE", fd, SPI_IOC_RD_MODE, p);
		return ubyte(p.getValue());
	}

	public static void setMode(int fd, int mode) throws CException {
		ByteByReference p = new ByteByReference((byte) mode);
		CLib.ioctl("SPI_IOC_WR_MODE", fd, SPI_IOC_WR_MODE, p);
	}

	public static boolean isLsbFirst(int fd) throws CException {
		ByteByReference p = new ByteByReference();
		CLib.ioctl("SPI_IOC_RD_LSB_FIRST", fd, SPI_IOC_RD_LSB_FIRST, p);
		return p.getValue() != 0;
	}

	public static void setLsbFirst(int fd, boolean enabled) throws CException {
		ByteByReference p = new ByteByReference((byte) (enabled ? 1 : 0));
		CLib.ioctl("SPI_IOC_WR_LSB_FIRST", fd, SPI_IOC_WR_LSB_FIRST, p);
	}

	public static int getBitsPerWord(int fd) throws CException {
		ByteByReference p = new ByteByReference();
		CLib.ioctl("SPI_IOC_RD_BITS_PER_WORD", fd, SPI_IOC_RD_BITS_PER_WORD, p);
		return ubyte(p.getValue());
	}

	public static void setBitsPerWord(int fd, int bitsPerWord) throws CException {
		ByteByReference p = new ByteByReference((byte) bitsPerWord);
		CLib.ioctl("SPI_IOC_WR_BITS_PER_WORD", fd, SPI_IOC_WR_BITS_PER_WORD, p);
	}

	public static int getMaxSpeedHz(int fd) throws CException {
		IntByReference p = new IntByReference();
		CLib.ioctl("SPI_IOC_RD_MAX_SPEED_HZ", fd, SPI_IOC_RD_MAX_SPEED_HZ, p);
		return p.getValue();
	}

	public static void setMaxSpeedHz(int fd, int maxSpeedHz) throws CException {
		IntByReference p = new IntByReference(maxSpeedHz);
		CLib.ioctl("SPI_IOC_WR_MAX_SPEED_HZ", fd, SPI_IOC_WR_MAX_SPEED_HZ, p);
	}

	public static int getMode32(int fd) throws CException {
		IntByReference p = new IntByReference();
		CLib.ioctl("SPI_IOC_RD_MODE32", fd, SPI_IOC_RD_MODE32, p);
		return p.getValue();
	}

	public static void setMode32(int fd, int mode) throws CException {
		IntByReference p = new IntByReference(mode);
		CLib.ioctl("SPI_IOC_WR_MODE32", fd, SPI_IOC_WR_MODE32, p);
	}

}
