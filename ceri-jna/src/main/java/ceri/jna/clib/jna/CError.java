package ceri.jna.clib.jna;

import java.util.stream.Stream;
import ceri.common.data.TypeTranscoder;
import ceri.common.util.OsUtil;

/**
 * Error types and codes.
 */
public enum CError {

	/* errno-base.h */

	/** Operation not permitted */
	EPERM(1),
	/** No such file or directory */
	ENOENT(2),
	/** No such process */
	ESRCH(3),
	/** Interrupted system call */
	EINTR(4),
	/** I/O error */
	EIO(5),
	/** No such device or address */
	ENXIO(6),
	/** Argument list too long */
	E2BIG(7),
	/** Exec format error */
	ENOEXEC(8),
	/** Bad file number */
	EBADF(9),
	/** No child processes */
	ECHILD(10),
	/** Try again */
	EAGAIN(Os.EAGAIN), // (EWOULDBLOCK)
	/** Out of memory */
	ENOMEM(12),
	/** Permission denied */
	EACCES(13),
	/** Bad address */
	EFAULT(14),
	/** Block device required */
	ENOTBLK(15),
	/** Device or resource busy */
	EBUSY(16),
	/** File exists */
	EEXIST(17),
	/** Cross-device link */
	EXDEV(18),
	/** No such device */
	ENODEV(19),
	/** Not a directory */
	ENOTDIR(20),
	/** Is a directory */
	EISDIR(21),
	/** Invalid argument */
	EINVAL(22),
	/** File table overflow */
	ENFILE(23),
	/** Too many open files */
	EMFILE(24),
	/** Not a typewriter */
	ENOTTY(25),
	/** Text file busy */
	ETXTBSY(26),
	/** File too large */
	EFBIG(27),
	/** No space left on device */
	ENOSPC(28),
	/** Illegal seek */
	ESPIPE(29),
	/** Read-only file system */
	EROFS(30),
	/** Too many links */
	EMLINK(31),
	/** Broken pipe */
	EPIPE(32),
	/** Math argument out of domain of func */
	EDOM(33),
	/** Math result not representable */
	ERANGE(34),

	/* errno.h */

