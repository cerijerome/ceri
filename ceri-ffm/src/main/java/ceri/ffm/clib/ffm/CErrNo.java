package ceri.ffm.clib.ffm;

import static ceri.ffm.util.FfmOs.linux;
import static ceri.ffm.util.FfmOs.mac;
import java.util.Set;
import ceri.common.data.TypeValue;
import ceri.common.data.Xcoder;
import ceri.common.stream.Streams;
import ceri.common.text.Strings;
import ceri.common.util.Basics;
import ceri.common.util.Os;
import ceri.ffm.core.LastErrorException;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.reflect.CAnnotations.CType;
import ceri.ffm.reflect.CAnnotations.CUndefined;

/**
 * Error codes from {@code <errno.h>}.
 */
@CInclude("errno.h")
public enum CErrNo {
	/** Signifies an undefined code */
	@CUndefined
	UNDEFINED(Const.UNDEFINED),

	// Common codes

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
	EAGAIN(Const.EAGAIN),
	/** Operation would block */
	EWOULDBLOCK(Const.EWOULDBLOCK),
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

	// OS-defined codes

	/** Resource deadlock would occur */
	EDEADLK(Const.EDEADLK), // (EDEADLOCK)
	/** File name too long */
	ENAMETOOLONG(Const.ENAMETOOLONG),
	/** No record locks available */
	ENOLCK(Const.ENOLCK),
	/** Function not implemented */
	ENOSYS(Const.ENOSYS),
	/** Directory not empty */
	ENOTEMPTY(Const.ENOTEMPTY),
	/** Too many symbolic links encountered */
	ELOOP(Const.ELOOP),
	/** No message of desired type */
	ENOMSG(Const.ENOMSG),
	/** Identifier removed */
	EIDRM(Const.EIDRM),
	/** Channel number out of range */
	@CType(os = linux)
	ECHRNG(Const.ECHRNG),
	/** Level 2 not synchronized */
	@CType(os = linux)
	EL2NSYNC(Const.EL2NSYNC),
	/** Level 3 halted */
	@CType(os = linux)
	EL3HLT(Const.EL3HLT),
	/** Level 3 reset */
	@CType(os = linux)
	EL3RST(Const.EL3RST),
	/** Link number out of range */
	@CType(os = linux)
	ELNRNG(Const.ELNRNG),
	/** Protocol driver not attached */
	@CType(os = linux)
	EUNATCH(Const.EUNATCH),
	/** No CSI structure available */
	@CType(os = linux)
	ENOCSI(Const.ENOCSI),
	/** Level 2 halted */
	@CType(os = linux)
	EL2HLT(Const.EL2HLT),
	/** Invalid exchange */
	@CType(os = linux)
	EBADE(Const.EBADE),
	/** Invalid request descriptor */
	@CType(os = linux)
	EBADR(Const.EBADR),
	/** Exchange full */
	@CType(os = linux)
	EXFULL(Const.EXFULL),
	/** No anode */
	@CType(os = linux)
	ENOANO(Const.ENOANO),
	/** Invalid request code */
	@CType(os = linux)
	EBADRQC(Const.EBADRQC),
	/** Invalid slot */
	@CType(os = linux)
	EBADSLT(Const.EBADSLT),
	/** Bad font file format */
	@CType(os = linux)
	EBFONT(Const.EBFONT),
	/** Device not a stream */
	ENOSTR(Const.ENOSTR),
	/** No data available */
	ENODATA(Const.ENODATA),
	/** Timer expired */
	ETIME(Const.ETIME),
	/** Out of streams resources */
	ENOSR(Const.ENOSR),
	/** Machine is not on the network */
	@CType(os = linux)
	ENONET(Const.ENONET),
	/** Package not installed */
	@CType(os = linux)
	ENOPKG(Const.ENOPKG),
	/** Object is remote */
	EREMOTE(Const.EREMOTE),
	/** Link has been severed */
	ENOLINK(Const.ENOLINK),
	/** Advertise error */
	@CType(os = linux)
	EADV(Const.EADV),
	/** Srmount error */
	@CType(os = linux)
	ESRMNT(Const.ESRMNT),
	/** Communication error on send */
	@CType(os = linux)
	ECOMM(Const.ECOMM),
	/** Protocol error */
	EPROTO(Const.EPROTO),
	/** Multi-hop attempted */
	EMULTIHOP(Const.EMULTIHOP),
	/** RFS specific error */
	@CType(os = linux)
	EDOTDOT(Const.EDOTDOT),
	/** Not a data message */
	EBADMSG(Const.EBADMSG),
	/** Value too large for defined data type */
	EOVERFLOW(Const.EOVERFLOW),
	/** Name not unique on network */
	@CType(os = linux)
	ENOTUNIQ(Const.ENOTUNIQ),
	/** File descriptor in bad state */
	@CType(os = linux)
	EBADFD(Const.EBADFD),
	/** Remote address changed */
	@CType(os = linux)
	EREMCHG(Const.EREMCHG),
	/** Can not access a needed shared library */
	@CType(os = linux)
	ELIBACC(Const.ELIBACC),
	/** Accessing a corrupted shared library */
	@CType(os = linux)
	ELIBBAD(Const.ELIBBAD),
	/** .lib section in a.out corrupted */
	@CType(os = linux)
	ELIBSCN(Const.ELIBSCN),
	/** Attempting to link in too many shared libraries */
	@CType(os = linux)
	ELIBMAX(Const.ELIBMAX),
	/** Cannot exec a shared library directly */
	@CType(os = linux)
	ELIBEXEC(Const.ELIBEXEC),
	/** Illegal byte sequence */
	EILSEQ(Const.EILSEQ),
	/** Interrupted system call should be restarted */
	@CType(os = linux)
	ERESTART(Const.ERESTART),
	/** Streams pipe error */
	@CType(os = linux)
	ESTRPIPE(Const.ESTRPIPE),
	/** Too many users */
	EUSERS(Const.EUSERS),
	/** Socket operation on non-socket */
	ENOTSOCK(Const.ENOTSOCK),
	/** Destination address required */
	EDESTADDRREQ(Const.EDESTADDRREQ),
	/** Message too long */
	EMSGSIZE(Const.EMSGSIZE),
	/** Protocol wrong type for socket */
	EPROTOTYPE(Const.EPROTOTYPE),
	/** Protocol not available */
	ENOPROTOOPT(Const.ENOPROTOOPT),
	/** Protocol not supported */
	EPROTONOSUPPORT(Const.EPROTONOSUPPORT),
	/** Socket type not supported */
	ESOCKTNOSUPPORT(Const.ESOCKTNOSUPPORT),
	/** Operation not supported on transport endpoint */
	EOPNOTSUPP(Const.EOPNOTSUPP),
	/** Operation not supported */
	ENOTSUP(Const.ENOTSUP),
	/** Protocol family not supported */
	EPFNOSUPPORT(Const.EPFNOSUPPORT),
	/** Address family not supported by protocol */
	EAFNOSUPPORT(Const.EAFNOSUPPORT),
	/** Address already in use */
	EADDRINUSE(Const.EADDRINUSE),
	/** Cannot assign requested address */
	EADDRNOTAVAIL(Const.EADDRNOTAVAIL),
	/** Network is down */
	ENETDOWN(Const.ENETDOWN),
	/** Network is unreachable */
	ENETUNREACH(Const.ENETUNREACH),
	/** Network dropped connection because of reset */
	ENETRESET(Const.ENETRESET),
	/** Software caused connection abort */
	ECONNABORTED(Const.ECONNABORTED),
	/** Connection reset by peer */
	ECONNRESET(Const.ECONNRESET),
	/** No buffer space available */
	ENOBUFS(Const.ENOBUFS),
	/** Transport endpoint is already connected */
	EISCONN(Const.EISCONN),
	/** Transport endpoint is not connected */
	ENOTCONN(Const.ENOTCONN),
	/** Cannot send after transport endpoint shutdown */
	ESHUTDOWN(Const.ESHUTDOWN),
	/** Too many references: cannot splice */
	ETOOMANYREFS(Const.ETOOMANYREFS),
	/** Connection timed out */
	ETIMEDOUT(Const.ETIMEDOUT),
	/** Connection refused */
	ECONNREFUSED(Const.ECONNREFUSED),
	/** Host is down */
	EHOSTDOWN(Const.EHOSTDOWN),
	/** No route to host */
	EHOSTUNREACH(Const.EHOSTUNREACH),
	/** Operation already in progress */
	EALREADY(Const.EALREADY),
	/** Operation now in progress */
	EINPROGRESS(Const.EINPROGRESS),
	/** Stale NFS file handle */
	ESTALE(Const.ESTALE),
	/** Structure needs cleaning */
	@CType(os = linux)
	EUCLEAN(Const.EUCLEAN),
	/** Not a XENIX named type file */
	@CType(os = linux)
	ENOTNAM(Const.ENOTNAM),
	/** No XENIX semaphores available */
	@CType(os = linux)
	ENAVAIL(Const.ENAVAIL),
	/** Is a named type file */
	@CType(os = linux)
	EISNAM(Const.EISNAM),
	/** Remote I/O error */
	@CType(os = linux)
	EREMOTEIO(Const.EREMOTEIO),
	/** Quota exceeded */
	EDQUOT(Const.EDQUOT),
	/** No medium found */
	@CType(os = linux)
	ENOMEDIUM(Const.ENOMEDIUM),
	/** Wrong medium type */
	@CType(os = linux)
	EMEDIUMTYPE(Const.EMEDIUMTYPE),
	/** Operation Canceled */
	ECANCELED(Const.ECANCELED),
	/** Required key not available */
	@CType(os = linux)
	ENOKEY(Const.ENOKEY),
	/** Key has expired */
	@CType(os = linux)
	EKEYEXPIRED(Const.EKEYEXPIRED),
	/** Key has been revoked */
	@CType(os = linux)
	EKEYREVOKED(Const.EKEYREVOKED),
	/** Key was rejected by service */
	@CType(os = linux)
	EKEYREJECTED(Const.EKEYREJECTED),
	/** For robust mutexes */
	/** Owner died */
	EOWNERDEAD(Const.EOWNERDEAD),
	/** State not recoverable */
	ENOTRECOVERABLE(Const.ENOTRECOVERABLE),
	/** Operation not possible due to RF-kill */
	@CType(os = linux)
	ERFKILL(Const.ERFKILL),
	/** Memory page has hardware error */
	@CType(os = linux)
	EHWPOISON(Const.EHWPOISON),

