package ceri.ffm.clib.ffm;

import java.io.IOException;
import java.util.Arrays;
import ceri.common.test.BinaryPrinter;
import ceri.common.test.FileTestHelper;
import ceri.ffm.clib.ffm.CSignal.sighandler_t;
import ceri.ffm.clib.ffm.CSignal.sigset_t;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.type.Pointer;

public class CLibTester {
	private static final String FILE = "file.txt";

	public static void main(String[] args) throws Exception {
		runCUnistd();
		runCStdLib();
		runCSignal();
		FfmTesting.title("Methods");
		CLib.library.methods().values().forEach(System.out::println);
	}

	private static void runCUnistd() throws Exception {
		FfmTesting.title("CUnistd");
		runMisc();
		runOpenVarArg();
		runPipe();
	}

	private static void runCStdLib() throws CException {
		FfmTesting.title("CStdLib");
		runEnv();
	}

	private static void runCSignal() throws CException {
		FfmTesting.title("CSignal");
		runSignal();
		runSigSet();
	}

	private static void runMisc() throws CException {
		System.out.println("pagesize = " + CUnistd.getpagesize());
	}

	private static void runOpenVarArg() throws IOException {
		try (var files = FileTestHelper.builder().file(FILE, "abc/nde/nf").build()) {
			var file = files.path(FILE).toString();
			System.out.println("file = " + file);
			int fd1 = CFcntl.open(file, CFcntl.Open.O_RDONLY.value);
			System.out.println("fd1 = " + fd1);
			int fd2 = CFcntl.open(file, CFcntl.Open.O_RDONLY.value, 0777);
			System.out.println("fd2 = " + fd2);
			System.out.println("fd2 tty = " + CUnistd.isatty(fd2));
			BinaryPrinter.STD.print(CUnistd.readAllBytes(fd1, 100));
			CUnistd.position(fd2, 3);
			BinaryPrinter.STD.print(CUnistd.readAllBytes(fd2, 100));
			CUnistd.close(fd2);
			CUnistd.close(fd1);
		}
	}

	private static void runPipe() throws CException {
		var pipeFds = CUnistd.pipe();
		System.out.println("pipe() = " + Arrays.toString(pipeFds));
		CUnistd.closeSilently(pipeFds);
	}

	private static void runEnv() throws CException {
		var key = "CERI_TEST";
		System.out.printf("\"%s\" = %s%n", key, CStdLib.getenv(key));
		CStdLib.setenv(key, "hello1", false);
		System.out.printf("\"%s\" = %s%n", key, CStdLib.getenv(key));
		CStdLib.setenv(key, "hello2", true);
		System.out.printf("\"%s\" = %s%n", key, CStdLib.getenv(key));
		CStdLib.setenv(key, "hello3", false);
		System.out.printf("\"%s\" = %s%n", key, CStdLib.getenv(key));
		key = "";
		System.out.printf("\"%s\" = %s%n", key, CStdLib.getenv(key));
		try {
			CStdLib.setenv(key, "hello4", true);
		} catch (Exception e) {
			System.out.println("Expected: " + e.getMessage());
		}
	}

	private static void runSignal() throws CException {
		int signum = CSignal.SIGUSR1;
		try (sighandler_t sh1 = i -> System.out.println("sh1=" + i);
			sighandler_t sh2 = i -> System.out.println("sh2=" + i)) {
			var previous = CSignal.signal(signum, sh1);
			CSignal.raise(signum);
			previous = CSignal.signal(signum, sh2);
			previous.invoke(signum);
			CSignal.raise(signum);
			previous = CSignal.signal(signum, 0);
			previous.invoke(signum);
			previous = CSignal.signal(signum, 1);
			previous.invoke(signum);
			CSignal.raise(signum);
			previous = CSignal.signal(signum, sh1);
			previous.invoke(signum);
			CSignal.raise(signum);
		}
	}

	private static void runSigSet() throws CException {
		var set = new sigset_t();
		var m = sigset_t.$.alloc(set);
		var p = Pointer.of(m, sigset_t.$);
		CSignal.sigemptyset(p);
	}
}
