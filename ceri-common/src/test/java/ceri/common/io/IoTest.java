package ceri.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.data.ByteProvider;
import ceri.common.except.Exceptions;
import ceri.common.function.Excepts;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.ErrorGen;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestInputStream;
import ceri.common.test.Testing;
import ceri.common.text.Strings;
import ceri.common.util.SystemVars;

public class IoTest {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createFiles() throws IOException {
		helper = FileTestHelper.builder(SystemVars.tempDir()).file("a/a/a.txt", "aaa")
			.file("b/b.txt", "bbb").file("c.txt", "ccc").dir("d").build();
	}

	@AfterClass
	public static void deleteFiles() {
		helper.close();
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Io.class);
	}

	@Test
	public void testEolBytes() {
		Assert.equal(Io.eolBytes(null), Io.EOL_BYTES);
		Assert.equal(Io.eolBytes(Charset.defaultCharset()), Io.EOL_BYTES);
		Assert.array(Io.eolBytes(StandardCharsets.US_ASCII),
			Strings.EOL.getBytes(StandardCharsets.US_ASCII));
	}

	@Test
	public void testIoExceptionf() {
		Assert.equal(Exceptions.io("%s", "test").getMessage(), "test");
		Assert.equal(Exceptions.io(new Throwable(), "%s", "test").getMessage(), "test");
	}

	@Test
	public void testClearReader() throws IOException {
		Assert.equal(Io.clear(new StringReader(Strings.repeat('x', 0x41))), 0x41L);
	}

	@Test
	public void testClearInputStream() throws IOException {
		assertClearBuffer(new byte[0]);
		assertClearBuffer(new byte[100]);
		assertClearBuffer(new byte[32 * 1024]);
	}

	private void assertClearBuffer(byte[] buffer) throws IOException {
		var in = new ByteArrayInputStream(buffer);
		Assert.equal(Io.clear(in), (long) buffer.length);
		Assert.equal(in.available(), 0);
	}

	@Test
	public void testPollString() throws IOException {
		try (var in = Testing.inputStream("test")) {
			var s = Io.pollString(in);
			Assert.equal(s, "test");
		}
	}

	@Test
	public void testAvailableChar() throws IOException {
		try (var sys = SystemIo.of()) {
			sys.in(Testing.inputStream("test"));
			Assert.equal(Io.availableChar(), 't');
			Assert.equal(Io.availableChar(), 'e');
			Assert.equal(Io.availableChar(), 's');
			Assert.equal(Io.availableChar(), 't');
			sys.in().close();
			Assert.equal(Io.availableChar(), '\0');
		}
		try (var in = TestInputStream.of()) {
			in.available.error.setFrom(ErrorGen.IOX);
			Assert.equal(Io.availableChar(in), '\0');
		}
	}

	@Test
	public void testAvailableString() throws IOException {
		Assert.isNull(Io.availableString(null));
		try (var in = new ByteArrayInputStream("test".getBytes())) {
			Assert.equal(Io.availableString(in), "test");
			Assert.equal(Io.availableString(in), "");
		}
	}

	@Test
	public void testAvailableLine() throws IOException {
		Assert.isNull(Io.availableLine(null));
		var s = "te" + Strings.EOL + Strings.EOL + "s" + Strings.EOL + "t";
		try (var in = new ByteArrayInputStream(s.getBytes())) {
			Assert.equal(Io.availableLine(in), "te" + Strings.EOL);
			Assert.equal(Io.availableLine(in), Strings.EOL);
			Assert.equal(Io.availableLine(in), "s" + Strings.EOL);
			Assert.equal(Io.availableLine(in), "t");
		}
	}

	@Test
	public void testAvailableBytes() throws IOException {
		Assert.isNull(Io.availableBytes(null));
		try (var in = new ByteArrayInputStream(Array.BYTE.of(0, 1, 2, 3, 4))) {
			Assert.equal(Io.availableBytes(in), ByteProvider.of(0, 1, 2, 3, 4));
			Assert.equal(Io.availableBytes(in), ByteProvider.empty());
		}
		try (var in = IoStream.in((_, _, _) -> 0, () -> 3)) {
			Assert.equal(Io.availableBytes(in), ByteProvider.empty());
		}
	}

	@Test
	public void testAvailableBytesWithPredicate() throws IOException {
		Assert.isNull(Io.availableBytes(null, null));
		Excepts.ObjIntPredicate<RuntimeException, byte[]> p = (b, n) -> b[n - 1] == -1;
		try (var in = new ByteArrayInputStream(Array.BYTE.of(0, 1, -1, -1, 2, 3))) {
			Assert.equal(Io.availableBytes(in, p), ByteProvider.of(0, 1, -1));
			Assert.equal(Io.availableBytes(in, p), ByteProvider.of(-1));
			Assert.equal(Io.availableBytes(in, p), ByteProvider.of(2, 3));
			Assert.equal(Io.availableBytes(in, p), ByteProvider.empty());
		}
		try (var in = IoStream.in((_, _, _) -> 0, () -> 3)) {
			Assert.equal(Io.availableBytes(in, null), ByteProvider.of(0, 0, 0));
		}
		try (var in = IoStream.in((_, _, _) -> -1, () -> 3)) {
			Assert.equal(Io.availableBytes(in, null), ByteProvider.empty());
		}
	}

	@Test
	public void testReadBytes() throws IOException {
		Assert.equal(Io.readBytes(null, null), 0);
		try (var in = new ByteArrayInputStream(Array.BYTE.of(0, 1, 2, 3, 4))) {
			Assert.equal(Io.readBytes(in, null), 0);
			byte[] buffer = new byte[4];
			Io.readBytes(in, buffer);
			Assert.array(buffer, 0, 1, 2, 3);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testReadNext() throws IOException {
		Assert.isNull(Io.readNext(null));
		try (var in = TestInputStream.of()) {
			in.to.writeBytes(1, 2, 3);
			Assert.array(Io.readNext(in), 1, 2, 3);
			in.to.writeBytes(4);
			Assert.array(Io.readNext(in), 4);
			in.to.writeBytes(5);
			in.read.autoResponses(-1);
			Assert.array(Io.readNext(in));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testReadNextString() throws IOException {
		Assert.isNull(Io.readNext(null));
		try (var in = TestInputStream.of()) {
			in.to.writeBytes('a', 'b', 'c');
			Assert.equal(Io.readNextString(in), "abc");
			in.to.writeBytes('d');
			Assert.equal(Io.readNextString(in), "d");
			in.to.writeBytes(5);
			in.read.autoResponses(-1);
			Assert.equal(Io.readNextString(in), "");
		}
	}

	@Test
	public void testPollForData() throws IOException {
		int[] available = { 0 };
		try (var in = IoStream.in((IoStream.Read) null, () -> available[0])) {
			Assert.thrown(IoExceptions.Timeout.class, () -> Io.pollForData(in, 1, 1, 1));
			available[0] = 3;
			Assert.equal(Io.pollForData(in, 1, 0, 1), 3);
		}
	}

	@Test
	public void testPipe() throws IOException {
		var in = Testing.inputStream(1, 2, 3, 4, 5);
		var out = new ByteArrayOutputStream();
		Io.pipe(in, out);
		Assert.array(out.toByteArray(), 1, 2, 3, 4, 5);
	}

	@Test
	public void testPipeWithDelay() throws IOException {
		var read = CallSync.supplier(1, 0, 3, -1);
		try (var in = IoStream.in((_, _, _) -> read.get())) {
			var out = new ByteArrayOutputStream();
			Io.pipe(in, out, new byte[3], 0);
			Assert.array(out.toByteArray(), 0, 0, 0, 0);
		}
	}

	@Test
	public void testReadString() throws IOException {
		var in = Testing.inputStream("abc\0");
		Assert.equal(Io.readString(in), "abc\0");
	}

	@Test
	public void testLines() throws IOException {
		var in = Testing.inputStream("line0\n\nline2\nend");
		Assert.stream(Io.lines(in), "line0", "", "line2", "end");
	}
}