	/** Resource deadlock would occur */
	EDEADLK(Os.EDEADLK), // (EDEADLOCK)
	/** File name too long */
	ENAMETOOLONG(Os.ENAMETOOLONG),
	/** No record locks available */
	ENOLCK(Os.ENOLCK),
	/** Function not implemented */
	ENOSYS(Os.ENOSYS),
	/** Directory not empty */
	ENOTEMPTY(Os.ENOTEMPTY),
	/** Too many symbolic links encountered */
	ELOOP(Os.ELOOP),
	/** No message of desired type */
	ENOMSG(Os.ENOMSG),
	/** Identifier removed */
	EIDRM(Os.EIDRM),
	/** Channel number out of range */
	ECHRNG(Os.ECHRNG),
	/** Level 2 not synchronized */
	EL2NSYNC(Os.EL2NSYNC),
	/** Level 3 halted */
	EL3HLT(Os.EL3HLT),
	/** Level 3 reset */
	EL3RST(Os.EL3RST),
	/** Link number out of range */
	ELNRNG(Os.ELNRNG),
	/** Protocol driver not attached */
	EUNATCH(Os.EUNATCH),
	/** No CSI structure available */
	ENOCSI(Os.ENOCSI),
	/** Level 2 halted */
	EL2HLT(Os.EL2HLT),
	/** Invalid exchange */
	EBADE(Os.EBADE),
	/** Invalid request descriptor */
	EBADR(Os.EBADR),
	/** Exchange full */
	EXFULL(Os.EXFULL),
	/** No anode */
	ENOANO(Os.ENOANO),
	/** Invalid request code */
	EBADRQC(Os.EBADRQC),
	/** Invalid slot */
	EBADSLT(Os.EBADSLT),
	/** Bad font file format */
	EBFONT(Os.EBFONT),
	/** Device not a stream */
	ENOSTR(Os.ENOSTR),
	/** No data available */
	ENODATA(Os.ENODATA),
	/** Timer expired */
	ETIME(Os.ETIME),
	/** Out of streams resources */
	ENOSR(Os.ENOSR),
	/** Machine is not on the network */
	ENONET(Os.ENONET),
	/** Package not installed */
	ENOPKG(Os.ENOPKG),
	/** Object is remote */
	EREMOTE(Os.EREMOTE),
	/** Link has been severed */
	ENOLINK(Os.ENOLINK),
	/** Advertise error */
	EADV(Os.EADV),
	/** Srmount error */
	ESRMNT(Os.ESRMNT),
	/** Communication error on send */
	ECOMM(Os.ECOMM),
	/** Protocol error */
	EPROTO(Os.EPROTO),
	/** Multi-hop attempted */
	EMULTIHOP(Os.EMULTIHOP),
	/** RFS specific error */
	EDOTDOT(Os.EDOTDOT),
	/** Not a data message */
	EBADMSG(Os.EBADMSG),
	/** Value too large for defined data type */
	EOVERFLOW(Os.EOVERFLOW),
	/** Name not unique on network */
	ENOTUNIQ(Os.ENOTUNIQ),
	/** File descriptor in bad state */
	EBADFD(Os.EBADFD),
	/** Remote address changed */
	EREMCHG(Os.EREMCHG),
	/** Can not access a needed shared library */
	ELIBACC(Os.ELIBACC),
	/** Accessing a corrupted shared library */
	ELIBBAD(Os.ELIBBAD),
	/** .lib section in a.out corrupted */
	ELIBSCN(Os.ELIBSCN),
	/** Attempting to link in too many shared libraries */
	ELIBMAX(Os.ELIBMAX),
	/** Cannot exec a shared library directly */
	ELIBEXEC(Os.ELIBEXEC),
	/** Illegal byte sequence */
	EILSEQ(Os.EILSEQ),
	/** Interrupted system call should be restarted */
	ERESTART(Os.ERESTART),
	/** Streams pipe error */
	ESTRPIPE(Os.ESTRPIPE),
	/** Too many users */
	EUSERS(Os.EUSERS),
	/** Socket operation on non-socket */
	ENOTSOCK(Os.ENOTSOCK),
	/** Destination address required */
	EDESTADDRREQ(Os.EDESTADDRREQ),
	/** Message too long */
	EMSGSIZE(Os.EMSGSIZE),
	/** Protocol wrong type for socket */
	EPROTOTYPE(Os.EPROTOTYPE),
	/** Protocol not available */
	ENOPROTOOPT(Os.ENOPROTOOPT),
	/** Protocol not supported */
	EPROTONOSUPPORT(Os.EPROTONOSUPPORT),
	/** Socket type not supported */
	ESOCKTNOSUPPORT(Os.ESOCKTNOSUPPORT),
	/** Operation not supported on transport endpoint */
	EOPNOTSUPP(Os.EOPNOTSUPP),
	/** Operation not supported */
	ENOTSUP(Os.ENOTSUP),
	/** Protocol family not supported */
	EPFNOSUPPORT(Os.EPFNOSUPPORT),
	/** Address family not supported by protocol */
	EAFNOSUPPORT(Os.EAFNOSUPPORT),
	/** Address already in use */
	EADDRINUSE(Os.EADDRINUSE),
	/** Cannot assign requested address */
	EADDRNOTAVAIL(Os.EADDRNOTAVAIL),
	/** Network is down */
	ENETDOWN(Os.ENETDOWN),
	/** Network is unreachable */
	ENETUNREACH(Os.ENETUNREACH),
	/** Network dropped connection because of reset */
	ENETRESET(Os.ENETRESET),
	/** Software caused connection abort */
	ECONNABORTED(Os.ECONNABORTED),
	/** Connection reset by peer */
	ECONNRESET(Os.ECONNRESET),
	/** No buffer space available */
	ENOBUFS(Os.ENOBUFS),
	/** Transport endpoint is already connected */
	EISCONN(Os.EISCONN),
	/** Transport endpoint is not connected */
	ENOTCONN(Os.ENOTCONN),
	/** Cannot send after transport endpoint shutdown */
	ESHUTDOWN(Os.ESHUTDOWN),
	/** Too many references: cannot splice */
	ETOOMANYREFS(Os.ETOOMANYREFS),
	/** Connection timed out */
	ETIMEDOUT(Os.ETIMEDOUT),
	/** Connection refused */
	ECONNREFUSED(Os.ECONNREFUSED),
	/** Host is down */
	EHOSTDOWN(Os.EHOSTDOWN),
	/** No route to host */
	EHOSTUNREACH(Os.EHOSTUNREACH),
	/** Operation already in progress */
	EALREADY(Os.EALREADY),
	/** Operation now in progress */
	EINPROGRESS(Os.EINPROGRESS),
	/** Stale NFS file handle */
	ESTALE(Os.ESTALE),
	/** Structure needs cleaning */
	EUCLEAN(Os.EUCLEAN),
	/** Not a XENIX named type file */
	ENOTNAM(Os.ENOTNAM),
	/** No XENIX semaphores available */
	ENAVAIL(Os.ENAVAIL),
	/** Is a named type file */
	EISNAM(Os.EISNAM),
	/** Remote I/O error */
	EREMOTEIO(Os.EREMOTEIO),
	/** Quota exceeded */
	EDQUOT(Os.EDQUOT),
	/** No medium found */
	ENOMEDIUM(Os.ENOMEDIUM),
	/** Wrong medium type */
	EMEDIUMTYPE(Os.EMEDIUMTYPE),
	/** Operation Canceled */
	ECANCELED(Os.ECANCELED),
	/** Required key not available */
	ENOKEY(Os.ENOKEY),
	/** Key has expired */
	EKEYEXPIRED(Os.EKEYEXPIRED),
	/** Key has been revoked */
	EKEYREVOKED(Os.EKEYREVOKED),
	/** Key was rejected by service */
	EKEYREJECTED(Os.EKEYREJECTED),
	/** For robust mutexes */
	/** Owner died */
	EOWNERDEAD(Os.EOWNERDEAD),
	/** State not recoverable */
	ENOTRECOVERABLE(Os.ENOTRECOVERABLE),
	/** Operation not possible due to RF-kill */
	ERFKILL(Os.ERFKILL),
	/** Memory page has hardware error */
	EHWPOISON(Os.EHWPOISON),

