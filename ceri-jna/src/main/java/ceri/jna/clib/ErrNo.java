package ceri.jna.clib;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.sun.jna.LastErrorException;
import ceri.common.data.TypeValue;
import ceri.common.data.Xcoder;
import ceri.common.text.Strings;
import ceri.common.util.Basics;
import ceri.jna.clib.jna.CErrNo;
import ceri.jna.clib.jna.CException;

/**
 * Enumerated error codes.
 */
public enum ErrNo {
	UNDEFINED(CErrNo.UNDEFINED),
	/* Common values */
	EPERM(CErrNo.EPERM),
	ENOENT(CErrNo.ENOENT),
	ESRCH(CErrNo.ESRCH),
	EINTR(CErrNo.EINTR),
	EIO(CErrNo.EIO),
	ENXIO(CErrNo.ENXIO),
	E2BIG(CErrNo.E2BIG),
	ENOEXEC(CErrNo.ENOEXEC),
	EBADF(CErrNo.EBADF),
	ECHILD(CErrNo.ECHILD),
	EAGAIN(CErrNo.EAGAIN),
	EWOULDBLOCK(CErrNo.EWOULDBLOCK),
	ENOMEM(CErrNo.ENOMEM),
	EACCES(CErrNo.EACCES),
	EFAULT(CErrNo.EFAULT),
	ENOTBLK(CErrNo.ENOTBLK),
	EBUSY(CErrNo.EBUSY),
	EEXIST(CErrNo.EEXIST),
	EXDEV(CErrNo.EXDEV),
	ENODEV(CErrNo.ENODEV),
	ENOTDIR(CErrNo.ENOTDIR),
	EISDIR(CErrNo.EISDIR),
	EINVAL(CErrNo.EINVAL),
	ENFILE(CErrNo.ENFILE),
	EMFILE(CErrNo.EMFILE),
	ENOTTY(CErrNo.ENOTTY),
	ETXTBSY(CErrNo.ETXTBSY),
	EFBIG(CErrNo.EFBIG),
	ENOSPC(CErrNo.ENOSPC),
	ESPIPE(CErrNo.ESPIPE),
	EROFS(CErrNo.EROFS),
	EMLINK(CErrNo.EMLINK),
	EPIPE(CErrNo.EPIPE),
	EDOM(CErrNo.EDOM),
	ERANGE(CErrNo.ERANGE),
	/* OS-specific values */
	EDEADLK(CErrNo.EDEADLK),
	ENAMETOOLONG(CErrNo.ENAMETOOLONG),
	ENOLCK(CErrNo.ENOLCK),
	ENOSYS(CErrNo.ENOSYS),
	ENOTEMPTY(CErrNo.ENOTEMPTY),
	ELOOP(CErrNo.ELOOP),
	ENOMSG(CErrNo.ENOMSG),
	EIDRM(CErrNo.EIDRM),
	ECHRNG(CErrNo.ECHRNG),
	EL2NSYNC(CErrNo.EL2NSYNC),
	EL3HLT(CErrNo.EL3HLT),
	EL3RST(CErrNo.EL3RST),
	ELNRNG(CErrNo.ELNRNG),
	EUNATCH(CErrNo.EUNATCH),
	ENOCSI(CErrNo.ENOCSI),
	EL2HLT(CErrNo.EL2HLT),
	EBADE(CErrNo.EBADE),
	EBADR(CErrNo.EBADR),
	EXFULL(CErrNo.EXFULL),
	ENOANO(CErrNo.ENOANO),
	EBADRQC(CErrNo.EBADRQC),
	EBADSLT(CErrNo.EBADSLT),
	EBFONT(CErrNo.EBFONT),
	ENOSTR(CErrNo.ENOSTR),
	ENODATA(CErrNo.ENODATA),
	ETIME(CErrNo.ETIME),
	ENOSR(CErrNo.ENOSR),
	ENONET(CErrNo.ENONET),
	ENOPKG(CErrNo.ENOPKG),
	EREMOTE(CErrNo.EREMOTE),
	ENOLINK(CErrNo.ENOLINK),
	EADV(CErrNo.EADV),
	ESRMNT(CErrNo.ESRMNT),
	ECOMM(CErrNo.ECOMM),
	EPROTO(CErrNo.EPROTO),
	EMULTIHOP(CErrNo.EMULTIHOP),
	EDOTDOT(CErrNo.EDOTDOT),
	EBADMSG(CErrNo.EBADMSG),
	EOVERFLOW(CErrNo.EOVERFLOW),
	ENOTUNIQ(CErrNo.ENOTUNIQ),
	EBADFD(CErrNo.EBADFD),
	EREMCHG(CErrNo.EREMCHG),
	ELIBACC(CErrNo.ELIBACC),
	ELIBBAD(CErrNo.ELIBBAD),
	ELIBSCN(CErrNo.ELIBSCN),
	ELIBMAX(CErrNo.ELIBMAX),
	ELIBEXEC(CErrNo.ELIBEXEC),
	EILSEQ(CErrNo.EILSEQ),
	ERESTART(CErrNo.ERESTART),
	ESTRPIPE(CErrNo.ESTRPIPE),
	EUSERS(CErrNo.EUSERS),
	ENOTSOCK(CErrNo.ENOTSOCK),
	EDESTADDRREQ(CErrNo.EDESTADDRREQ),
	EMSGSIZE(CErrNo.EMSGSIZE),
	EPROTOTYPE(CErrNo.EPROTOTYPE),
	ENOPROTOOPT(CErrNo.ENOPROTOOPT),
	EPROTONOSUPPORT(CErrNo.EPROTONOSUPPORT),
	ESOCKTNOSUPPORT(CErrNo.ESOCKTNOSUPPORT),
	EOPNOTSUPP(CErrNo.EOPNOTSUPP),
	ENOTSUP(CErrNo.ENOTSUP),
	EPFNOSUPPORT(CErrNo.EPFNOSUPPORT),
	EAFNOSUPPORT(CErrNo.EAFNOSUPPORT),
	EADDRINUSE(CErrNo.EADDRINUSE),
	EADDRNOTAVAIL(CErrNo.EADDRNOTAVAIL),
	ENETDOWN(CErrNo.ENETDOWN),
	ENETUNREACH(CErrNo.ENETUNREACH),
	ENETRESET(CErrNo.ENETRESET),
	ECONNABORTED(CErrNo.ECONNABORTED),
	ECONNRESET(CErrNo.ECONNRESET),
	ENOBUFS(CErrNo.ENOBUFS),
	EISCONN(CErrNo.EISCONN),
	ENOTCONN(CErrNo.ENOTCONN),
	ESHUTDOWN(CErrNo.ESHUTDOWN),
	ETOOMANYREFS(CErrNo.ETOOMANYREFS),
	ETIMEDOUT(CErrNo.ETIMEDOUT),
	ECONNREFUSED(CErrNo.ECONNREFUSED),
	EHOSTDOWN(CErrNo.EHOSTDOWN),
	EHOSTUNREACH(CErrNo.EHOSTUNREACH),
	EALREADY(CErrNo.EALREADY),
	EINPROGRESS(CErrNo.EINPROGRESS),
	ESTALE(CErrNo.ESTALE),
	EUCLEAN(CErrNo.EUCLEAN),
	ENOTNAM(CErrNo.ENOTNAM),
	ENAVAIL(CErrNo.ENAVAIL),
	EISNAM(CErrNo.EISNAM),
	EREMOTEIO(CErrNo.EREMOTEIO),
	EDQUOT(CErrNo.EDQUOT),
	ENOMEDIUM(CErrNo.ENOMEDIUM),
	EMEDIUMTYPE(CErrNo.EMEDIUMTYPE),
	ECANCELED(CErrNo.ECANCELED),
	ENOKEY(CErrNo.ENOKEY),
	EKEYEXPIRED(CErrNo.EKEYEXPIRED),
	EKEYREVOKED(CErrNo.EKEYREVOKED),
	EKEYREJECTED(CErrNo.EKEYREJECTED),
	EOWNERDEAD(CErrNo.EOWNERDEAD),
	ENOTRECOVERABLE(CErrNo.ENOTRECOVERABLE),
	ERFKILL(CErrNo.ERFKILL),
	EHWPOISON(CErrNo.EHWPOISON),
	/* Mac only */
	EAUTH(CErrNo.EAUTH),
	EBADRPC(CErrNo.EBADRPC),
	EFTYPE(CErrNo.EFTYPE),
	ENEEDAUTH(CErrNo.ENEEDAUTH),
	ENOATTR(CErrNo.ENOATTR),
	EPROCLIM(CErrNo.EPROCLIM),
	EPROCUNAVAIL(CErrNo.EPROCUNAVAIL),
	EPROGMISMATCH(CErrNo.EPROGMISMATCH),
	EPROGUNAVAIL(CErrNo.EPROGUNAVAIL),
	ERPCMISMATCH(CErrNo.ERPCMISMATCH);

