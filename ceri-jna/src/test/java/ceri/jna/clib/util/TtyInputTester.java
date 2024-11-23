package ceri.jna.clib.util;

import java.io.IOException;

public class TtyInputTester {

	public static void main(String[] args) throws IOException {
		try (var tty = TtyInput.in()) {
			while (true) {
				System.out.print("Enter> ");
				var line = tty.ref.readLine();
				if ("x".equals(line)) break;
				System.out.println(line);
			}
		}
	}

}
