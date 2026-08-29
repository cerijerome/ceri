package ceri.ffm.clib.test;

import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.SystemIoCaptor;
import ceri.ffm.clib.ffm.CErrNo;
import ceri.ffm.clib.ffm.CTermios;
import ceri.ffm.clib.test.TestCLibNative.Result;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.util.FfmOs;

public class CLibVerifierBehavior {
	private final FfmTesting.Lib<? extends TestCLibNative> ref = TestCLibNative.lib();

	@After
	public void after() {
		Closeables.close(ref);
	}

	@Test
	public void shouldVerifyAll() throws IOException {
		try (var sys = SystemIoCaptor.of()) {
			CLibVerifier.main(new String[] {});
			Assert.find(sys.out, "skipping CTermios");
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
		FfmOs.linux.accept(_ -> {
			var lib = ref.init();
			lib.poll.autoResponse(p -> {
				for (var pollFd : p.pollFds())
					pollFd.revents = pollFd.events;
			}, null);
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
		// cfmakeraw, cfsetispeed, cfsetospeed, cfgetispeed, cfgetospeed, ...
		lib.cf.autoResponses(Result.of(0), Result.of(0), Result.of(0), Result.of(CTermios.B9600),
			Result.of(CTermios.B9600), Result.of(0));
		CLibVerifier.verifyTermios("serial");
	}

	@Test
	public void shouldFailToVerifyBadSerial() throws IOException {
		var lib = ref.init();
		lib.open.error.setFrom(CErrNo.ENOENT::error);
		Assert.no(CLibVerifier.verifyTermios("serial"));
	}

	@Test
	public void shouldVerifyEnv() throws IOException {
		CLibVerifier.verifyEnv();
	}
}
