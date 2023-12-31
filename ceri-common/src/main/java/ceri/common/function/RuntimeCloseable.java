package ceri.common.function;

public interface RuntimeCloseable extends ExceptionCloseable<RuntimeException> {
	static RuntimeCloseable NULL = () -> {};
	
	static RuntimeCloseable from(ExceptionCloseable<RuntimeException> closeable) {
		return closeable::close;
	}
}
