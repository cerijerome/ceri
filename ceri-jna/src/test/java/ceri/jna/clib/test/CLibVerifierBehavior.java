package ceri.jna.clib.test;

import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertFind;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.SystemIoCaptor;
import ceri.jna.clib.ErrNo;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.util.JnaLibrary;
import ceri.jna.util.JnaOs;

public class CLibVerifierBehavior {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();

	@After
	public void after() {
		Closeables.close(ref);
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
		JnaOs.linux.accept(_ -> {
			var lib = ref.init();
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
		var lib = ref.init();
		lib.cf.autoResponses(0, 0, 0, // cfmakeraw, cfsetispeed, cfsetospeed
			CTermios.B9600, CTermios.B9600, 0); // cfgetispeed, cfgetospeed, ...
		CLibVerifier.verifyTermios("serial");
	}

	@Test
	public void shouldFailToVerifyBadSerial() throws IOException {
		var lib = ref.init();
		lib.open.error.setFrom(ErrNo.ENOENT::lastError);
		assertFalse(CLibVerifier.verifyTermios("serial"));
	}

	@Test
	public void shouldVerifyEnv() throws IOException {
		CLibVerifier.verifyEnv();
	}
}
