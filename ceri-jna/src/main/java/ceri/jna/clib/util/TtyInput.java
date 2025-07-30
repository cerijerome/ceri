package ceri.jna.clib.util;

import static ceri.jna.clib.jna.CTermios.ECHO;
import static ceri.jna.clib.jna.CTermios.ECHONL;
import static ceri.jna.clib.jna.CTermios.ICANON;
import static ceri.jna.clib.jna.CUnistd.STDIN_FILENO;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import ceri.common.function.Functions;
import ceri.common.io.ConsoleInput;
import ceri.common.io.LineReader;
import ceri.common.util.Enclosure;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.Termios;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.util.JnaUtil;
import ceri.log.util.LogUtil;

/**
 * Modifies tty to provide char-based input and display. Attempts to copy functionality of terminal
 * line-based input, and provides line history recall/editing.
 */
public class TtyInput implements LineReader, Functions.Closeable {
	private final FileDescriptor fd;
	private final Reader reader;
	private final ConsoleInput consoleInput;
	private final Termios termios;

	/**
	 * Provide a line reader for stdin. Returns regular reader if not a tty.
	 */
	@SuppressWarnings("resource")
	public static Enclosure<? extends LineReader> in() throws IOException {
		return CUnistd.isatty(STDIN_FILENO) ? Enclosure.of(of()) :
			Enclosure.noOp(LineReader.of(System.in));
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
	public boolean ready() throws IOException {
		return consoleInput.ready();
	}

	@Override
	public void close() {
		LogUtil.close(termios::set);
	}

	private void configureTty(FileDescriptor fd) throws IOException {
		var termios = Termios.get(fd);
		JnaUtil.and(termios.localFlags(), ~(ICANON | ECHO | ECHONL));
		termios.set();
	}
}
