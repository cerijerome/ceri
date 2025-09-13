package ceri.x10.cm11a.device;

import java.io.IOException;
import org.junit.Test;
import ceri.x10.command.Command;
import ceri.x10.command.House;
import ceri.x10.command.TestCommandListener;

public class Cm11aBehavior {

	@Test
	public void shouldProvideNullDevice() throws IOException {
		try (var _ = Cm11a.NULL.listeners().enclose(_ -> {})) {
			var cl = TestCommandListener.of();
			try (var _ = Cm11a.NULL.listen(cl)) {
				Cm11a.NULL.command(Command.allUnitsOff(House.J));
				Cm11a.NULL.close();
			}
		}
	}
}
