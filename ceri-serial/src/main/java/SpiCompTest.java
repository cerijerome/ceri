

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * Comparison test for SPI on Raspberry Pi. Runs with jna-4.2.2.jar, available from
 * http://repo1.maven.org/maven2/net/java/dev/jna/jna/4.2.2/
 */
public class SpiCompTest {
    private static final CLib CLIB = (CLib) Native.loadLibrary(null, CLib.class);
    private static final int O_WRONLY = 0x0001;
    private static final int SPI_IOC_MESSAGE_1 = 0x40206b00;
	//private static final int SPI_IOC_WR_MODE = 0x40016b01;
	//private static final int SPI_IOC_RD_MODE = 0x80016b01;
	private static final int SPI_IOC_WR_LSB_FIRST = 0x40016b02;
	private static final int SPI_IOC_WR_BITS_PER_WORD = 0x40016b03;
	private static final int SPI_IOC_WR_MAX_SPEED_HZ = 0x40046b04;
    private static final int SPI_IOC_WR_MODE32 = 0x40046b05;

    static interface CLib extends Library {
        int ioctl(int fd, int request, Object... objs);
        int open(String path, int flags);
        int close(int fd);
    }

    public static class spi_ioc_transfer extends Structure {
        public long tx_buf;
        public long rx_buf = 0L;
        public int len;
        public int speed_hz;
        public short delay_usecs = 0;
        public byte bits_per_word = 8;
        public byte cs_change = 0;
        public byte tx_nbits = 0;
        public byte rx_nbits = 0;
        public short pad = 0;

        public spi_ioc_transfer() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("tx_buf", "rx_buf", "len", "speed_hz", "delay_usecs", "bits_per_word",
                "cs_change", "tx_nbits", "rx_nbits", "pad");
        }
    }

    private static int verify(int result) throws IOException {
        if (result >= 0) return result;
        throw new IOException("JNA call error: " + result);
    }

    private static spi_ioc_transfer initTransfer(Memory mem, byte fill, int speedHz) {
        ByteBuffer buffer = mem.getByteBuffer(0, mem.size());
        for (int i = 0; i < mem.size(); i++)
            buffer.put(fill);
        spi_ioc_transfer xfer = new spi_ioc_transfer();
        xfer.tx_buf = Pointer.nativeValue(mem);
        xfer.len = (int) mem.size();
        xfer.speed_hz = speedHz;
        xfer.bits_per_word = 8;
        return xfer;
    }

    private static void run(int bus, int chip, int mode, int speedHz, int size, int repeats,
        int fill) throws IOException {
        Memory mem = new Memory(size);
        spi_ioc_transfer xfer = initTransfer(mem, (byte) fill, speedHz);

        String path = String.format("/dev/spidev%d.%d", bus, chip);
        System.out.printf("%s mode=0x%02x speed=%dkHz size=%d(0x%02x) x%d: ",
            path, mode,    speedHz / 1000, size, fill, repeats);

        int fd = -1;
        try {
            fd = verify(CLIB.open(path, O_WRONLY));
            verify(CLIB.ioctl(fd, SPI_IOC_WR_MODE32, new IntByReference(mode)));
            verify(CLIB.ioctl(fd, SPI_IOC_WR_BITS_PER_WORD, new ByteByReference((byte) 8)));
            verify(CLIB.ioctl(fd, SPI_IOC_WR_LSB_FIRST, new ByteByReference((byte) 0)));
            verify(CLIB.ioctl(fd, SPI_IOC_WR_MAX_SPEED_HZ, new IntByReference(speedHz)));
            
            long t0 = System.currentTimeMillis();
            for (int i = 0; i < repeats; i++)
                verify(CLIB.ioctl(fd, SPI_IOC_MESSAGE_1, xfer));
            long t1 = System.currentTimeMillis();

            System.out.printf("%.2fs%n", (t1 - t0) / 1000.0);
        } finally {
            if (fd >= 0) CLIB.close(fd);
        }
    }

    private static int param(String[] args, int index, int def) {
        return args.length > index ? Integer.decode(args[index]) : def;
    }
    
    public static void main(String[] args) throws IOException {
        int i = 0;
        int bus = param(args, i++, 0);
        int chip = param(args, i++, 0);
        int mode = param(args, i++, 0);
        int speedHz = param(args, i++, 100000);
        int size = param(args, i++, 100);
        int repeats = param(args, i++, (speedHz / size) / 2);
        int fill = param(args, i++, 0xff);

        run(bus, chip, mode, speedHz, size, repeats, fill);
    }

}
