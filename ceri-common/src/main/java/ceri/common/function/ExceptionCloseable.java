package ceri.common.function;

public interface ExceptionCloseable<E extends Exception> extends AutoCloseable {

	@Override
	void close() throws E;

}
