package ceri.jna.clib.test;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.jna.test.JnaTestUtil.LINUX_OS;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.SystemIoCaptor;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Enclosed;
import ceri.jna.clib.ErrNo;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.test.JnaTestUtil;

public class CLibVerifierBehavior {
	private TestCLibNative lib;
	private Enclosed<RuntimeException, ?> enc;

	@After
	public void after() {
		CloseableUtil.close(enc);
		enc = null;
		lib = null;
	}

	@Test
	public void shouldVerifyAll() throws IOException {
		try (var sys = SystemIoCaptor.of()) {
			CLibVerifier.main(new String[] {});
			assertFind(sys.out, "skipping CTermios");
		}
	}

	@Test
	public void shouldVerifyFile() throws IOException {
		CLibVerifier.verifyFile();
	}

	@Test
	public void shouldVerifySignal() throws IOException {
		CLibVerifier.verifySignal();
	}

	@Test
	public void shouldVerifyPoll() throws IOException {
		CLibVerifier.verifyPoll();
	}

	@Test
	public void shouldVerifyLinuxPpoll() throws IOException {
		JnaTestUtil.testAsOs(LINUX_OS, () -> {
			initLib();
			lib.poll.autoResponses(2);
			CLibVerifier.verifyPoll();
		});
	}

	@Test
	public void shouldVerifyTermios() throws IOException {
		CLibVerifier.verifyTermios(null); // returns false if no serial devices
	}

	@Test
	public void shouldVerifySerial() throws IOException {
		initLib();
		lib.cf.autoResponses(0, 0, 0, // cfmakeraw, cfsetispeed, cfsetospeed
			CTermios.B9600, CTermios.B9600, 0); // cfgetispeed, cfgetospeed, ...
		CLibVerifier.verifyTermios("serial");
	}

	@Test
	public void shouldFailToVerifyBadSerial() throws IOException {
		initLib();
		lib.open.error.setFrom(ErrNo.ENOENT::lastError);
		assertFalse(CLibVerifier.verifyTermios("serial"));
	}

	@Test
	public void shouldVerifyEnv() throws IOException {
		CLibVerifier.verifyEnv();
	}

	private void initLib() {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
	}
}
