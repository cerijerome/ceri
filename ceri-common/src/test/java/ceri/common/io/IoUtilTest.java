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
import ceri.common.array.ArrayUtil;
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

public class IoUtilTest {
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
		Assert.privateConstructor(IoUtil.class);
	}

	@Test
	public void testEolBytes() {
		Assert.equal(IoUtil.eolBytes(null), IoUtil.EOL_BYTES);
		Assert.equal(IoUtil.eolBytes(Charset.defaultCharset()), IoUtil.EOL_BYTES);
		Assert.array(IoUtil.eolBytes(StandardCharsets.US_ASCII),
			Strings.EOL.getBytes(StandardCharsets.US_ASCII));
	}

	@Test
	public void testIoExceptionf() {
		Assert.equal(Exceptions.io("%s", "test").getMessage(), "test");
		Assert.equal(Exceptions.io(new Throwable(), "%s", "test").getMessage(), "test");
	}

	@Test
	public void testClearReader() throws IOException {
		Assert.equal(IoUtil.clear(new StringReader(Strings.repeat('x', 0x41))), 0x41L);
	}

	@Test
	public void testClearInputStream() throws IOException {
		assertClearBuffer(new byte[0]);
		assertClearBuffer(new byte[100]);
		assertClearBuffer(new byte[32 * 1024]);
	}

	private void assertClearBuffer(byte[] buffer) throws IOException {
		var in = new ByteArrayInputStream(buffer);
		Assert.equal(IoUtil.clear(in), (long) buffer.length);
		Assert.equal(in.available(), 0);
	}

	@Test
	public void testPollString() throws IOException {
		try (var in = Testing.inputStream("test")) {
			var s = IoUtil.pollString(in);
			Assert.equal(s, "test");
		}
	}

	@Test
	public void testAvailableChar() throws IOException {
		try (var sys = SystemIo.of()) {
			sys.in(Testing.inputStream("test"));
			Assert.equal(IoUtil.availableChar(), 't');
			Assert.equal(IoUtil.availableChar(), 'e');
			Assert.equal(IoUtil.availableChar(), 's');
			Assert.equal(IoUtil.availableChar(), 't');
			sys.in().close();
			Assert.equal(IoUtil.availableChar(), '\0');
		}
		try (var in = TestInputStream.of()) {
			in.available.error.setFrom(ErrorGen.IOX);
			Assert.equal(IoUtil.availableChar(in), '\0');
		}
	}

	@Test
	public void testAvailableString() throws IOException {
		Assert.isNull(IoUtil.availableString(null));
		try (var in = new ByteArrayInputStream("test".getBytes())) {
			Assert.equal(IoUtil.availableString(in), "test");
			Assert.equal(IoUtil.availableString(in), "");
		}
	}

	@Test
	public void testAvailableLine() throws IOException {
		Assert.isNull(IoUtil.availableLine(null));
		var s = "te" + Strings.EOL + Strings.EOL + "s" + Strings.EOL + "t";
		try (var in = new ByteArrayInputStream(s.getBytes())) {
			Assert.equal(IoUtil.availableLine(in), "te" + Strings.EOL);
			Assert.equal(IoUtil.availableLine(in), Strings.EOL);
			Assert.equal(IoUtil.availableLine(in), "s" + Strings.EOL);
			Assert.equal(IoUtil.availableLine(in), "t");
		}
	}

	@Test
	public void testAvailableBytes() throws IOException {
		Assert.isNull(IoUtil.availableBytes(null));
		try (var in = new ByteArrayInputStream(ArrayUtil.bytes.of(0, 1, 2, 3, 4))) {
			Assert.equal(IoUtil.availableBytes(in), ByteProvider.of(0, 1, 2, 3, 4));
			Assert.equal(IoUtil.availableBytes(in), ByteProvider.empty());
		}
		try (var in = IoStream.in((_, _, _) -> 0, () -> 3)) {
			Assert.equal(IoUtil.availableBytes(in), ByteProvider.empty());
		}
	}

	@Test
	public void testAvailableBytesWithPredicate() throws IOException {
		Assert.isNull(IoUtil.availableBytes(null, null));
		Excepts.ObjIntPredicate<RuntimeException, byte[]> p = (b, n) -> b[n - 1] == -1;
		try (var in = new ByteArrayInputStream(ArrayUtil.bytes.of(0, 1, -1, -1, 2, 3))) {
			Assert.equal(IoUtil.availableBytes(in, p), ByteProvider.of(0, 1, -1));
			Assert.equal(IoUtil.availableBytes(in, p), ByteProvider.of(-1));
			Assert.equal(IoUtil.availableBytes(in, p), ByteProvider.of(2, 3));
			Assert.equal(IoUtil.availableBytes(in, p), ByteProvider.empty());
		}
		try (var in = IoStream.in((_, _, _) -> 0, () -> 3)) {
			Assert.equal(IoUtil.availableBytes(in, null), ByteProvider.of(0, 0, 0));
		}
		try (var in = IoStream.in((_, _, _) -> -1, () -> 3)) {
			Assert.equal(IoUtil.availableBytes(in, null), ByteProvider.empty());
		}
	}

	@Test
	public void testReadBytes() throws IOException {
		Assert.equal(IoUtil.readBytes(null, null), 0);
		try (var in = new ByteArrayInputStream(ArrayUtil.bytes.of(0, 1, 2, 3, 4))) {
			Assert.equal(IoUtil.readBytes(in, null), 0);
			byte[] buffer = new byte[4];
			IoUtil.readBytes(in, buffer);
			Assert.array(buffer, 0, 1, 2, 3);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testReadNext() throws IOException {
		Assert.isNull(IoUtil.readNext(null));
		try (var in = TestInputStream.of()) {
			in.to.writeBytes(1, 2, 3);
			Assert.array(IoUtil.readNext(in), 1, 2, 3);
			in.to.writeBytes(4);
			Assert.array(IoUtil.readNext(in), 4);
			in.to.writeBytes(5);
			in.read.autoResponses(-1);
			Assert.array(IoUtil.readNext(in));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testReadNextString() throws IOException {
		Assert.isNull(IoUtil.readNext(null));
		try (var in = TestInputStream.of()) {
			in.to.writeBytes('a', 'b', 'c');
			Assert.equal(IoUtil.readNextString(in), "abc");
			in.to.writeBytes('d');
			Assert.equal(IoUtil.readNextString(in), "d");
			in.to.writeBytes(5);
			in.read.autoResponses(-1);
			Assert.equal(IoUtil.readNextString(in), "");
		}
	}

	@Test
	public void testPollForData() throws IOException {
		int[] available = { 0 };
		try (var in = IoStream.in((IoStream.Read) null, () -> available[0])) {
			Assert.thrown(IoExceptions.Timeout.class, () -> IoUtil.pollForData(in, 1, 1, 1));
			available[0] = 3;
			Assert.equal(IoUtil.pollForData(in, 1, 0, 1), 3);
		}
	}

	@Test
	public void testPipe() throws IOException {
		var in = Testing.inputStream(1, 2, 3, 4, 5);
		var out = new ByteArrayOutputStream();
		IoUtil.pipe(in, out);
		Assert.array(out.toByteArray(), 1, 2, 3, 4, 5);
	}

	@Test
	public void testPipeWithDelay() throws IOException {
		var read = CallSync.supplier(1, 0, 3, -1);
		try (var in = IoStream.in((_, _, _) -> read.get())) {
			var out = new ByteArrayOutputStream();
			IoUtil.pipe(in, out, new byte[3], 0);
			Assert.array(out.toByteArray(), 0, 0, 0, 0);
		}
	}

	@Test
	public void testReadString() throws IOException {
		var in = Testing.inputStream("abc\0");
		Assert.equal(IoUtil.readString(in), "abc\0");
	}

	@Test
	public void testLines() throws IOException {
		var in = Testing.inputStream("line0\n\nline2\nend");
		Assert.stream(IoUtil.lines(in), "line0", "", "line2", "end");
	}
}