	/* Mac only */

	/** Authentication error */
	EAUTH(Os.EAUTH),
	/** RPC struct is bad */
	EBADRPC(Os.EBADRPC),
	/** Inappropriate file type or format */
	EFTYPE(Os.EFTYPE),
	/** Need authenticator */
	ENEEDAUTH(Os.ENEEDAUTH),
	/** Attribute not found */
	ENOATTR(Os.ENOATTR),
	/** Too many processes */
	EPROCLIM(Os.EPROCLIM),
	/** Bad procedure for program */
	EPROCUNAVAIL(Os.EPROCUNAVAIL),
	/** Program version wrong */
	EPROGMISMATCH(Os.EPROGMISMATCH),
	/** RPC prog. not avail */
	EPROGUNAVAIL(Os.EPROGUNAVAIL),
	/** RPC version wrong */
	ERPCMISMATCH(Os.ERPCMISMATCH);

	private static final TypeTranscoder<CError> xcoder = TypeTranscoder.of(t -> t.code,
		Stream.of(CError.values()).filter(t -> t.code != Os.UNDEFINED).toList());
	public static final int UNDEFINED = Os.UNDEFINED;
	public final int code;

	public static CError from(int code) {
		return xcoder.decode(code);
	}

	private CError(int code) {
		this.code = code;
	}

	public boolean undefined() {
		return code == UNDEFINED;
	}

