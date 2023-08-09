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
import ceri.jna.clib.Mode.Mask;
import ceri.jna.clib.jna.CError;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.test.TestCLibNative;
import ceri.log.test.LogModifier;

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
		assertFalse(CFileDescriptor.isBroken(CException.of(-1, "test")));
		assertFalse(CFileDescriptor.isBroken(CException.of(1, "test")));
		assertTrue(CFileDescriptor.isBroken(CException.of(CError.ENOENT.code, "test")));
		assertTrue(CFileDescriptor.isBroken(CException.of(1, "remote i/o")));
		assertTrue(CFileDescriptor.isBroken(CException.of(-1, "remote i/o")));
	}

	@Test
	public void shouldOpenWithMode() throws IOException {
		try (var fd = CFileDescriptor.open("test", Mode.of(Mask.rwxo), OpenFlag.O_APPEND)) {
			lib.open.assertCall(List.of("test", OpenFlag.O_APPEND.value, Mask.rwxo.value));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldOpenWithOpener() throws IOException {
		new Opener("test", Mode.of(0567), OpenFlag.O_CREAT).get();
		lib.open.assertCall(List.of("test", OpenFlag.O_CREAT.value, 0567));
		new Opener("test", Mode.of(0756), List.of(OpenFlag.O_APPEND)).get();
		lib.open.assertCall(List.of("test", OpenFlag.O_APPEND.value, 0756));
	}

	@Test
	public void shouldNotBreachOpenerEqualsContract() {
		Opener t = new Opener("test", Mode.of(0767), OpenFlag.O_RDWR);
		Opener eq0 = new Opener("test", Mode.of(0767), List.of(OpenFlag.O_RDWR));
		Opener ne0 = new Opener("Test", Mode.of(0767), OpenFlag.O_RDWR);
		Opener ne1 = new Opener("test", Mode.of(0777), OpenFlag.O_RDWR);
		Opener ne2 = new Opener("test", Mode.of(0767), OpenFlag.O_RDONLY);
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
		lib.close.error.setFrom(() -> CError.EIO.error());
		LogModifier.run(fd::close, Level.OFF, CFileDescriptor.class);
		lib.close.error.clear();
	}

}
