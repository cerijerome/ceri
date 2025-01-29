package ceri.common.io;

import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;

public class ReplaceableBehavior {

	@Test
	public void shouldCreateFieldWithDefaultName() throws IOException {
		try (var field = Replaceable.field()) {
			assertThrown(() -> field.acceptValid(_ -> {}));
		}
	}

}
