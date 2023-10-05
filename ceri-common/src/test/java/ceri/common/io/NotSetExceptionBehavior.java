package ceri.common.io;

import org.junit.Test;

public class NotSetExceptionBehavior {

	@Test
	public void shouldCreateWithDefaultName() {
		var e = new NotSetException();
		e.getMessage();
	}

}
