package ceri.ffm.core;

import java.io.IOException;
import ceri.ffm.clib.ffm.CStdLib;
import ceri.ffm.type.Callback;

public class CallTester {

	public interface Cb extends Callback {
		long invoke(int i, long l);
	}

	public static void main(String[] args) throws Throwable {
		try (var cb = Callback.noOpCallback(Cb.class)) {
			System.out.println(cb.invoke(1, 2));
			cb.close();
			System.gc();
			System.out.println(cb.invoke(2, 3));
			System.gc();
			System.out.println(cb.invoke(2, 3));
			System.gc();
			System.out.println(cb.invoke(2, 3));
			System.gc();
			System.out.println(cb.invoke(2, 3));
		}
	}

	public static void downcalls() throws IOException {
		System.out.println(CStdLib.getenv("USER"));
		System.out.println(CStdLib.getenv("TESTXXX"));
		CStdLib.setenv("TESTXXX", "xxx", false);
		System.out.println(CStdLib.getenv("TESTXXX"));
		CStdLib.setenv("TESTXXX", "yyy", false);
		System.out.println(CStdLib.getenv("TESTXXX"));
		CStdLib.setenv("TESTXXX", "yyy", true);
		System.out.println(CStdLib.getenv("TESTXXX"));
		System.out.println("Expecting: <user>, null, xxx, xxx, yyy");
	}

}