	// Mac only

	/** Authentication error */
	@CType(os = mac)
	EAUTH(Const.EAUTH),
	/** RPC struct is bad */
	@CType(os = mac)
	EBADRPC(Const.EBADRPC),
	/** Inappropriate file type or format */
	@CType(os = mac)
	EFTYPE(Const.EFTYPE),
	/** Need authenticator */
	@CType(os = mac)
	ENEEDAUTH(Const.ENEEDAUTH),
	/** Attribute not found */
	@CType(os = mac)
	ENOATTR(Const.ENOATTR),
	/** Too many processes */
	@CType(os = mac)
	EPROCLIM(Const.EPROCLIM),
	/** Bad procedure for program */
	@CType(os = mac)
	EPROCUNAVAIL(Const.EPROCUNAVAIL),
	/** Program version wrong */
	@CType(os = mac)
	EPROGMISMATCH(Const.EPROGMISMATCH),
	/** RPC prog. not avail */
	@CType(os = mac)
	EPROGUNAVAIL(Const.EPROGUNAVAIL),
	/** RPC version wrong */
	@CType(os = mac)
	ERPCMISMATCH(Const.ERPCMISMATCH);

	private static final Xcoder.Type<CErrNo> xcoder = Xcoder.type(CErrNo.class, t -> t.code);
	public final int code;

