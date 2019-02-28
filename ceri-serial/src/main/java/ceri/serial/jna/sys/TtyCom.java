package ceri.serial.jna.sys;

import static ceri.serial.jna.sys.IocCom._IO;
import static ceri.serial.jna.sys.IocCom._IOR;
import static ceri.serial.jna.sys.IocCom._IOW;

/**
 * From ttycom.h
 */
public class TtyCom {
	private static final int SIZEOF_WINSIZE = SizeOf.SHORT * 4;
	private static final int SIZEOF_TERMIOS = (SizeOf.INT * 6) + (SizeOf.CHAR * 20);
	private static final int SIZEOF_TIMEVAL = SizeOf.LONG * 2;

	public static final int TIOCMODG = _IOR('t', 3, SizeOf.INT); /* get modem control state */
	public static final int TIOCMODS = _IOW('t', 4, SizeOf.INT); /* set modem control state */
	public static final int TIOCM_LE = 0001; /* line enable */
	public static final int TIOCM_DTR = 0002; /* data terminal ready */
	public static final int TIOCM_RTS = 0004; /* request to send */
	public static final int TIOCM_ST = 0010; /* secondary transmit */
	public static final int TIOCM_SR = 0020; /* secondary receive */
	public static final int TIOCM_CTS = 0040; /* clear to send */
	public static final int TIOCM_CAR = 0100; /* carrier detect */
	public static final int TIOCM_CD = TIOCM_CAR;
	public static final int TIOCM_RNG = 0200; /* ring */
	public static final int TIOCM_RI = TIOCM_RNG;
	public static final int TIOCM_DSR = 0400; /* data set ready */
	/* 8-10 compat */
	public static final int TIOCEXCL = _IO('t', 13); /* set exclusive use of tty */
	public static final int TIOCNXCL = _IO('t', 14); /* reset exclusive use of tty */
	/* 15 unused */
	public static final int TIOCFLUSH = _IOW('t', 16, Integer.SIZE); /* flush buffers */
	/* 17-18 compat */
	public static final int TIOCGETA = _IOR('t', 19, SIZEOF_TERMIOS); /* get termios struct */
	public static final int TIOCSETA = _IOW('t', 20, SIZEOF_TERMIOS); /* set termios struct */
	public static final int TIOCSETAW = _IOW('t', 21, SIZEOF_TERMIOS); /* drain output, set */
	public static final int TIOCSETAF = _IOW('t', 22, SIZEOF_TERMIOS); /* drn out, fls in, set */
	public static final int TIOCGETD = _IOR('t', 26, SizeOf.INT); /* get line discipline */
	public static final int TIOCSETD = _IOW('t', 27, SizeOf.INT); /* set line discipline */
	
	public static void main(String[] args) {
		System.out.printf("TIOCSBRK = 0x%x%n", TIOCSBRK);
		System.out.printf("TIOCCBRK = 0x%x%n", TIOCCBRK);
	}
	
