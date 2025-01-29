package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.common.test.TestUtil.exerciseRecord;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.util.Enclosed;
import ceri.jna.clib.CFileDescriptor.Opener;
import ceri.jna.clib.FileDescriptor.Open;
import ceri.jna.clib.Mode.Mask;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.OpenArgs;
import ceri.log.test.LogModifier;
import ceri.log.util.LogUtil;

public class CFileDescriptorBehavior {
	private static TestCLibNative lib;
	private static Enclosed<RuntimeException, ?> enc;
	private CFileDescriptor fd;

	@BeforeClass
	public static void beforeClass() {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
	}

	@AfterClass
	public static void afterClass() {
		enc.close();
	}

	@Before
	public void before() throws IOException {
		lib.reset();
		fd = CFileDescriptor.open("test");
	}

	@After
	public void after() {
		fd.closeSilently();
	}

	@Test
	public void testIsBroken() {
		assertFalse(CFileDescriptor.isBroken(null));
		assertFalse(CFileDescriptor.isBroken(new IOException("remote i/o")));
		assertFalse(CFileDescriptor.isBroken(ErrNo.UNDEFINED.error("test")));
		assertFalse(CFileDescriptor.isBroken(ErrNo.EPERM.error("test")));
		assertTrue(CFileDescriptor.isBroken(ErrNo.ENOENT.error("test")));
		assertTrue(CFileDescriptor.isBroken(ErrNo.EPERM.error("remote i/o")));
		assertTrue(CFileDescriptor.isBroken(ErrNo.UNDEFINED.error("remote i/o")));
	}

	@Test
	public void shouldOpenWithMode() throws IOException {
		try (var _ = CFileDescriptor.open("test", Mode.of(Mask.rwxo), Open.APPEND)) {
			lib.open.assertCall(new OpenArgs("test", Open.APPEND.value, Mask.rwxo.value));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldOpenWithOpener() throws IOException {
		new Opener("test", Mode.of(0567), Open.CREAT).get();
		lib.open.assertCall(new OpenArgs("test", Open.CREAT.value, 0567));
		new Opener("test", Mode.of(0756), List.of(Open.APPEND)).get();
		lib.open.assertCall(new OpenArgs("test", Open.APPEND.value, 0756));
	}

	@Test
	public void shouldNotBreachOpenerEqualsContract() {
		Opener t = new Opener("test", Mode.of(0767), Open.RDWR);
		Opener eq0 = new Opener("test", Mode.of(0767), List.of(Open.RDWR));
		Opener ne0 = new Opener("Test", Mode.of(0767), Open.RDWR);
		Opener ne1 = new Opener("test", Mode.of(0777), Open.RDWR);
		Opener ne2 = new Opener("test", Mode.of(0767), Open.RDONLY);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
		exerciseRecord(t);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBreachEqualsContract() {
		CFileDescriptor eq0 = CFileDescriptor.of(fd.fd());
		CFileDescriptor ne0 = CFileDescriptor.of(fd.fd() + 1);
		exerciseEquals(fd, eq0);
		assertAllNotEqual(fd, ne0);
	}

	@Test
	public void shouldFailToWrapInvalidDescriptor() {
		assertThrown(() -> CFileDescriptor.of(-1));
	}

	@Test
	public void shouldFailToProvideDescriptorIfClosed() {
		fd.fd();
		fd.close();
		assertThrown(() -> fd.fd());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideInputStream() throws IOException {
		lib.read.autoResponses(ByteProvider.of(33));
		fd.in().bufferSize(3);
		assertEquals(fd.in().read(), 33);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideOutputStream() throws IOException {
		lib.write.autoResponses(3);
		fd.out().bufferSize(3);
		fd.out().write(new byte[] { 1, 2, 3 });
	}

	@Test
	public void shouldAcceptConsumer() throws IOException {
		fd.accept(f -> assertEquals(f, fd.fd()));
	}

	@Test
	public void shouldApply() throws IOException {
		assertEquals(fd.apply(f -> {
			assertEquals(f, fd.fd());
			return 33;
		}), 33);
	}

	@Test
	public void shouldNotThrowExceptionOnClose() {
		lib.close.error.setFrom(() -> ErrNo.EIO.error());
		LogModifier.run(fd::close, Level.OFF, LogUtil.class);
		lib.close.error.clear();
	}

}
