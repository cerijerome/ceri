package ceri.common.concurrent;

import ceri.common.exception.ExceptionUtil;

public class RuntimeInterruptedException extends RuntimeException {
	private static final long serialVersionUID = 4222882577886865396L;

	public RuntimeInterruptedException(String message) {
		super(message);
	}

	public RuntimeInterruptedException(InterruptedException e) {
		super(ExceptionUtil.message(e), e);
	}

}