	/* 127-124 compat */
	public static final int TIOCSBRK = 0x5427; //_IO('t', 123); /* set break bit */
	public static final int TIOCCBRK = 0x5428; //_IO('t', 122); /* clear break bit */
	public static final int TIOCSDTR = _IO('t', 121); /* set data terminal ready */
	public static final int TIOCCDTR = _IO('t', 120); /* clear data terminal ready */
	public static final int TIOCGPGRP = _IOR('t', 119, SizeOf.INT); /* get pgrp of tty */
	public static final int TIOCSPGRP = _IOW('t', 118, SizeOf.INT); /* set pgrp of tty */
	/* 117-116 compat */
	public static final int TIOCOUTQ = _IOR('t', 115, SizeOf.INT); /* output queue size */
	public static final int TIOCSTI = _IOW('t', 114, SizeOf.CHAR); /* simulate terminal input */
	public static final int TIOCNOTTY = _IO('t', 113); /* void tty association */
	public static final int TIOCPKT = _IOW('t', 112, SizeOf.INT); /* pty: set/clear packet mode */
	public static final int TIOCPKT_DATA = 0x00; /* data packet */
	public static final int TIOCPKT_FLUSHREAD = 0x01; /* flush packet */
	public static final int TIOCPKT_FLUSHWRITE = 0x02; /* flush packet */
	public static final int TIOCPKT_STOP = 0x04; /* stop output */
	public static final int TIOCPKT_START = 0x08; /* start output */
	public static final int TIOCPKT_NOSTOP = 0x10; /* no more ^S, ^Q */
	public static final int TIOCPKT_DOSTOP = 0x20; /* now do ^S ^Q */
	public static final int TIOCPKT_IOCTL = 0x40; /* state change of pty driver */
	public static final int TIOCSTOP = _IO('t', 111); /* stop output, like ^S */
	public static final int TIOCSTART = _IO('t', 110); /* start output, like ^Q */
	public static final int TIOCMSET = _IOW('t', 109, SizeOf.INT); /* set all modem bits */
	public static final int TIOCMBIS = _IOW('t', 108, SizeOf.INT); /* bis modem bits */
	public static final int TIOCMBIC = _IOW('t', 107, SizeOf.INT); /* bic modem bits */
	public static final int TIOCMGET = _IOR('t', 106, SizeOf.INT); /* get all modem bits */
	public static final int TIOCREMOTE = _IOW('t', 105, SizeOf.INT); /* remote input editing */
	public static final int TIOCGWINSZ = _IOR('t', 104, SIZEOF_WINSIZE); /* get window size */
	public static final int TIOCSWINSZ = _IOW('t', 103, SIZEOF_WINSIZE); /* set window size */
	public static final int TIOCUCNTL = _IOW('t', 102, SizeOf.INT); /* pty: set/clr usr cntl mode */
	public static final int TIOCSTAT = _IO('t', 101); /* simulate ^T status message */
	public static final int TIOCSCONS = _IO('t', 99); /* 4.2 compatibility */
	public static final int TIOCCONS = _IOW('t', 98, SizeOf.INT); /* become virtual console */
	public static final int TIOCSCTTY = _IO('t', 97); /* become controlling tty */
	public static final int TIOCEXT = _IOW('t', 96, SizeOf.INT); /* pty: external processing */
	public static final int TIOCSIG = _IO('t', 95); /* pty: generate signal */
	public static final int TIOCDRAIN = _IO('t', 94); /* wait till output drained */
	public static final int TIOCMSDTRWAIT = _IOW('t', 91, SizeOf.INT);
	/* modem: set wait on close */
	public static final int TIOCMGDTRWAIT = _IOR('t', 90, SizeOf.INT);
	/* modem: get wait on close */
	public static final int TIOCTIMESTAMP = _IOR('t', 89, SIZEOF_TIMEVAL);
	/* enable/get timestamp of last input event */
	public static final int TIOCDCDTIMESTAMP = _IOR('t', 88, SIZEOF_TIMEVAL);
	/* enable/get timestamp of last DCd rise */
	public static final int TIOCSDRAINWAIT = _IOW('t', 87, SizeOf.INT); /* set ttywait timeout */
	public static final int TIOCGDRAINWAIT = _IOR('t', 86, SizeOf.INT); /* get ttywait timeout */
	public static final int TIOCDSIMICROCODE = _IO('t', 85);
	/* download microcode to DSI Softmodem */

	public static final int TTYDISC = 0; /* termios tty line discipline */
	public static final int TABLDISC = 3; /* tablet discipline */
	public static final int SLIPDISC = 4; /* serial IP discipline */
	public static final int PPPDISC = 5; /* PPP discipline */

	private TtyCom() {}

	/**
	 * UIOCCMD(n)
	 */
	public static int UIOCCMD(int n) {
		return _IO('u', n); /* usr cntl op "n" */
	}

}
