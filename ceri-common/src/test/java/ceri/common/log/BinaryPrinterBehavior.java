package ceri.common.log;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import ceri.common.util.StringUtil;

public class BinaryPrinterBehavior {

	@Test
	public void shouldPrintToString() {
		String s = new BinaryPrinter().toString();
		assertThat(s, is("BinaryPrinter(PrintStream,8,1,true,true,true)"));
	}

	@Test
	public void shouldPrintHex() {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin =
			BinaryPrinter.builder().out(StringUtil.asPrintStream(b)).showChar(false).build();
		byte[] bytes = { 0, 0x7f, -0x80, -1, 1, 0, 0, -0x7f };
		bin.print(bytes);
		assertThat(b.toString(),
			is("00000000 01111111 10000000 11111111 00000001 00000000 00000000 10000001  "
				+ "00 7F 80 FF 01 00 00 81  \n"));
	}

	@Test
	public void shouldPrintFromInputStream() throws IOException {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin =
			BinaryPrinter.builder().out(StringUtil.asPrintStream(b)).showHex(false).build();
		byte[] bytes = { 0, 0x7f, -0x80, -1, 1, 0, 0, -0x7f };
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			bin.print(in);
			assertThat(b.toString(),
				is("00000000 01111111 10000000 11111111 00000001 00000000 00000000 10000001  "
					+ "........\n"));
		}
	}

	@Test
	public void shouldPadColumns() throws IOException {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin =
			BinaryPrinter.builder().out(StringUtil.asPrintStream(b)).showBinary(false)
				.bytesPerColumn(3).columns(2).build();
		byte[] bytes = { 'A', 'a', '~', '!' };
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			bin.print(in);
			assertThat(b.toString(), is("41 61 7E  21        Aa~ !  \n"));
		}
	}

}