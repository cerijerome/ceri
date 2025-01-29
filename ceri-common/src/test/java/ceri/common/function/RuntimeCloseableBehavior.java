package ceri.common.function;

import org.junit.Test;

public class RuntimeCloseableBehavior {

	@Test
	public void shouldConvert() {
		try (ExceptionCloseable<RuntimeException> ec = () -> {};
			var _ = RuntimeCloseable.from(ec)) {}
	}

}
