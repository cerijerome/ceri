package ceri.jna.clib.jna;

import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.jna.clib.ErrNo;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.CtlArgs;
import ceri.jna.test.JnaAssert;
import ceri.jna.test.JnaTesting;
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
		Assert.privateConstructor(CUtil.class);
	}

	@Test
	public void testIoctlRead() throws CException {
		ref.init();
		int fd = CFcntl.open("test", 0);
		var s = new S();
		ref.lib().ioctl.autoResponse(a -> handleIoc(a, 100, 0));
		Assert.equal(CUtil.ioctlRead(fd, Ioc.A, s, CErrNo.EAGAIN).a, 100);
		ref.lib().ioctl.autoResponse(a -> handleIoc(a, 100, CErrNo.EAGAIN));
		Assert.equal(CUtil.ioctlRead(fd, Ioc.A, s, CErrNo.EAGAIN), null);
		ref.lib().ioctl.autoResponse(a -> handleIoc(a, 100, CErrNo.EPERM));
		JnaAssert.cexception(CErrNo.EPERM, () -> CUtil.ioctlRead(fd, Ioc.A, s, CErrNo.EAGAIN));
	}

	@Test
	public void testOptionalGet() throws CException {
		Assert.optional(CUtil.optionalGet(() -> "test", CErrNo.EAGAIN), "test");
		Assert.optional(
			CUtil.optionalGet(() -> Assert.throwIt(ErrNo.EAGAIN.error()), CErrNo.EAGAIN), null);
		JnaAssert.cexception(CErrNo.EPERM,
			() -> CUtil.optionalGet(() -> Assert.throwIt(ErrNo.EPERM.error()), CErrNo.EAGAIN));
	}

	@Test
	public void testRun() throws CException {
		Assert.equal(CUtil.run(() -> {}, CErrNo.EAGAIN), true);
		Assert.equal(CUtil.run(() -> Assert.throwIt(ErrNo.EAGAIN.error()), CErrNo.EAGAIN), false);
		JnaAssert.cexception(CErrNo.EPERM,
			() -> CUtil.run(() -> Assert.throwIt(ErrNo.EPERM.error()), CErrNo.EAGAIN));
	}

	@Test
	public void testRequireContiguous() throws IOException {
		CUtil.requireContiguous(new CPoll.pollfd[0]);
		CUtil.requireContiguous(CPoll.pollfd.array(0));
		CUtil.requireContiguous(CPoll.pollfd.array(2));
		JnaAssert.cexception(() -> CUtil
			.requireContiguous(new CPoll.pollfd[] { new CPoll.pollfd(), new CPoll.pollfd() }));
	}

	@Test
	public void testTty() {
		var lib = ref.init();
		lib.isatty.autoResponses(0, 1);
		Assert.equal(CUtil.tty(), false);
		Assert.equal(CUtil.tty(), true);
		lib.isatty.error.setFrom(ErrNo.EBADFD::lastError);
		Assert.equal(CUtil.tty(), false);
	}

	private static int handleIoc(CtlArgs args, int a, int errNo) {
		if (errNo != 0) throw JnaTesting.lastError(errNo, "test");
		JnaTesting.handleStructRef(args.arg(0), new S(), s -> s.a = a);
		return 0;
	}
}
