package ceri.common.concurrent;

public class RuntimeInterruptedException extends RuntimeException {
	private static final long serialVersionUID = 4222882577886865396L;

	public RuntimeInterruptedException(String message) {
		super(message);
	}

	public RuntimeInterruptedException(InterruptedException e) {
		super(e);
	}

}
