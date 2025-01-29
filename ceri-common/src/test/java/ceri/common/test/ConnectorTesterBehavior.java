package ceri.common.test;

import static ceri.common.test.AssertUtil.assertFind;
import java.io.IOException;
import org.junit.Test;

public class ConnectorTesterBehavior {

	@Test
	public void shouldExecuteCommands() throws IOException {
		try (var _ = ManualTester.fastMode(); SystemIoCaptor sys = SystemIoCaptor.of();
			var c = TestConnector.of()) {
			sys.in.print("O\nC\nOs\nz\nZ\n!\n");
			ConnectorTester.test(c);
			assertFind(sys.out, "(?s)=> broken.*=> fixed");
		}
	}

}
