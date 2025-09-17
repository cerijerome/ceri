package ceri.common.concurrent;

import ceri.common.exception.Exceptions;

@SuppressWarnings("serial")
public class RuntimeInterruptedException extends RuntimeException {

	public RuntimeInterruptedException(String message) {
		super(message);
	}

	public RuntimeInterruptedException(InterruptedException e) {
		super(Exceptions.message(e), e);
	}
}
