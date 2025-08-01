package ceri.jna.clib;

import static ceri.jna.clib.jna.CTermios.TCSANOW;
import java.io.IOException;
import ceri.common.io.Direction;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.jna.CTermios.tcflag_t;
import ceri.jna.clib.jna.CTermios.termios;
import ceri.jna.type.Struct;

/**
 * Encapsulates a file descriptor and termios structure for terminal/communications control.
 */
public class Termios {
	private final int fd;
	private final termios termios;

	public static Termios get(FileDescriptor fd) throws IOException {
		return fd.apply(f -> Termios.get(f));
	}

	public static Termios get(int fd) throws IOException {
		return new Termios(fd, CTermios.tcgetattr(fd));
	}

	private Termios(int fd, termios termios) {
		this.fd = fd;
		this.termios = termios;
	}

	/**
	 * Set termios attributes.
	 */
	public void set() throws IOException {
		CTermios.tcsetattr(fd, TCSANOW, termios);
	}

	/**
	 * Access input flags.
	 */
	public tcflag_t inFlags() {
		return termios.c_iflag;
	}

	/**
	 * Access output flags.
	 */
	public tcflag_t outFlags() {
		return termios.c_oflag;
	}

	/**
	 * Access control flags.
	 */
	public tcflag_t controlFlags() {
		return termios.c_cflag;
	}

	/**
	 * Access local flags.
	 */
	public tcflag_t localFlags() {
		return termios.c_lflag;
	}

	/**
	 * Get control char at index.
	 */
	public int controlChar(int index) {
		return termios.c_cc[index];
	}

	/**
	 * Set control char at index.
	 */
	public Termios controlChar(int index, int value) {
		termios.c_cc[index] = (byte) value;
		return this;
	}

	/**
	 * Get input speed.
	 */
	public int inSpeed() throws IOException {
		return CTermios.cfgetispeed(termios);
	}

	/**
	 * Set input speed.
	 */
	public Termios inSpeed(int speed) throws IOException {
		Struct.write(termios);
		CTermios.cfsetispeed(termios, speed);
		return this;
	}

	/**
	 * Get input speed.
	 */
	public int outSpeed() throws IOException {
		return CTermios.cfgetospeed(termios);
	}

	/**
	 * Set input speed.
	 */
	public Termios outSpeed(int speed) throws IOException {
		Struct.write(termios);
		CTermios.cfsetospeed(termios, speed);
		return this;
	}

	/**
	 * Configure for raw mode.
	 */
	public Termios makeRaw() throws IOException {
		Struct.write(termios);
		CTermios.cfmakeraw(termios);
		return this;
	}

	/**
	 * Send a stream of zero bytes. Duration value is terminal-specific.
	 */
	public Termios sendBreak(int duration) throws IOException {
		CTermios.tcsendbreak(fd, duration);
		return this;
	}

	/**
	 * Waits until all output has been written.
	 */
	public Termios drain() throws IOException {
		CTermios.tcdrain(fd);
		return this;
	}

	/**
	 * Discards data from input/output streams.
	 */
	public Termios flush(Direction direction) throws IOException {
		switch (direction) {
			case in -> CTermios.tcflush(fd, CTermios.TCIFLUSH);
			case out -> CTermios.tcflush(fd, CTermios.TCOFLUSH);
			case duplex -> CTermios.tcflush(fd, CTermios.TCIOFLUSH);
			default -> {} // do nothing
		}
		return this;
	}

	/**
	 * Suspends or restarts data flow.
	 */
	public Termios flow(Direction direction, boolean on) throws IOException {
		switch (direction) {
			case in -> CTermios.tcflow(fd, on ? CTermios.TCION : CTermios.TCIOFF);
			case out -> CTermios.tcflow(fd, on ? CTermios.TCOON : CTermios.TCOOFF);
			case duplex -> flow(Direction.in, on).flow(Direction.out, on);
			default -> {} // do nothing
		}
		return this;
	}
}