	private static class Os {
		private static final int UNDEFINED = -1;
		private static final int EAGAIN;
		private static final int EDEADLK;
		private static final int ENAMETOOLONG;
		private static final int ENOLCK;
		private static final int ENOSYS;
		private static final int ENOTEMPTY;
		private static final int ELOOP;
		private static final int ENOMSG;
		private static final int EIDRM;
		private static final int ECHRNG;
		private static final int EL2NSYNC;
		private static final int EL3HLT;
		private static final int EL3RST;
		private static final int ELNRNG;
		private static final int EUNATCH;
		private static final int ENOCSI;
		private static final int EL2HLT;
		private static final int EBADE;
		private static final int EBADR;
		private static final int EXFULL;
		private static final int ENOANO;
		private static final int EBADRQC;
		private static final int EBADSLT;
		private static final int EBFONT;
		private static final int ENOSTR;
		private static final int ENODATA;
		private static final int ETIME;
		private static final int ENOSR;
		private static final int ENONET;
		private static final int ENOPKG;
		private static final int EREMOTE;
		private static final int ENOLINK;
		private static final int EADV;
		private static final int ESRMNT;
		private static final int ECOMM;
		private static final int EPROTO;
		private static final int EMULTIHOP;
		private static final int EDOTDOT;
		private static final int EBADMSG;
		private static final int EOVERFLOW;
		private static final int ENOTUNIQ;
		private static final int EBADFD;
		private static final int EREMCHG;
		private static final int ELIBACC;
		private static final int ELIBBAD;
		private static final int ELIBSCN;
		private static final int ELIBMAX;
		private static final int ELIBEXEC;
		private static final int EILSEQ;
		private static final int ERESTART;
		private static final int ESTRPIPE;
		private static final int EUSERS;
		private static final int ENOTSOCK;
		private static final int EDESTADDRREQ;
		private static final int EMSGSIZE;
		private static final int EPROTOTYPE;
		private static final int ENOPROTOOPT;
		private static final int EPROTONOSUPPORT;
		private static final int ESOCKTNOSUPPORT;
		private static final int EOPNOTSUPP;
		private static final int ENOTSUP;
		private static final int EPFNOSUPPORT;
		private static final int EAFNOSUPPORT;
		private static final int EADDRINUSE;
		private static final int EADDRNOTAVAIL;
		private static final int ENETDOWN;
		private static final int ENETUNREACH;
		private static final int ENETRESET;
		private static final int ECONNABORTED;
		private static final int ECONNRESET;
		private static final int ENOBUFS;
		private static final int EISCONN;
		private static final int ENOTCONN;
		private static final int ESHUTDOWN;
		private static final int ETOOMANYREFS;
		private static final int ETIMEDOUT;
		private static final int ECONNREFUSED;
		private static final int EHOSTDOWN;
		private static final int EHOSTUNREACH;
		private static final int EALREADY;
		private static final int EINPROGRESS;
		private static final int ESTALE;
		private static final int EUCLEAN;
		private static final int ENOTNAM;
		private static final int ENAVAIL;
		private static final int EISNAM;
		private static final int EREMOTEIO;
		private static final int EDQUOT;
		private static final int ENOMEDIUM;
		private static final int EMEDIUMTYPE;
		private static final int ECANCELED;
		private static final int ENOKEY;
		private static final int EKEYEXPIRED;
		private static final int EKEYREVOKED;
		private static final int EKEYREJECTED;
		private static final int EOWNERDEAD;
		private static final int ENOTRECOVERABLE;
		private static final int ERFKILL;
		private static final int EHWPOISON;
		/* Mac only */
		private static final int EAUTH;
		private static final int EBADRPC;
		private static final int EFTYPE;
		private static final int ENEEDAUTH;
		private static final int ENOATTR;
		private static final int EPROCLIM;
		private static final int EPROCUNAVAIL;
		private static final int EPROGMISMATCH;
		private static final int EPROGUNAVAIL;
		private static final int ERPCMISMATCH;

		private Os() {}

