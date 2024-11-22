package ceri.jna.clib.util;

import static ceri.jna.clib.jna.CTermios.ECHO;
import static ceri.jna.clib.jna.CTermios.ECHONL;
import static ceri.jna.clib.jna.CTermios.ICANON;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import ceri.common.function.RuntimeCloseable;
import ceri.common.io.ConsoleInput;
import ceri.common.io.LineReader;
import ceri.common.text.StringUtil;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.Termios;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.util.JnaUtil;
import ceri.log.util.LogUtil;

public class TtyInput implements LineReader, RuntimeCloseable {
	private final FileDescriptor fd;
	private final Reader reader;
	private final ConsoleInput consoleInput;
	private final Termios termios;

	public static void main(String[] args) throws IOException {
		try (var tty = TtyInput.of()) {
			while (true) {
				System.out.print("Enter: ");
				var line = StringUtil.trim(tty.readLine());
				System.out.println("\nLine = " + line);
				if ("x".equals(line)) break;
			}
		}
	}

	public static TtyInput of() throws IOException {
		return of(ConsoleInput.Config.BLOCK);
	}

	public static TtyInput of(ConsoleInput.Config config, String... history) throws IOException {
		return new TtyInput(config, Arrays.asList(history));
	}

	private TtyInput(ConsoleInput.Config config, Iterable<String> history) throws IOException {
		fd = CFileDescriptor.of(CUnistd.STDIN_FILENO);
		termios = Termios.get(fd);
		configureTty(fd);
		reader = new InputStreamReader(System.in);
		consoleInput = ConsoleInput.of(reader, System.out, config, history);
	}

	@Override
	public String readLine() throws IOException {
		return consoleInput.readLine();
	}

	@Override
	public void close() {
		LogUtil.close(termios::set, fd);
	}

	private void configureTty(FileDescriptor fd) throws IOException {
		var termios = Termios.get(fd);
		JnaUtil.and(termios.localFlags(), ~(ICANON | ECHO | ECHONL));
		termios.set();
	}
}
