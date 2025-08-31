package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertString;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Test;
import ceri.common.data.ByteUtil;

public class BinaryPrinterBehavior {
	private StringBuilder b;
	private BinaryPrinter bin;

	@After
	public void after() {
		b = null;
		bin = null;
	}

	@Test
	public void shouldSpacesIfConfigured() {
		bin = init().printableSpace(true).showBinary(false).showHex(false).build();
		bin.print("a b c".getBytes(StandardCharsets.US_ASCII));
		bin = BinaryPrinter.builder(bin).printableSpace(false).build();
		bin.print("a b c".getBytes(StandardCharsets.US_ASCII));
		assertString(b, "a b c   \na.b.c   \n");
	}

	@Test
	public void shouldPrintToString() {
		assertEquals(new BinaryPrinter().toString(), new BinaryPrinter().toString());
		assertEquals(BinaryPrinter.builder().out(() -> null).build().toString(),
			BinaryPrinter.builder().out(() -> null).build().toString());
	}

	@Test
	public void shouldAllowCustomBufferSize() throws IOException {
		bin = init().bufferSize(1).bytesPerColumn(1).build();
		byte[] bytes = { 0, 0 };
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			bin.print(in, 1);
			assertEquals(b.toString(), "00000000  00  .\n");
		}
	}

	@Test
	public void shouldPrintUpperCase() {
		bin = init().showBinary(false).showChar(false).upper(true).build();
		bin.print(0xff, 0xaa, 0);
		assertEquals(b.toString(), "FF AA 00                 \n");
	}

	@Test
	public void shouldPrintSpaces() {
		bin = init().bytesPerColumn(4).showBinary(false).printableSpace(true).build();
		bin.print(0xff, 0x20, 0x00);
		assertEquals(b.toString(), "ff 20 00     . . \n");
	}

	@Test
	public void shouldPrintWithoutColumnSpaces() {
		bin = init().bytesPerColumn(4).columnSpace(false).build();
		bin.print(0xff, 0, 0x55);
		assertEquals(b.toString(), "111111110000000001010101         ff0055   ..U \n");
	}

	@Test
	public void shouldPrintByteBuffer() {
		bin = init().showBinary(false).build();
		ByteBuffer buffer = ByteBuffer.wrap(ByteUtil.toAscii("abc").copy(0));
		bin.print(buffer);
		assertEquals(b.toString(), "61 62 63                 abc     \n");
	}

	@Test
	public void shouldPrintByteArray() {
		bin = init().showBinary(false).build();
		bin.print(ByteUtil.toAscii("abc"));
		assertEquals(b.toString(), "61 62 63                 abc     \n");
	}

	@Test
	public void shouldPrintBytes() {
		bin = init().showBinary(false).build();
		bin.print('a', 'b', 'c');
		assertEquals(b.toString(), "61 62 63                 abc     \n");
	}

	@Test
	public void shouldPrintCodePoints() {
		bin = init().showBinary(false).build();
		bin.printCodePoints("abc\u2154");
		assertEquals(b.toString(), "00 61 00 62 00 63 21 54  .a.b.c!T\n");
	}

	@Test
	public void shouldPrintAscii() {
		bin = init().showBinary(false).build();
		bin.printAscii("abc\u2154");
		assertEquals(b.toString(), "61 62 63 3f              abc?    \n");
	}

	@Test
	public void shouldPrintUtf8() {
		bin = init().showBinary(false).build();
		bin.print("abc\u2154");
		assertEquals(b.toString(), "61 62 63 e2 85 94        abc...  \n");
	}

	@Test
	public void shouldPrintHex() {
		bin = init().showChar(false).build();
		byte[] bytes = { 0, 0x7f, -0x80, -1, 1, 0, 0, -0x7f };
		bin.print(bytes);
		assertEquals(b.toString(),
			"00000000 01111111 10000000 11111111 00000001 00000000 00000000 10000001  "
				+ "00 7f 80 ff 01 00 00 81  \n");
	}

	@Test
	public void shouldPrintFromInputStream() throws IOException {
		bin = init().showHex(false).build();
		byte[] bytes = { 0, 0x7f, -0x80, -1, 1, 0, 0, -0x7f };
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			bin.print(in);
			assertEquals(b.toString(),
				"00000000 01111111 10000000 11111111 00000001 00000000 00000000 10000001  "
					+ "........\n");
		}
	}

	@Test
	public void shouldPadColumns() throws IOException {
		bin = init().showBinary(false).bytesPerColumn(3).columns(2).build();
		byte[] bytes = { 'A', 'a', '~', '!' };
		var in = new ByteArrayInputStream(bytes);
		bin.print(in).flush();
		assertEquals(b.toString(), "41 61 7e  21        Aa~ !  \n");
	}

	private BinaryPrinter.Builder init() {
		b = new StringBuilder();
		return BinaryPrinter.builder().out(b);
	}
}