		static {
			if (OsUtil.IS_MAC) {
				EAGAIN = 35;
				EDEADLK = 11;
				ENAMETOOLONG = 63;
				ENOLCK = 77;
				ENOSYS = 78;
				ENOTEMPTY = 66;
				ELOOP = 62;
				ENOMSG = 91;
				EIDRM = 90;
				ECHRNG = UNDEFINED;
				EL2NSYNC = UNDEFINED;
				EL3HLT = UNDEFINED;
				EL3RST = UNDEFINED;
				ELNRNG = UNDEFINED;
				EUNATCH = UNDEFINED;
				ENOCSI = UNDEFINED;
				EL2HLT = UNDEFINED;
				EBADE = UNDEFINED;
				EBADR = UNDEFINED;
				EXFULL = UNDEFINED;
				ENOANO = UNDEFINED;
				EBADRQC = UNDEFINED;
				EBADSLT = UNDEFINED;
				EBFONT = UNDEFINED;
				ENOSTR = 99;
				ENODATA = 96;
				ETIME = 101;
				ENOSR = 98;
				ENONET = UNDEFINED;
				ENOPKG = UNDEFINED;
				EREMOTE = 71;
				ENOLINK = 97;
				EADV = UNDEFINED;
				ESRMNT = UNDEFINED;
				ECOMM = UNDEFINED;
				EPROTO = 100;
				EMULTIHOP = 95;
				EDOTDOT = UNDEFINED;
				EBADMSG = 94;
				EOVERFLOW = 84;
				ENOTUNIQ = UNDEFINED;
				EBADFD = UNDEFINED;
				EREMCHG = UNDEFINED;
				ELIBACC = UNDEFINED;
				ELIBBAD = UNDEFINED;
				ELIBSCN = UNDEFINED;
				ELIBMAX = UNDEFINED;
				ELIBEXEC = UNDEFINED;
				EILSEQ = 92;
				ERESTART = UNDEFINED;
				ESTRPIPE = UNDEFINED;
				EUSERS = 68;
				ENOTSOCK = 38;
				EDESTADDRREQ = 39;
				EMSGSIZE = 40;
				EPROTOTYPE = 41;
				ENOPROTOOPT = 42;
				EPROTONOSUPPORT = 43;
				ESOCKTNOSUPPORT = 44;
				EOPNOTSUPP = 102;
				ENOTSUP = 45;
				EPFNOSUPPORT = 46;
				EAFNOSUPPORT = 47;
				EADDRINUSE = 48;
				EADDRNOTAVAIL = 49;
				ENETDOWN = 50;
				ENETUNREACH = 51;
				ENETRESET = 52;
				ECONNABORTED = 53;
				ECONNRESET = 54;
				ENOBUFS = 55;
				EISCONN = 56;
				ENOTCONN = 57;
				ESHUTDOWN = 58;
				ETOOMANYREFS = 59;
				ETIMEDOUT = 60;
				ECONNREFUSED = 61;
				EHOSTDOWN = 64;
				EHOSTUNREACH = 65;
				EALREADY = 37;
				EINPROGRESS = 36;
				ESTALE = 70;
				EUCLEAN = UNDEFINED;
				ENOTNAM = UNDEFINED;
				ENAVAIL = UNDEFINED;
				EISNAM = UNDEFINED;
				EREMOTEIO = UNDEFINED;
				EDQUOT = 69;
				ENOMEDIUM = UNDEFINED;
				EMEDIUMTYPE = UNDEFINED;
				ECANCELED = 89;
				ENOKEY = UNDEFINED;
				EKEYEXPIRED = UNDEFINED;
				EKEYREVOKED = UNDEFINED;
				EKEYREJECTED = UNDEFINED;
				EOWNERDEAD = 105;
				ENOTRECOVERABLE = 104;
				ERFKILL = UNDEFINED;
				EHWPOISON = UNDEFINED;
				EAUTH = 80;
				EBADRPC = 72;
				EFTYPE = 79;
				ENEEDAUTH = 81;
				ENOATTR = 93;
				EPROCLIM = 67;
				EPROCUNAVAIL = 76;
				EPROGMISMATCH = 75;
				EPROGUNAVAIL = 74;
				ERPCMISMATCH = 73;
			} else {
				EAGAIN = 11;
				EDEADLK = 35;
				ENAMETOOLONG = 36;
				ENOLCK = 37;
				ENOSYS = 38;
				ENOTEMPTY = 39;
				ELOOP = 40;
				ENOMSG = 42;
				EIDRM = 43;
				ECHRNG = 44;
				EL2NSYNC = 45;
				EL3HLT = 46;
				EL3RST = 47;
				ELNRNG = 48;
				EUNATCH = 49;
				ENOCSI = 50;
				EL2HLT = 51;
				EBADE = 52;
				EBADR = 53;
				EXFULL = 54;
				ENOANO = 55;
				EBADRQC = 56;
				EBADSLT = 57;
				EBFONT = 59;
				ENOSTR = 60;
				ENODATA = 61;
				ETIME = 62;
				ENOSR = 63;
				ENONET = 64;
				ENOPKG = 65;
				EREMOTE = 66;
				ENOLINK = 67;
				EADV = 68;
				ESRMNT = 69;
				ECOMM = 70;
				EPROTO = 71;
				EMULTIHOP = 72;
				EDOTDOT = 73;
				EBADMSG = 74;
				EOVERFLOW = 75;
				ENOTUNIQ = 76;
				EBADFD = 77;
				EREMCHG = 78;
				ELIBACC = 79;
				ELIBBAD = 80;
				ELIBSCN = 81;
				ELIBMAX = 82;
				ELIBEXEC = 83;
				EILSEQ = 84;
				ERESTART = 85;
				ESTRPIPE = 86;
				EUSERS = 87;
				ENOTSOCK = 88;
				EDESTADDRREQ = 89;
				EMSGSIZE = 90;
				EPROTOTYPE = 91;
				ENOPROTOOPT = 92;
				EPROTONOSUPPORT = 93;
				ESOCKTNOSUPPORT = 94;
				EOPNOTSUPP = 95;
				ENOTSUP = 95;
				EPFNOSUPPORT = 96;
				EAFNOSUPPORT = 97;
				EADDRINUSE = 98;
				EADDRNOTAVAIL = 99;
				ENETDOWN = 100;
				ENETUNREACH = 101;
				ENETRESET = 102;
				ECONNABORTED = 103;
				ECONNRESET = 104;
				ENOBUFS = 105;
				EISCONN = 106;
				ENOTCONN = 107;
				ESHUTDOWN = 108;
				ETOOMANYREFS = 109;
				ETIMEDOUT = 110;
				ECONNREFUSED = 111;
				EHOSTDOWN = 112;
				EHOSTUNREACH = 113;
				EALREADY = 114;
				EINPROGRESS = 115;
				ESTALE = 116;
				EUCLEAN = 117;
				ENOTNAM = 118;
				ENAVAIL = 119;
				EISNAM = 120;
				EREMOTEIO = 121;
				EDQUOT = 122;
				ENOMEDIUM = 123;
				EMEDIUMTYPE = 124;
				ECANCELED = 125;
				ENOKEY = 126;
				EKEYEXPIRED = 127;
				EKEYREVOKED = 128;
				EKEYREJECTED = 129;
				EOWNERDEAD = 130;
				ENOTRECOVERABLE = 131;
				ERFKILL = 132;
				EHWPOISON = 133;
				EAUTH = UNDEFINED;
				EBADRPC = UNDEFINED;
				EFTYPE = UNDEFINED;
				ENEEDAUTH = UNDEFINED;
				ENOATTR = UNDEFINED;
				EPROCLIM = UNDEFINED;
				EPROCUNAVAIL = UNDEFINED;
				EPROGMISMATCH = UNDEFINED;
				EPROGUNAVAIL = UNDEFINED;
				ERPCMISMATCH = UNDEFINED;
			}
		}
	}
}
