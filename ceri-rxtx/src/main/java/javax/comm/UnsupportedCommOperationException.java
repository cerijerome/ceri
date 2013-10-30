package javax.comm;

public class UnsupportedCommOperationException extends Exception {
	private static final long serialVersionUID = -7476232496591452602L;

	UnsupportedCommOperationException(gnu.io.UnsupportedCommOperationException e) {
		super(e);
	}
}
