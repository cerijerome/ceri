package ceri.jna.clib.test;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.data.ByteProvider;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.jna.CTermios.speed_t;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.clib.test.TestCLibNative.CfArgs;
import ceri.jna.clib.test.TestCLibNative.LseekArgs;
import ceri.jna.clib.test.TestCLibNative.OpenArgs;
import ceri.jna.clib.test.TestCLibNative.PollArgs;
import ceri.jna.clib.test.TestCLibNative.ReadArgs;
import ceri.jna.clib.test.TestCLibNative.SignalArgs;
import ceri.jna.clib.test.TestCLibNative.TcArgs;
import ceri.jna.clib.test.TestCLibNative.WriteArgs;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.type.Struct;
import ceri.jna.util.JnaLibrary;

public class TestCLibNativeBehavior {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private int fd = -1;

	@After
	public void after() {
		if (fd >= 0) ref.lib().close(fd);
		Closeables.close(ref);
		fd = -1;
	}

	@Test
	public void shouldProvideArgumentTypes() {
		Testing.exerciseRecord(new OpenArgs("test", 111, 222));
		Testing.exerciseRecord(new ReadArgs(111, 222));
		Testing.exerciseRecord(new WriteArgs(111, ByteProvider.of(1, 2, 3)));
		Testing.exerciseRecord(new LseekArgs(111, 222, 333));
		Testing.exerciseRecord(new SignalArgs(111, new Pointer(1)));
		Assert.assertion(() -> new SignalArgs(111, null));
		Testing.exerciseRecord(new PollArgs(List.of(), Duration.ofMillis(0), Set.of()));
		Testing.exerciseRecord(new TcArgs("test", 0, List.of()));
		Testing.exerciseRecord(new CfArgs("test", null, List.of()));
	}

	@Test
	public void shouldProvideAutoErrorLogic() throws CException {
		var lib = initFd();
		TestCLibNative.autoError(lib.fcntl, 333, args -> args.request() < 0, "Test");
		Assert.equal(CFcntl.fcntl(fd, -1), 333);
		Assert.thrown(() -> CFcntl.fcntl(fd, 1));
		lib.fcntl.error.clear();
	}

	@Test
	public void shouldCaptureOpenParams() {
		var lib = initFd();
		Assert.equal(lib.fdContext.get(fd).args(), new OpenArgs("test", CFcntl.O_RDWR, 0666));
	}

	@Test
	public void shouldOpenPipe() throws CException {
		var lib = ref.init();
		var pipefd = CUnistd.pipe();
		lib.pipe.assertValues(new int[] { lib.fd(pipefd[0]), lib.fd(pipefd[1]) });
	}

	@Test
	public void shouldPpollWithNoFds() {
		var lib = ref.init();
		lib.ppoll(null, 0, null, null);
	}

	@Test
	public void shouldIgnoreFailedSigSet() {
		var lib = ref.init();
		lib.sigset.autoResponses(1);
		lib.sigemptyset(null);
	}

	@Test
	public void shouldReadIntoMemory() throws IOException {
		var lib = initFd();
		lib.read.autoResponses(ByteProvider.of(1, 2, 3), null, ByteProvider.empty());
		Assert.array(CUnistd.readBytes(fd, 5), 1, 2, 3);
		lib.read.assertAuto(new ReadArgs(fd, 5));
		Assert.array(CUnistd.readBytes(fd, 3));
		lib.read.assertAuto(new ReadArgs(fd, 3));
		Assert.array(CUnistd.readBytes(fd, 2));
		lib.read.assertAuto(new ReadArgs(fd, 2));
	}

	@Test
	public void shouldWriteFromMemory() throws IOException {
		var lib = initFd();
		lib.write.autoResponses(2, 1);
		Assert.equal(CUnistd.write(fd, JnaTestUtil.mem(1, 2, 3).m, 3), 2);
		lib.write.assertAuto(WriteArgs.of(fd, 1, 2, 3));
		Assert.equal(CUnistd.write(fd, (Pointer) null, 2), 1);
		lib.write.assertAuto(WriteArgs.of(fd, 0, 0));
	}

	@Test
	public void shouldFailForInvalidFd() {
		Assert.thrown(() -> CUnistd.write(-1, 1, 2, 3));
	}

	@Test
	public void shouldProvideLastFd() {
		var lib = ref.init();
		int fd = lib.open("test", 0, 0);
		Assert.equal(lib.lastFd(), fd);
	}

	@Test
	public void shouldAccessLinuxTermiosFromCfCalls() {
		var lib = ref.init();
		var termios = new CTermios.Linux.termios();
		termios.c_cc[0] = (byte) 0xff;
		Struct.write(termios);
		lib.cfmakeraw(termios.getPointer());
		Assert.equals(lib.cf.awaitAuto().termiosLinux().c_cc[0], 0xff);
	}

	@Test
	public void shouldAccessMacTermiosFromCfCalls() {
		var lib = ref.init();
		var termios = new CTermios.Mac.termios();
		termios.c_cc[0] = (byte) 0xff;
		Struct.write(termios);
		lib.cfmakeraw(termios.getPointer());
		Assert.equals(lib.cf.awaitAuto().termiosMac().c_cc[0], 0xff);
	}

	@Test
	public void shouldAccessCfArgs() {
		var lib = ref.init();
		lib.cfsetispeed(null, new speed_t(250000));
		Assert.equal(lib.cf.awaitAuto().arg(0), 250000);
	}

	private TestCLibNative initFd() {
		var lib = ref.init();
		fd = lib.open("test", CFcntl.O_RDWR, 0666);
		return lib;
	}
}
