package ceri.common.concurrent;

import ceri.common.except.Exceptions;

@SuppressWarnings("serial")
public class RuntimeInterruptedException extends RuntimeException {

	public RuntimeInterruptedException(String message) {
		super(message);
	}

	public RuntimeInterruptedException(Exception e) {
		super(Exceptions.message(e), e);
	}
}
