package ceri.common.test;

import org.junit.Test;
import ceri.common.io.SystemIo;
import ceri.common.text.StringBuilders;

public class PulsePrinterBehavior {

	@Test
	public void shouldPrintToStdOut() {
		Assert.equal(stdOut(() -> {
			PulsePrinter p = PulsePrinter.of();
			p.print(true).print(false);
		}), "\u2587\u2581");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldPrintToGivenPrintStream() {
		StringBuilder b = new StringBuilder();
		PulsePrinter p = PulsePrinter.builder().out(StringBuilders.printStream(b)).build();
		p.print(true).print(false);
		Assert.equal(b.toString(), "\u2587\u2581");
	}

	@Test
	public void shouldPrintUpToGivenBytesPerLine() {
		Assert.equal(stdOut(() -> {
			PulsePrinter p = PulsePrinter.ofBytes(1);
			p.print(0, 0xff);
		}), "\u2581\u2581\u2581\u2581\u2581\u2581\u2581\u2581\n"
			+ "\u2587\u2587\u2587\u2587\u2587\u2587\u2587\u2587\n");
	}

	@Test
	public void shouldPrintUpToGivenBitsPerLine() {
		Assert.equal(stdOut(() -> {
			PulsePrinter p = PulsePrinter.ofBits(3);
			p.print(0x77);
		}), "\u2581\u2587\u2587\n\u2587\u2581\u2587\n\u2587\u2587");
	}

	@Test
	public void shouldPrintBitsMsbFirst() {
		Assert.equal(stdOut(() -> {
			PulsePrinter p = PulsePrinter.builder().high('1').low('0').build();
			p.print(true).print(false).print(0xf0, 0x33).printBit(0x3, 1).printBit(0x3, 6);
		}), "10111100000011001101");
	}

	@Test
	public void shouldPrintBitsLsbFirst() {
		Assert.equal(stdOut(() -> {
			PulsePrinter p = PulsePrinter.builder().high('1').low('0').lsbFirst().build();
			p.print(true).print(false).print(0xf0, 0x33).printBit(0x3, 1).printBit(0x3, 6);
		}), "10000011111100110010");
	}

	@SuppressWarnings("resource")
	private String stdOut(Runnable runnable) {
		StringBuilder b = new StringBuilder();
		try (SystemIo sys = SystemIo.of()) {
			sys.out(StringBuilders.printStream(b));
			runnable.run();
			return b.toString();
		}
	}

}
