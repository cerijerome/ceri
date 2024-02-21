package ceri.serial.spi.test;

import java.io.IOException;
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
	public final CallSync.Function<Integer, ByteProvider> in;
	public final CallSync.Consumer<ByteProvider> out;

	private static class IoResponder implements SpiEmulator.Responder {
		private final CallSync.Function<Integer, ByteProvider> in =
			CallSync.function(0, ByteProvider.empty());
		private final CallSync.Consumer<ByteProvider> out = CallSync.consumer(null, true);

		@Override
		public byte[] in(int size) throws IOException {
			return in.apply(size, IoUtil.IO_ADAPTER).copy(0);
		}

		@Override
		public void out(byte[] data) throws IOException {
			out.accept(ByteProvider.of(data), IoUtil.IO_ADAPTER);
		}

		@Override
		public byte[] duplex(byte[] data) throws IOException {
			out(data);
			return in(data.length);
		}
	}

	public static TestSpi of() {
		return new TestSpi(new IoResponder());
	}

	private TestSpi(IoResponder responder) {
		super(responder);
		this.in = responder.in;
		this.out = responder.out;
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
