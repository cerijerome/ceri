package ceri.ffm.core;

import java.io.IOException;
import ceri.ffm.clib.ffm.CStdLib;

public class CallsTester {

	public static void main(String[] args) throws IOException {
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
