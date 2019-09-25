package ceri.common.function;

public class WrapperException extends RuntimeException {
	private static final long serialVersionUID = -7884771697875904804L;
	final FunctionWrapper<?> wrapper;

	WrapperException(FunctionWrapper<?> wrapper, Exception e) {
		super(e);
		this.wrapper = wrapper;
	}

}