	public static final Xcoder.Type<ErrNo> xcoder = Xcoder.type(ErrNo.class, t -> t.code);
	public final int code;

	/**
	 * Returns an immutable set of error codes, ignoring undefined codes.
	 */
	public static Set<Integer> codes(ErrNo... errors) {
		return Stream.of(errors).filter(ErrNo::defined).map(e -> e.code)
			.collect(Collectors.toUnmodifiableSet());
	}

	public static ErrNo from(CException e) {
		return from(e.code);
	}

	public static ErrNo from(LastErrorException e) {
		return from(e.getErrorCode());
	}

	/**
	 * Returns a value with type and code. Type will be the undefined type if no code matches.
	 */
	public static TypeValue<ErrNo> value(int code) {
		return TypeValue.of(code, from(code), null);
	}

	/**
	 * Returns a matching type, or undefined type if no match.
	 */
	public static ErrNo from(int code) {
		return Basics.def(xcoder.decode(code), UNDEFINED);
	}

	private ErrNo(int code) {
		this.code = code;
	}

	/**
	 * Create a LastErrorException from the code.
	 */
	public LastErrorException lastError() {
		return lastError(name());
	}

	/**
	 * Create a LastErrorException from the code and message.
	 */
	public LastErrorException lastError(String message, Object... args) {
		return new LastErrorException("[" + code + "] " + Strings.format(message, args));
	}

	/**
	 * Create a CException from the code.
	 */
	public CException error() {
		return CException.of(code, prefix());
	}

	/**
	 * Create a CException from the code and message.
	 */
	public CException error(String message, Object... args) {
		return CException.of(code, prefix() + " " + Strings.format(message, args));
	}

	public boolean defined() {
		return code != CErrNo.UNDEFINED;
	}

	private String prefix() {
		return "[" + name() + ":" + code + "]";
	}
}
