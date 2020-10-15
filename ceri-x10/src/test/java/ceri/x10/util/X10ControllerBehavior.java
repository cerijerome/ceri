package ceri.x10.util;

import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import java.io.IOException;
import org.junit.Test;
import ceri.common.util.Enclosed;
import ceri.x10.command.Command;
import ceri.x10.command.CommandListener;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

public class X10ControllerBehavior {

	@Test
	public void shouldSupportNoCommandByDefault() {
		assertThat(X10Controller.NULL.supports(Command.on(House.J, Unit._8)), is(false));
		assertThrown(() -> X10Controller.verifySupported(null, Command.on(House.J, Unit._8)));
	}

	@Test
	public void should() throws IOException {
		X10Controller x10 = new X10Controller() {
			@Override
			public boolean supports(Command command) {
				return command.type() == FunctionType.off;
			}
		};
		x10.command(Command.off(House.G, Unit._12));
		assertThrown(() -> x10.command(Command.on(House.G, Unit._12)));
	}

	@Test
	public void shouldThrowExceptionForUnsupportCommand() {
		assertThrown(() -> X10Controller.NULL.command(Command.dim(House.N, 10, Unit._2)));
	}

	@Test
	public void shouldNotNotifyListenersByDefault() {
		try (Enclosed<CommandListener> enclosed = X10Controller.NULL.listen(null)) {}
	}

}
