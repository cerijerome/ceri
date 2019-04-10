package ceri.serial.pigpio;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.log.util.LogUtil;
import ceri.serial.pigpio.jna.Pigpio;

public class PigpioSpi implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final int handle;
	public final PigpioSpiChannel channel;
	public final int baudHz;
	public final PigpioSpiFlags flags;
	
	public static PigpioSpi open(PigpioSpiChannel channel, int baudHz, PigpioSpiFlags flags) 
		throws IOException {
		int handle = Pigpio.spiOpen(channel.value, baudHz, flags.value);
		return new PigpioSpi(handle, channel, baudHz, flags);
	}
	
	private PigpioSpi(int handle, PigpioSpiChannel channel, int baudHz, PigpioSpiFlags flags) {
		this.handle = handle;
		this.channel = channel;
		this.baudHz = baudHz;
		this.flags = flags;
	}
	
	public int read(ByteBuffer buffer) throws IOException {
		return Pigpio.spiRead(handle, buffer, buffer.remaining());
	}
	
	public int write(ByteBuffer buffer) throws IOException {
		return Pigpio.spiWrite(handle, buffer, buffer.remaining());
	}
	
	public int xfer(ByteBuffer txBuffer, ByteBuffer rxBuffer) throws IOException {
		return Pigpio.spiXfer(handle, txBuffer, rxBuffer, txBuffer.remaining());
	}
	
	@Override
	public void close() {
		if (handle >= 0) LogUtil.execute(logger, () -> Pigpio.spiClose(handle));
	}
}
