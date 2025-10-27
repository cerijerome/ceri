package ceri.serial.spi.pulse;

import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.TestUtil.inputStream;
import static ceri.common.test.TestUtil.provider;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.log.test.LogModifier;
import ceri.serial.spi.util.SpiEmulator;
import ceri.serial.spi.util.SpiEmulator.Responder;

public class SpiPulseTransmitterBehavior {
	private static final SpiPulseConfig config =
		SpiPulseConfig.builder(5).delayMicros(1).resetDelayMs(1).build();
	private CallSync.Consumer<ByteProvider> sync;
	private SpiEmulator spi;
	private SpiPulseTransmitter spix;

	@After
	public void after() {
		Closeables.close(spix);
		spix = null;
	}

	@Test
	public void shouldExposeParameters() {
		init();
		Assert.equal(spix.id(), 1);
		Assert.equal(spix.cycle(), PulseCycle.Std._4_9.cycle);
		Assert.equal(spix.length(), 5); // 4 pulse bits per data bit = 20 spi bytes
	}

	@Test
	public void shouldProvideStringRepresentation() {
		init();
		Assert.find(spix, "4nbit9");
	}

	@Test
	public void shouldIgnoreEmptyWrites() {
		init();
		spix.copyFrom(0, new byte[0]);
		spix.copyFrom(0, provider());
		spix.fill(0, 0, 0);
	}

	@Test
	public void shouldWriteDataAsPulses() {
		init();
		// Setting data to [0x01, 0xff, 0x00, 0x80, 0x7f]
		spix.fill(0, 3, 0xff);
		spix.setByte(0, 0x01);
		spix.setBytes(2, 0x00, 0x80, 0x7f);
		spix.send();
		sync.assertCall(provider(0x88, 0x88, 0x88, 0x8c, // 7 wide, 1 narrow
			0xcc, 0xcc, 0xcc, 0xcc, // 8 wide
			0x88, 0x88, 0x88, 0x88, // 8 narrow
			0xc8, 0x88, 0x88, 0x88, // 1 wide, 7 narrow
			0x8c, 0xcc, 0xcc, 0xcc)); // 1 narrow, 7 wide
		// PulsePrinter.of().print(sync.await().copy(0));
	}

	@Test
	public void shouldCopyDataAsPulses() throws IOException {
		init();
		// Setting data to [0x01, 0xff, 0x00, 0x80, 0x7f]
		spix.copyFrom(0, provider(0x01, 0xff));
		spix.readFrom(3, inputStream(0x80, 0x7f));
		spix.send();
		sync.assertCall(provider(0x88, 0x88, 0x88, 0x8c, // 7 wide, 1 narrow
			0xcc, 0xcc, 0xcc, 0xcc, // 8 wide
			0x88, 0x88, 0x88, 0x88, // 8 narrow
			0xc8, 0x88, 0x88, 0x88, // 1 wide, 7 narrow
			0x8c, 0xcc, 0xcc, 0xcc)); // 1 narrow, 7 wide
	}

	@Test
	public void shouldHandleSpiFailures() {
		init();
		LogModifier.run(() -> {
			sync.error.setFrom(IOX);
			spix.send();
			sync.await(); // error
			spix.send();
			sync.await(); // error
			sync.error.clear();
			spix.send();
			sync.await(); // no error
		}, Level.OFF, SpiPulseTransmitter.class);
	}

	private Responder responder(CallSync.Consumer<ByteProvider> sync) {
		return new Responder() {
			@Override
			public void out(byte[] data) throws IOException {
				sync.accept(ByteProvider.of(data), ExceptionAdapter.io);
			}
		};
	}

	private void init() {
		sync = CallSync.consumer(null, false);
		spi = SpiEmulator.of(responder(sync)).delay(false);
		spix = SpiPulseTransmitter.of(1, spi, config);
	}
}
