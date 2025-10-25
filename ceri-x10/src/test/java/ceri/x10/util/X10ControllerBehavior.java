package ceri.x10.util;

import static ceri.common.test.Assert.assertFalse;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;
import ceri.x10.command.Command;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

public class X10ControllerBehavior {

	@Test
	public void shouldEnumerateType() {
		TestUtil.exerciseEnum(X10Controller.Type.class);
	}
	
	@Test
	public void shouldSupportNoCommandByDefault() {
		assertFalse(X10Controller.NULL.supports(Command.on(House.J, Unit._8)));
		Assert.thrown(() -> X10Controller.verifySupported(null, Command.on(House.J, Unit._8)));
	}

	@Test
	public void shouldFailForUnsupportedCommand() throws IOException {
		var x10 = new X10Controller() {
			@Override
			public boolean supports(Command command) {
				return command.type() == FunctionType.off;
			}
		};
		x10.command(Command.off(House.G, Unit._12));
		Assert.thrown(() -> x10.command(Command.on(House.G, Unit._12)));
	}

	@Test
	public void shouldThrowExceptionForUnsupportCommand() {
		Assert.thrown(() -> X10Controller.NULL.command(Command.dim(House.N, 10, Unit._2)));
	}

	@Test
	public void shouldNotNotifyListenersByDefault() {
		try (var _ = X10Controller.NULL.listen(null)) {}
	}
}
