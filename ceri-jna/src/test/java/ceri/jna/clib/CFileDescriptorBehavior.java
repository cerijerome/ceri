package ceri.jna.clib;

import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.jna.clib.CFileDescriptor.Opener;
import ceri.jna.clib.FileDescriptor.Open;
import ceri.jna.clib.Mode.Mask;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.util.JnaLibrary;
import ceri.log.test.LogModifier;
import ceri.log.util.Logs;

public class CFileDescriptorBehavior {
	private final JnaLibrary.Ref<TestCLibNative> ref = TestCLibNative.ref();
	private CFileDescriptor fd;

	@After
	public void after() {
		Closeables.close(fd, ref);
		fd = null;
	}

	@Test
	public void testIsBroken() {
		Assert.no(CFileDescriptor.isBroken(null));
		Assert.no(CFileDescriptor.isBroken(new IOException("remote i/o")));
		Assert.no(CFileDescriptor.isBroken(ErrNo.UNDEFINED.error("test")));
		Assert.no(CFileDescriptor.isBroken(ErrNo.EPERM.error("test")));
		Assert.yes(CFileDescriptor.isBroken(ErrNo.ENOENT.error("test")));
		Assert.yes(CFileDescriptor.isBroken(ErrNo.EPERM.error("remote i/o")));
		Assert.yes(CFileDescriptor.isBroken(ErrNo.UNDEFINED.error("remote i/o")));
	}

	@Test
	public void shouldOpenWithMode() throws IOException {
		var lib = ref.init();
		try (var _ = CFileDescriptor.open("test", Mode.of(Mask.rwxo), Open.APPEND)) {
			lib.open.assertCall(
				new TestCLibNative.OpenArgs("test", Open.APPEND.value, Mask.rwxo.value));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldOpenWithOpener() throws IOException {
		var lib = ref.init();
		new Opener("test", Mode.of(0567), Open.CREAT).get();
		lib.open.assertCall(new TestCLibNative.OpenArgs("test", Open.CREAT.value, 0567));
		new Opener("test", Mode.of(0756), List.of(Open.APPEND)).get();
		lib.open.assertCall(new TestCLibNative.OpenArgs("test", Open.APPEND.value, 0756));
	}

	@Test
	public void shouldNotBreachOpenerEqualsContract() {
		var t = new Opener("test", Mode.of(0767), Open.RDWR);
		var eq0 = new Opener("test", Mode.of(0767), List.of(Open.RDWR));
		var ne0 = new Opener("Test", Mode.of(0767), Open.RDWR);
		var ne1 = new Opener("test", Mode.of(0777), Open.RDWR);
		var ne2 = new Opener("test", Mode.of(0767), Open.RDONLY);
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2);
		Testing.exerciseRecord(t);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBreachEqualsContract() throws IOException {
		init();
		var eq0 = CFileDescriptor.of(fd.fd());
		var ne0 = CFileDescriptor.of(fd.fd() + 1);
		Testing.exerciseEquals(fd, eq0);
		Assert.notEqualAll(fd, ne0);
	}

	@Test
	public void shouldFailToWrapInvalidDescriptor() {
		Assert.thrown(() -> CFileDescriptor.of(-1));
	}

	@Test
	public void shouldFailToProvideDescriptorIfClosed() throws IOException {
		init();
		fd.fd();
		fd.close();
		Assert.thrown(() -> fd.fd());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideInputStream() throws IOException {
		var lib = init();
		lib.read.autoResponses(ByteProvider.of(33));
		fd.in().bufferSize(3);
		Assert.equal(fd.in().read(), 33);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideOutputStream() throws IOException {
		var lib = init();
		lib.write.autoResponses(3);
		fd.out().bufferSize(3);
		fd.out().write(new byte[] { 1, 2, 3 });
	}

	@Test
	public void shouldAcceptConsumer() throws IOException {
		init();
		fd.accept(f -> Assert.equal(f, fd.fd()));
	}

	@Test
	public void shouldApply() throws IOException {
		init();
		Assert.equal(fd.apply(f -> {
			Assert.equal(f, fd.fd());
			return 33;
		}), 33);
	}

	@Test
	public void shouldNotThrowExceptionOnClose() throws IOException {
		var lib = init();
		lib.close.error.setFrom(ErrNo.EIO::lastError);
		LogModifier.run(fd::close, Level.OFF, Logs.class);
		lib.close.error.clear();
	}

	@Test
	public void shouldCloseSilently() throws IOException {
		var lib = init();
		lib.close.error.setFrom(ErrNo.EIO::lastError, null, null, ErrNo.EIO::error);
		Assert.equal(fd.closeSilently(), false);
		Assert.equal(fd.closeSilently(), true);
		Assert.equal(fd.closeSilently(), true);
		Assert.equal(fd.closeSilently(), true); // already closed
	}

	private TestCLibNative init() throws IOException {
		ref.init();
		fd = CFileDescriptor.open("test");
		return ref.get();
	}
}
