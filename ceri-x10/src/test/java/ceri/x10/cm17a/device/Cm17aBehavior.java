package ceri.x10.cm17a.device;

import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.Test;
import ceri.x10.command.Command;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

public class Cm17aBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		assertTrue(Cm17a.NULL.supports(Command.on(House.I, Unit._4)));
		Cm17a.NULL.command(Command.on(House.I, Unit._4));
		try (var _ = Cm17a.NULL.listen(new Command.Listener() {})) {}
		try (var _ = Cm17a.NULL.listeners().enclose(_ -> {})) {}
		Cm17a.NULL.close();
	}

}
