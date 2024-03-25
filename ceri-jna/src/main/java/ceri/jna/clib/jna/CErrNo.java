package ceri.jna.clib.jna;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import ceri.common.util.OsUtil;

/**
 * Error codes from {@code <errno.h>}.
 */
public class CErrNo {
	/** Signifies an undefined code */
	public static final int UNDEFINED = -1;
	
	/* Common codes */
	
	/** Operation not permitted */
	public static final int EPERM = 1;
	/** No such file or directory */
	public static final int ENOENT = 2;
	/** No such process */
	public static final int ESRCH = 3;
	/** Interrupted system call */
	public static final int EINTR = 4;
	/** I/O error */
	public static final int EIO = 5;
	/** No such device or address */
	public static final int ENXIO = 6;
	/** Argument list too long */
	public static final int E2BIG = 7;
	/** Exec format error */
	public static final int ENOEXEC = 8;
	/** Bad file number */
	public static final int EBADF = 9;
	/** No child processes */
	public static final int ECHILD = 10;
	/** Try again */
	public static final int EAGAIN;
	/** Operation would block */
	public static final int EWOULDBLOCK;
	/** Out of memory */
	public static final int ENOMEM = 12;
	/** Permission denied */
	public static final int EACCES = 13;
	/** Bad address */
	public static final int EFAULT = 14;
	/** Block device required */
	public static final int ENOTBLK = 15;
	/** Device or resource busy */
	public static final int EBUSY = 16;
	/** File exists */
	public static final int EEXIST = 17;
	/** Cross-device link */
	public static final int EXDEV = 18;
	/** No such device */
	public static final int ENODEV = 19;
	/** Not a directory */
	public static final int ENOTDIR = 20;
	/** Is a directory */
	public static final int EISDIR = 21;
	/** Invalid argument */
	public static final int EINVAL = 22;
	/** File table overflow */
	public static final int ENFILE = 23;
	/** Too many open files */
	public static final int EMFILE = 24;
	/** Not a typewriter */
	public static final int ENOTTY = 25;
	/** Text file busy */
	public static final int ETXTBSY = 26;
	/** File too large */
	public static final int EFBIG = 27;
	/** No space left on device */
	public static final int ENOSPC = 28;
	/** Illegal seek */
	public static final int ESPIPE = 29;
	/** Read-only file system */
	public static final int EROFS = 30;
	/** Too many links */
	public static final int EMLINK = 31;
	/** Broken pipe */
	public static final int EPIPE = 32;
	/** Math argument out of domain of func */
	public static final int EDOM = 33;
	/** Math result not representable */
	public static final int ERANGE = 34;

	/* OS-defined codes */
	
