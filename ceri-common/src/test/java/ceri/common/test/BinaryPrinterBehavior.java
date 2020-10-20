package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.text.StringUtil.asPrintStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import ceri.common.data.ByteUtil;

@SuppressWarnings("resource")
public class BinaryPrinterBehavior {

	@Test
	public void shouldSpacesIfConfigured() {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().printableSpace(true).out(asPrintStream(b))
			.showBinary(false).showHex(false).build();
		bin.print("a b c".getBytes(StandardCharsets.US_ASCII));
		bin = BinaryPrinter.builder(bin).printableSpace(false).build();
		bin.print("a b c".getBytes(StandardCharsets.US_ASCII));
		assertEquals(b.toString(), "a b c   \na.b.c   \n");
	}

	@Test
	public void shouldPrintToString() {
		assertEquals(new BinaryPrinter().toString(), new BinaryPrinter().toString());
		assertEquals(BinaryPrinter.builder().out(null).build().toString(),
			BinaryPrinter.builder().out(null).build().toString());
	}

	@Test
	public void shouldAllowCustomBufferSize() throws IOException {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin =
			BinaryPrinter.builder().out(asPrintStream(b)).bufferSize(1).bytesPerColumn(1).build();
		byte[] bytes = { 0, 0 };
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			bin.print(in, 1);
			assertEquals(b.toString(), "00000000  00  .\n");
		}
	}

	@Test
	public void shouldPrintUpperCase() {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().out(asPrintStream(b)).showBinary(false)
			.showChar(false).upper(true).build();
		bin.print(0xff, 0xaa, 0);
		assertEquals(b.toString(), "FF AA 00                 \n");
	}

	@Test
	public void shouldPrintSpaces() {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().out(asPrintStream(b)).bytesPerColumn(4)
			.showBinary(false).printableSpace(true).build();
		bin.print(0xff, 0x20, 0x00);
		assertEquals(b.toString(), "ff 20 00     . . \n");
	}

	@Test
	public void shouldPrintWithoutColumnSpaces() {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().out(asPrintStream(b)).bytesPerColumn(4)
			.columnSpace(false).build();
		bin.print(0xff, 0, 0x55);
		assertEquals(b.toString(), "111111110000000001010101         ff0055   ..U \n");
	}

	@Test
	public void shouldPrintByteBuffer() {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().out(asPrintStream(b)).showBinary(false).build();
		ByteBuffer buffer = ByteBuffer.wrap(ByteUtil.toAscii("abc").copy(0));
		bin.print(buffer);
		assertEquals(b.toString(), "61 62 63                 abc     \n");
	}

	@Test
	public void shouldPrintByteArray() {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().out(asPrintStream(b)).showBinary(false).build();
		bin.print(ByteUtil.toAscii("abc"));
		assertEquals(b.toString(), "61 62 63                 abc     \n");
	}

	@Test
	public void shouldPrintBytes() {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().out(asPrintStream(b)).showBinary(false).build();
		bin.print('a', 'b', 'c');
		assertEquals(b.toString(), "61 62 63                 abc     \n");
	}

	@Test
	public void shouldPrintCodePoints() {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().out(asPrintStream(b)).showBinary(false).build();
		bin.printCodePoints("abc\u2154");
		assertEquals(b.toString(), "00 61 00 62 00 63 21 54  .a.b.c!T\n");
	}

	@Test
	public void shouldPrintAscii() {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().out(asPrintStream(b)).showBinary(false).build();
		bin.printAscii("abc\u2154");
		assertEquals(b.toString(), "61 62 63 3f              abc?    \n");
	}

	@Test
	public void shouldPrintUtf8() {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().out(asPrintStream(b)).showBinary(false).build();
		bin.print("abc\u2154");
		assertEquals(b.toString(), "61 62 63 e2 85 94        abc...  \n");
	}

	@Test
	public void shouldPrintHex() {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().out(asPrintStream(b)).showChar(false).build();
		byte[] bytes = { 0, 0x7f, -0x80, -1, 1, 0, 0, -0x7f };
		bin.print(bytes);
		assertEquals(b.toString(),
			"00000000 01111111 10000000 11111111 00000001 00000000 00000000 10000001  " +
				"00 7f 80 ff 01 00 00 81  \n");
	}

	@Test
	public void shouldPrintFromInputStream() throws IOException {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().out(asPrintStream(b)).showHex(false).build();
		byte[] bytes = { 0, 0x7f, -0x80, -1, 1, 0, 0, -0x7f };
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			bin.print(in);
			assertEquals(b.toString(),
				"00000000 01111111 10000000 11111111 00000001 00000000 00000000 10000001  " +
					"........\n");
		}
	}

	@Test
	public void shouldPadColumns() throws IOException {
		StringBuilder b = new StringBuilder();
		BinaryPrinter bin = BinaryPrinter.builder().out(asPrintStream(b)).showBinary(false)
			.bytesPerColumn(3).columns(2).build();
		byte[] bytes = { 'A', 'a', '~', '!' };
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			bin.print(in).flush();
			assertEquals(b.toString(), "41 61 7e  21        Aa~ !  \n");
		}
	}

}
