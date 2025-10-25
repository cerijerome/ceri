package ceri.common.io;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;

public class ReplaceableBehavior {

	@Test
	public void shouldCreateFieldWithDefaultName() throws IOException {
		try (var field = Replaceable.field()) {
			Assert.thrown(() -> field.acceptValid(_ -> {}));
		}
	}
}
