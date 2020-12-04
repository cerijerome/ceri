package ceri.serial.spi.pulse;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.TestUtil.inputStream;
import static ceri.common.test.TestUtil.provider;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.test.CallSync;
import ceri.log.test.LogModifier;
import ceri.serial.spi.util.SpiEmulator;
import ceri.serial.spi.util.SpiEmulator.Responder;

public class SpiPulseTransmitterBehavior {
	private static final SpiPulseConfig config =
		SpiPulseConfig.builder(5).delayMicros(1).resetDelayMs(1).build();
	private CallSync.Accept<ByteProvider> sync;
	private SpiEmulator spi;
	private SpiPulseTransmitter xmit;

	@Before
	public void before() {
		sync = CallSync.consumer(null, false);
		spi = SpiEmulator.of(responder(sync));
		xmit = SpiPulseTransmitter.of(1, spi, config);
	}

	@After
	public void after() {
		xmit.close();
	}

	@Test
	public void shouldExposeParameters() {
		assertEquals(xmit.id(), 1);
		assertEquals(xmit.cycle(), PulseCycles.Std._4_9.cycle);
		assertEquals(xmit.length(), 5); // 4 pulse bits per data bit = 20 spi bytes
	}

	@Test
	public void shouldWriteDataAsPulses() {
		// Setting data to [0x01, 0xff, 0x00, 0x80, 0x7f]
		xmit.fill(0, 3, 0xff);
		xmit.setByte(0, 0x01);
		xmit.setBytes(2, 0x00, 0x80, 0x7f);
		xmit.send();
		sync.assertCall(provider(0x88, 0x88, 0x88, 0x8c, // 7 wide, 1 narrow
			0xcc, 0xcc, 0xcc, 0xcc, // 8 wide
			0x88, 0x88, 0x88, 0x88, // 8 narrow
			0xc8, 0x88, 0x88, 0x88, // 1 wide, 7 narrow
			0x8c, 0xcc, 0xcc, 0xcc)); // 1 narrow, 7 wide
		// PulsePrinter.of().print(sync.await().copy(0));
	}

	@Test
	public void shouldCopyDataAsPulses() throws IOException {
		// Setting data to [0x01, 0xff, 0x00, 0x80, 0x7f]
		xmit.copyFrom(0, provider(0x01, 0xff));
		xmit.readFrom(3, inputStream(0x80, 0x7f));
		xmit.send();
		sync.assertCall(provider(0x88, 0x88, 0x88, 0x8c, // 7 wide, 1 narrow
			0xcc, 0xcc, 0xcc, 0xcc, // 8 wide
			0x88, 0x88, 0x88, 0x88, // 8 narrow
			0xc8, 0x88, 0x88, 0x88, // 1 wide, 7 narrow
			0x8c, 0xcc, 0xcc, 0xcc)); // 1 narrow, 7 wide
	}

	@Test
	public void shouldHandleSpiFailures() {
		LogModifier.run(() -> {
			sync.error.setFrom(IOX);
			xmit.send();
			sync.await(); // error
			xmit.send();
			sync.await(); // error
			sync.error.clear();
			xmit.send();
			sync.await(); // no error
		}, Level.OFF, SpiPulseTransmitter.class);
	}

	private Responder responder(CallSync.Accept<ByteProvider> sync) {
		return new Responder() {
			@Override
			public void out(byte[] data) throws IOException {
				sync.accept(ByteArray.Immutable.wrap(data), IO_ADAPTER);
			}
		};
	}
}
