package ceri.jna.clib.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertByte;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseRecord;
import static ceri.jna.clib.jna.CFcntl.O_RDWR;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.data.ByteProvider;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.clib.test.TestCLibNative.CfArgs;
import ceri.jna.clib.test.TestCLibNative.LseekArgs;
import ceri.jna.clib.test.TestCLibNative.OpenArgs;
import ceri.jna.clib.test.TestCLibNative.PollArgs;
import ceri.jna.clib.test.TestCLibNative.ReadArgs;
import ceri.jna.clib.test.TestCLibNative.SignalArgs;
import ceri.jna.clib.test.TestCLibNative.TcArgs;
import ceri.jna.clib.test.TestCLibNative.WriteArgs;
import ceri.jna.util.GcMemory;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.Struct;

public class TestCLibNativeBehavior {
	private TestCLibNative lib;
	private Enclosed<RuntimeException, ?> enc;
	private int fd;

	@Before
	public void before() {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
		fd = lib.open("test", O_RDWR, 0666);
	}

	@After
	public void after() {
		lib.close(fd);
		enc.close();
	}

	@Test
	public void shouldProvideArgumentTypes() {
		exerciseRecord(new OpenArgs("test", 111, 222));
		exerciseRecord(new ReadArgs(111, 222));
		exerciseRecord(new WriteArgs(111, ByteProvider.of(1, 2, 3)));
		exerciseRecord(new LseekArgs(111, 222, 333));
		exerciseRecord(new SignalArgs(111, new Pointer(1)));
		assertAssertion(() -> new SignalArgs(111, null));
		exerciseRecord(new PollArgs(List.of(), 0));
		exerciseRecord(new TcArgs("test", 0, List.of()));
		exerciseRecord(new CfArgs("test", null, List.of()));
	}

	@Test
	public void shouldProvideAutoErrorLogic() throws CException {
		TestCLibNative.autoError(lib.fcntl, 333, args -> args.request() < 0, "Test");
		assertEquals(CFcntl.fcntl(fd, -1), 333);
		assertThrown(() -> CFcntl.fcntl(fd, 1));
		lib.fcntl.error.clear();
	}

	@Test
	public void shouldCaptureOpenParams() {
		assertEquals(lib.fds.get(fd), new OpenArgs("test", O_RDWR, 0666));
	}

	@Test
	public void shouldOpenPipe() throws CException {
		var pipefd = CUnistd.pipe();
		lib.pipe.assertValues(new int[] { lib.fd(pipefd[0]), lib.fd(pipefd[1]) });
	}

	@Test
	public void shouldReadIntoMemory() throws IOException {
		lib.read.autoResponses(ByteProvider.of(1, 2, 3), null, ByteProvider.empty());
		assertArray(CUnistd.readBytes(fd, 5), 1, 2, 3);
		lib.read.assertAuto(new ReadArgs(fd, 5));
		assertArray(CUnistd.readBytes(fd, 3));
		lib.read.assertAuto(new ReadArgs(fd, 3));
		assertArray(CUnistd.readBytes(fd, 2));
		lib.read.assertAuto(new ReadArgs(fd, 2));
	}

	@Test
	public void shouldWriteFromMemory() throws IOException {
		lib.write.autoResponses(2, 1);
		assertEquals(CUnistd.write(fd, GcMemory.mallocBytes(1, 2, 3).m, 3), 2);
		lib.write.assertAuto(WriteArgs.of(fd, 1, 2, 3));
		assertEquals(CUnistd.write(fd, (Pointer) null, 2), 1);
		lib.write.assertAuto(WriteArgs.of(fd, 0, 0));
	}

	@Test
	public void shouldFailForInvalidFd() {
		assertThrown(() -> CUnistd.write(-1, 1, 2, 3));
	}

	@Test
	public void shouldProvideLastFd() {
		int fd = lib.open("test", 0, 0);
		assertEquals(lib.lastFd(), fd);
	}

	@Test
	public void shouldAccessLinuxTermiosFromCfCalls() {
		var termios = new CTermios.Linux.termios();
		termios.c_cc[0] = (byte) 0xff;
		Struct.write(termios);
		lib.cfmakeraw(termios.getPointer());
		assertByte(lib.cf.awaitAuto().termiosLinux().c_cc[0], 0xff);
	}

	@Test
	public void shouldAccessMacTermiosFromCfCalls() {
		var termios = new CTermios.Mac.termios();
		termios.c_cc[0] = (byte) 0xff;
		Struct.write(termios);
		lib.cfmakeraw(termios.getPointer());
		assertByte(lib.cf.awaitAuto().termiosMac().c_cc[0], 0xff);
	}

	@Test
	public void shouldAccessCfArgs() {
		lib.cfsetispeed(null, JnaUtil.unlong(250000));
		assertEquals(lib.cf.awaitAuto().arg(0), 250000);
	}

}
