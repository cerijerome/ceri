package ceri.common.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import ceri.common.util.StringUtil;

public class HexPrinterBehavior {

	@Test
	public void shouldPrintHex() {
		StringBuilder b = new StringBuilder();
		HexPrinter hex = new HexPrinter(StringUtil.asPrintStream(b), 1, false);
		byte[] bytes = { 0, 0x7f, -0x80, -1, 1, 0, 0, -0x7f }; 
		hex.print(bytes);
		assertThat(b.toString(), is("00 7F 80 FF 01 00 00 81  ........\n"));
	}

	@Test
	public void shouldPrintFromInputStream() throws IOException {
		StringBuilder b = new StringBuilder();
		HexPrinter hex = new HexPrinter(StringUtil.asPrintStream(b), 1, false);
		byte[] bytes = { 0, 0x7f, -0x80, -1, 1, 0, 0, -0x7f };
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			hex.print(in);
			assertThat(b.toString(), is("00 7F 80 FF 01 00 00 81  ........\n"));
		}
	}

	@Test
	public void shouldPadColumns() throws IOException {
		StringBuilder b = new StringBuilder();
		HexPrinter hex = new HexPrinter(StringUtil.asPrintStream(b), 2, true);
		byte[] bytes = { 0, 0x7f, -0x80, -1, 1, 0, 0, -0x7f, 'A', 'a', '~', '!' };
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			hex.print(in);
			assertThat(b.toString(), is("00 7F 80 FF 01 00 00 81  41 61 7E 21            " +
				"  ........ Aa~!    \n"));
		}
	}

}
