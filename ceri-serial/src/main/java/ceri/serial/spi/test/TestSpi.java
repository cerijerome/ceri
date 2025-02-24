package ceri.serial.spi.test;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteProvider;
import ceri.common.io.Direction;
import ceri.common.io.IoUtil;
import ceri.common.test.CallSync;
import ceri.common.test.ErrorGen;
import ceri.serial.spi.SpiMode;
import ceri.serial.spi.SpiTransfer;
import ceri.serial.spi.util.SpiEmulator;

public class TestSpi extends SpiEmulator {
	public final ErrorGen error = ErrorGen.of();
	public final CallSync.Function<Request, ByteProvider> xfer;

	public record Request(int n, ByteProvider data) {
		public static final Request NULL = new Request(0, null);
		
		public static Request in(int size) {
			return new Request(size, ByteProvider.empty());
		}
		
		public static Request out(int... data) {
			return out(ArrayUtil.bytes(data));
		}
		
		public static Request out(byte[] data) {
			return new Request(0, ByteProvider.of(data));
		}
		
		public static Request duplex(int... data) {
			return duplex(ArrayUtil.bytes(data));
		}
		
		public static Request duplex(byte[] data) {
			return new Request(data.length, ByteProvider.copyOf(data));
		}
	}

	private static class IoResponder implements SpiEmulator.Responder {
		private final CallSync.Function<Request, ByteProvider> xfer =
			CallSync.function(Request.NULL, ByteProvider.empty());

		@Override
		public byte[] in(int size) throws IOException {
			return xfer.apply(Request.in(size), IO_ADAPTER).copy(0);
		}

		@Override
		public void out(byte[] data) throws IOException {
			xfer.apply(Request.out(data), IO_ADAPTER);
		}

		@Override
		public byte[] duplex(byte[] data) throws IOException {
			return xfer.apply(Request.duplex(data), IO_ADAPTER).copy(0);
		}
	}

	public static TestSpi of() {
		return new TestSpi(new IoResponder());
	}

	private TestSpi(IoResponder responder) {
		super(responder);
		xfer = responder.xfer;
		delay(false);
	}

	@Override
	public SpiMode mode() throws IOException {
		error.call(IoUtil.IO_ADAPTER);
		return super.mode();
	}

	@Override
	public void mode(SpiMode mode) throws IOException {
		error.call(IoUtil.IO_ADAPTER);
		super.mode(mode);
	}

	@Override
	public boolean lsbFirst() throws IOException {
		error.call(IoUtil.IO_ADAPTER);
		return super.lsbFirst();
	}

	@Override
	public void lsbFirst(boolean enabled) throws IOException {
		error.call(IoUtil.IO_ADAPTER);
		super.lsbFirst(enabled);
	}

	@Override
	public int bitsPerWord() throws IOException {
		error.call(IoUtil.IO_ADAPTER);
		return super.bitsPerWord();
	}

	@Override
	public void bitsPerWord(int bitsPerWord) throws IOException {
		error.call(IoUtil.IO_ADAPTER);
		super.bitsPerWord(bitsPerWord);
	}

	@Override
	public int maxSpeedHz() throws IOException {
		error.call(IoUtil.IO_ADAPTER);
		return super.maxSpeedHz();
	}

	@Override
	public void maxSpeedHz(int maxSpeedHz) throws IOException {
		error.call(IoUtil.IO_ADAPTER);
		super.maxSpeedHz(maxSpeedHz);
	}

	@Override
	public SpiTransfer transfer(Direction direction, int size) {
		error.call();
		return super.transfer(direction, size);
	}
}