	/**
	 * Returns an immutable set of error codes, ignoring undefined codes.
	 */
	public static Set<Integer> codes(int... errorCodes) {
		return Streams.ints(errorCodes).filter(c -> c != Const.UNDEFINED).boxed().toSet();
	}

	/**
	 * Returns an immutable set of error codes, ignoring undefined codes.
	 */
	public static Set<Integer> codes(CErrNo... errors) {
		return Streams.of(errors).filter(CErrNo::defined).map(e -> e.code).toSet();
	}

	/**
	 * Find from exception error code.
	 */
	public static CErrNo from(CException e) {
		return from(e.code);
	}

	/**
	 * Find from exception error code.
	 */
	public static CErrNo from(LastErrorException e) {
		return from(e.errNo);
	}

	/**
	 * Returns a matching type, or undefined type if no match.
	 */
	public static CErrNo from(int code) {
		return Basics.def(xcoder.decode(code), UNDEFINED);
	}

	/**
	 * Returns a value with type and code. Type will be the undefined type if no code matches.
	 */
	public static TypeValue<CErrNo> value(int code) {
		return TypeValue.of(code, from(code), null);
	}

	private CErrNo(int code) {
		this.code = code;
	}

	/**
	 * Create an exception from the code.
	 */
	public LastErrorException lastError() {
		return lastError(name());
	}

	/**
	 * Create an exception from the code and message.
	 */
	public LastErrorException lastError(String message, Object... args) {
		return LastErrorException.full(code, message, args);
	}

