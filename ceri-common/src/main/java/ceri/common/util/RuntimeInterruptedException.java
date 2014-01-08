package ceri.common.util;

public class RuntimeInterruptedException extends RuntimeException {
	private static final long serialVersionUID = 4222882577886865396L;

	public RuntimeInterruptedException(InterruptedException e) {
		super(e);
	}

}
