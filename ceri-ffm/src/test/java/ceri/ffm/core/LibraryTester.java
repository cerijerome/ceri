package ceri.ffm.core;

import java.io.IOException;
import java.util.Arrays;
import ceri.common.test.BinaryPrinter;
import ceri.common.test.FileTestHelper;
import ceri.ffm.clib.ffm.CException;
import ceri.ffm.clib.ffm.CFcntl;
import ceri.ffm.clib.ffm.CLib;
import ceri.ffm.clib.ffm.CStdLib;
import ceri.ffm.clib.ffm.CUnistd;

public class LibraryTester {

	private static final String FILE = "file.txt";

	public static void main(String[] args) throws Exception {
		runMisc();
		runOpenVarArg();
		runPipe();
		runEnv();
		printMethods(CLib.library);
	}

	public static void printMethods(Library<?> lib) {
		lib.methods().forEach((k, c) -> {
			System.out.println();
			System.out.println(k);
			System.out.println(c);
		});
	}

	public static void runMisc() throws CException {
		System.out.println("pagesize = " + CUnistd.getpagesize());
	}

	public static void runOpenVarArg() throws IOException {
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

	public static void runPipe() throws CException {
		var pipeFds = CUnistd.pipe();
		System.out.println("pipe() = " + Arrays.toString(pipeFds));
		CUnistd.closeSilently(pipeFds);
	}

	public static void runEnv() throws CException {
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

}
