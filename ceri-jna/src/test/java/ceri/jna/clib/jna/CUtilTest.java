package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOptional;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.throwIt;
import static ceri.jna.test.JnaTestUtil.assertCException;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.jna.clib.ErrNo;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.CtlArgs;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.type.Struct;
import ceri.jna.type.Struct.Fields;
import ceri.jna.util.JnaLibrary;

public class CUtilTest {
	private final JnaLibrary.Ref<TestCLibNative> ref = TestCLibNative.ref();

	enum Ioc implements CUtil.Ioctl {
		A(111),
		B(222);

		public final int value;

		private Ioc(int value) {
			this.value = value;
		}

		@Override
		public int ioctl(int fd, Object... objs) throws CException {
			return CIoctl.ioctl(name(), fd, value, objs);
		}
	}

	@Fields("a")
	public static class S extends Struct {
		public int a;
	}

	@After
	public void after() {
		Closeables.close(ref);
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CUtil.class);
	}

	@Test
	public void testIoctlRead() throws CException {
		ref.init();
		int fd = CFcntl.open("test", 0);
		var s = new S();
		ref.lib().ioctl.autoResponse(a -> handleIoc(a, 100, 0));
		assertEquals(CUtil.ioctlRead(fd, Ioc.A, s, CErrNo.EAGAIN).a, 100);
		ref.lib().ioctl.autoResponse(a -> handleIoc(a, 100, CErrNo.EAGAIN));
		assertEquals(CUtil.ioctlRead(fd, Ioc.A, s, CErrNo.EAGAIN), null);
		ref.lib().ioctl.autoResponse(a -> handleIoc(a, 100, CErrNo.EPERM));
		assertCException(CErrNo.EPERM, () -> CUtil.ioctlRead(fd, Ioc.A, s, CErrNo.EAGAIN));
	}

	@Test
	public void testOptionalGet() throws CException {
		assertOptional(CUtil.optionalGet(() -> "test", CErrNo.EAGAIN), "test");
		assertOptional(CUtil.optionalGet(() -> throwIt(ErrNo.EAGAIN.error()), CErrNo.EAGAIN), null);
		assertCException(CErrNo.EPERM,
			() -> CUtil.optionalGet(() -> throwIt(ErrNo.EPERM.error()), CErrNo.EAGAIN));
	}

	@Test
	public void testRun() throws CException {
		assertEquals(CUtil.run(() -> {}, CErrNo.EAGAIN), true);
		assertEquals(CUtil.run(() -> throwIt(ErrNo.EAGAIN.error()), CErrNo.EAGAIN), false);
		assertCException(CErrNo.EPERM,
			() -> CUtil.run(() -> throwIt(ErrNo.EPERM.error()), CErrNo.EAGAIN));
	}

	@Test
	public void testRequireContiguous() throws IOException {
		CUtil.requireContiguous(new CPoll.pollfd[0]);
		CUtil.requireContiguous(CPoll.pollfd.array(0));
		CUtil.requireContiguous(CPoll.pollfd.array(2));
		assertCException(() -> CUtil
			.requireContiguous(new CPoll.pollfd[] { new CPoll.pollfd(), new CPoll.pollfd() }));
	}

	@Test
	public void testTty() {
		var lib = ref.init();
		lib.isatty.autoResponses(0, 1);
		assertEquals(CUtil.tty(), false);
		assertEquals(CUtil.tty(), true);
		lib.isatty.error.setFrom(ErrNo.EBADFD::lastError);
		assertEquals(CUtil.tty(), false);
	}

	private static int handleIoc(CtlArgs args, int a, int errNo) {
		if (errNo != 0) throw JnaTestUtil.lastError(errNo, "test");
		JnaTestUtil.handleStructRef(args.arg(0), new S(), s -> s.a = a);
		return 0;
	}
}
