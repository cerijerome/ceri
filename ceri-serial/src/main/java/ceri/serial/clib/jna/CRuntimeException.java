package ceri.serial.clib.jna;

public class CRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -1L;
	public final int code;

	public CRuntimeException(CException e) {
		super(e.getMessage(), e);
		code = e.code;
	}

}
