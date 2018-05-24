package javax.comm;

public class PortInUseException extends Exception {
	private static final long serialVersionUID = 1872331673223268172L;

	public PortInUseException(purejavacomm.PortInUseException e) {
		super(e);
	}
}