	/** Resource deadlock would occur */
	public static final int EDEADLK; // (EDEADLOCK)
	/** File name too long */
	public static final int ENAMETOOLONG;
	/** No record locks available */
	public static final int ENOLCK;
	/** Function not implemented */
	public static final int ENOSYS;
	/** Directory not empty */
	public static final int ENOTEMPTY;
	/** Too many symbolic links encountered */
	public static final int ELOOP;
	/** No message of desired type */
	public static final int ENOMSG;
	/** Identifier removed */
	public static final int EIDRM;
	/** Channel number out of range */
	public static final int ECHRNG;
	/** Level 2 not synchronized */
	public static final int EL2NSYNC;
	/** Level 3 halted */
	public static final int EL3HLT;
	/** Level 3 reset */
	public static final int EL3RST;
	/** Link number out of range */
	public static final int ELNRNG;
	/** Protocol driver not attached */
	public static final int EUNATCH;
	/** No CSI structure available */
	public static final int ENOCSI;
	/** Level 2 halted */
	public static final int EL2HLT;
	/** Invalid exchange */
	public static final int EBADE;
	/** Invalid request descriptor */
	public static final int EBADR;
	/** Exchange full */
	public static final int EXFULL;
	/** No anode */
	public static final int ENOANO;
	/** Invalid request code */
	public static final int EBADRQC;
	/** Invalid slot */
	public static final int EBADSLT;
	/** Bad font file format */
	public static final int EBFONT;
	/** Device not a stream */
	public static final int ENOSTR;
	/** No data available */
	public static final int ENODATA;
	/** Timer expired */
	public static final int ETIME;
	/** Out of streams resources */
	public static final int ENOSR;
	/** Machine is not on the network */
	public static final int ENONET;
	/** Package not installed */
	public static final int ENOPKG;
	/** Object is remote */
	public static final int EREMOTE;
	/** Link has been severed */
	public static final int ENOLINK;
	/** Advertise error */
	public static final int EADV;
	/** Srmount error */
	public static final int ESRMNT;
	/** Communication error on send */
	public static final int ECOMM;
	/** Protocol error */
	public static final int EPROTO;
	/** Multi-hop attempted */
	public static final int EMULTIHOP;
	/** RFS specific error */
	public static final int EDOTDOT;
	/** Not a data message */
	public static final int EBADMSG;
	/** Value too large for defined data type */
	public static final int EOVERFLOW;
	/** Name not unique on network */
	public static final int ENOTUNIQ;
	/** File descriptor in bad state */
	public static final int EBADFD;
	/** Remote address changed */
	public static final int EREMCHG;
	/** Can not access a needed shared library */
	public static final int ELIBACC;
	/** Accessing a corrupted shared library */
	public static final int ELIBBAD;
	/** .lib section in a.out corrupted */
	public static final int ELIBSCN;
	/** Attempting to link in too many shared libraries */
	public static final int ELIBMAX;
	/** Cannot exec a shared library directly */
	public static final int ELIBEXEC;
	/** Illegal byte sequence */
	public static final int EILSEQ;
	/** Interrupted system call should be restarted */
	public static final int ERESTART;
	/** Streams pipe error */
	public static final int ESTRPIPE;
	/** Too many users */
	public static final int EUSERS;
	/** Socket operation on non-socket */
	public static final int ENOTSOCK;
	/** Destination address required */
	public static final int EDESTADDRREQ;
	/** Message too long */
	public static final int EMSGSIZE;
	/** Protocol wrong type for socket */
	public static final int EPROTOTYPE;
	/** Protocol not available */
	public static final int ENOPROTOOPT;
	/** Protocol not supported */
	public static final int EPROTONOSUPPORT;
	/** Socket type not supported */
	public static final int ESOCKTNOSUPPORT;
	/** Operation not supported on transport endpoint */
	public static final int EOPNOTSUPP;
	/** Operation not supported */
	public static final int ENOTSUP;
	/** Protocol family not supported */
	public static final int EPFNOSUPPORT;
	/** Address family not supported by protocol */
	public static final int EAFNOSUPPORT;
	/** Address already in use */
	public static final int EADDRINUSE;
	/** Cannot assign requested address */
	public static final int EADDRNOTAVAIL;
	/** Network is down */
	public static final int ENETDOWN;
	/** Network is unreachable */
	public static final int ENETUNREACH;
	/** Network dropped connection because of reset */
	public static final int ENETRESET;
	/** Software caused connection abort */
	public static final int ECONNABORTED;
	/** Connection reset by peer */
	public static final int ECONNRESET;
	/** No buffer space available */
	public static final int ENOBUFS;
	/** Transport endpoint is already connected */
	public static final int EISCONN;
	/** Transport endpoint is not connected */
	public static final int ENOTCONN;
	/** Cannot send after transport endpoint shutdown */
	public static final int ESHUTDOWN;
	/** Too many references: cannot splice */
	public static final int ETOOMANYREFS;
	/** Connection timed out */
	public static final int ETIMEDOUT;
	/** Connection refused */
	public static final int ECONNREFUSED;
	/** Host is down */
	public static final int EHOSTDOWN;
	/** No route to host */
	public static final int EHOSTUNREACH;
	/** Operation already in progress */
	public static final int EALREADY;
	/** Operation now in progress */
	public static final int EINPROGRESS;
	/** Stale NFS file handle */
	public static final int ESTALE;
	/** Structure needs cleaning */
	public static final int EUCLEAN;
	/** Not a XENIX named type file */
	public static final int ENOTNAM;
	/** No XENIX semaphores available */
	public static final int ENAVAIL;
	/** Is a named type file */
	public static final int EISNAM;
	/** Remote I/O error */
	public static final int EREMOTEIO;
	/** Quota exceeded */
	public static final int EDQUOT;
	/** No medium found */
	public static final int ENOMEDIUM;
	/** Wrong medium type */
	public static final int EMEDIUMTYPE;
	/** Operation Canceled */
	public static final int ECANCELED;
	/** Required key not available */
	public static final int ENOKEY;
	/** Key has expired */
	public static final int EKEYEXPIRED;
	/** Key has been revoked */
	public static final int EKEYREVOKED;
	/** Key was rejected by service */
	public static final int EKEYREJECTED;
	/** For robust mutexes */
	/** Owner died */
	public static final int EOWNERDEAD;
	/** State not recoverable */
	public static final int ENOTRECOVERABLE;
	/** Operation not possible due to RF-kill */
	public static final int ERFKILL;
	/** Memory page has hardware error */
	public static final int EHWPOISON;

	/* Mac only */

	/** Authentication error */
	public static final int EAUTH;
	/** RPC struct is bad */
	public static final int EBADRPC;
	/** Inappropriate file type or format */
	public static final int EFTYPE;
	/** Need authenticator */
	public static final int ENEEDAUTH;
	/** Attribute not found */
	public static final int ENOATTR;
	/** Too many processes */
	public static final int EPROCLIM;
	/** Bad procedure for program */
	public static final int EPROCUNAVAIL;
	/** Program version wrong */
	public static final int EPROGMISMATCH;
	/** RPC prog. not avail */
	public static final int EPROGUNAVAIL;
	/** RPC version wrong */
	public static final int ERPCMISMATCH;

	private CErrNo() {}

	/**
	 * Returns an immutable set of error codes, ignoring undefined codes.
	 */
	public static Set<Integer> codes(int... errorCodes) {
		return IntStream.of(errorCodes).filter(c -> c != UNDEFINED).boxed()
			.collect(Collectors.toUnmodifiableSet());
	}

	static {
		if (OsUtil.os().mac) {
			EAGAIN = 35;
			EWOULDBLOCK = 35;
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
			EWOULDBLOCK = 11;
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