	/**
	 * Create a CException from the code.
	 */
	public CException cerror() {
		return CException.of(code, prefix());
	}

	/**
	 * Create a CException from the code and message.
	 */
	public CException cerror(String message, Object... args) {
		return CException.of(code, prefix() + " " + Strings.format(message, args));
	}

	public boolean defined() {
		return code != Const.UNDEFINED;
	}

	// support

	private String prefix() {
		return "[" + name() + ":" + code + "]";
	}

	private static class Const {
		public static final int UNDEFINED = -1;
		public static final int EAGAIN;
		public static final int EWOULDBLOCK;
		public static final int EDEADLK; // (EDEADLOCK)
		public static final int ENAMETOOLONG;
		public static final int ENOLCK;
		public static final int ENOSYS;
		public static final int ENOTEMPTY;
		public static final int ELOOP;
		public static final int ENOMSG;
		public static final int EIDRM;
		public static final int ECHRNG;
		public static final int EL2NSYNC;
		public static final int EL3HLT;
		public static final int EL3RST;
		public static final int ELNRNG;
		public static final int EUNATCH;
		public static final int ENOCSI;
		public static final int EL2HLT;
		public static final int EBADE;
		public static final int EBADR;
		public static final int EXFULL;
		public static final int ENOANO;
		public static final int EBADRQC;
		public static final int EBADSLT;
		public static final int EBFONT;
		public static final int ENOSTR;
		public static final int ENODATA;
		public static final int ETIME;
		public static final int ENOSR;
		public static final int ENONET;
		public static final int ENOPKG;
		public static final int EREMOTE;
		public static final int ENOLINK;
		public static final int EADV;
		public static final int ESRMNT;
		public static final int ECOMM;
		public static final int EPROTO;
		public static final int EMULTIHOP;
		public static final int EDOTDOT;
		public static final int EBADMSG;
		public static final int EOVERFLOW;
		public static final int ENOTUNIQ;
		public static final int EBADFD;
		public static final int EREMCHG;
		public static final int ELIBACC;
		public static final int ELIBBAD;
		public static final int ELIBSCN;
		public static final int ELIBMAX;
		public static final int ELIBEXEC;
		public static final int EILSEQ;
		public static final int ERESTART;
		public static final int ESTRPIPE;
		public static final int EUSERS;
		public static final int ENOTSOCK;
		public static final int EDESTADDRREQ;
		public static final int EMSGSIZE;
		public static final int EPROTOTYPE;
		public static final int ENOPROTOOPT;
		public static final int EPROTONOSUPPORT;
		public static final int ESOCKTNOSUPPORT;
		public static final int EOPNOTSUPP;
		public static final int ENOTSUP;
		public static final int EPFNOSUPPORT;
		public static final int EAFNOSUPPORT;
		public static final int EADDRINUSE;
		public static final int EADDRNOTAVAIL;
		public static final int ENETDOWN;
		public static final int ENETUNREACH;
		public static final int ENETRESET;
		public static final int ECONNABORTED;
		public static final int ECONNRESET;
		public static final int ENOBUFS;
		public static final int EISCONN;
		public static final int ENOTCONN;
		public static final int ESHUTDOWN;
		public static final int ETOOMANYREFS;
		public static final int ETIMEDOUT;
		public static final int ECONNREFUSED;
		public static final int EHOSTDOWN;
		public static final int EHOSTUNREACH;
		public static final int EALREADY;
		public static final int EINPROGRESS;
		public static final int ESTALE;
		public static final int EUCLEAN;
		public static final int ENOTNAM;
		public static final int ENAVAIL;
		public static final int EISNAM;
		public static final int EREMOTEIO;
		public static final int EDQUOT;
		public static final int ENOMEDIUM;
		public static final int EMEDIUMTYPE;
		public static final int ECANCELED;
		public static final int ENOKEY;
		public static final int EKEYEXPIRED;
		public static final int EKEYREVOKED;
		public static final int EKEYREJECTED;
		public static final int EOWNERDEAD;
		public static final int ENOTRECOVERABLE;
		public static final int ERFKILL;
		public static final int EHWPOISON;
		/* Mac only */
		public static final int EAUTH;
		public static final int EBADRPC;
		public static final int EFTYPE;
		public static final int ENEEDAUTH;
		public static final int ENOATTR;
		public static final int EPROCLIM;
		public static final int EPROCUNAVAIL;
		public static final int EPROGMISMATCH;
		public static final int EPROGUNAVAIL;
		public static final int ERPCMISMATCH;

		static {
			if (Os.info().mac) {
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
}
