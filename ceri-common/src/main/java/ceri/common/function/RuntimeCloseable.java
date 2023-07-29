package ceri.common.function;

public interface RuntimeCloseable extends ExceptionCloseable<RuntimeException> {
	static RuntimeCloseable NULL = () -> {}; 
}
