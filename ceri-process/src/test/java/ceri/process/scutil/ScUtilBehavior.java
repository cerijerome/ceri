package ceri.process.scutil;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNotNull;
import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import ceri.common.process.Parameters;
import ceri.common.test.TestProcess;
import ceri.common.test.TestProcess.TestProcessor;
import ceri.common.test.TestUtil;

public class ScUtilBehavior {

	@Test
	public void shouldHaveDefaultProcessor() {
		assertNotNull(ScUtil.of().nc);
	}

	@Test
	public void shouldExecuteNcList() throws IOException {
		TestProcessor p = TestProcess.processor(TestUtil.resource("list-output.txt"));
		var result = ScUtil.of(p).nc.list();
		p.exec.assertAuto(Parameters.of("scutil", "--nc", "list"));
		assertIterable(result.parse(),
			NcListItem.builder().enabled(false).state("Disconnected")
				.passwordHash("XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX").protocol("PPP")
				.device("FT245R USB FIFO").name("FT245R USB FIFO").type("Modem").build(),
			NcListItem.builder().enabled(true).state("Connecting")
				.passwordHash("00000000-0000-0000-0000-000000000000").protocol("I2C")
				.device("FT232R USB UART").name("FT232R USB UART").type("Device").build());
	}

	@Test
	public void shouldExecuteNcStatus() throws IOException {
		TestProcessor p = TestProcess.processor(TestUtil.resource("status-output.txt"));
		ScUtil scUtil = ScUtil.of(p);
		var result = scUtil.nc.status("test");
		p.assertParameters("scutil", "--nc", "status", "test");
		NcStatus ns = result.parse();
		assertEquals(ns.state, NcServiceState.connected);
		assertEquals(ns.data.find("Extended Status.PPP.DeviceLastCause").parse().toInt(), 99);
		assertEquals(ns.data.find("Extended Status.PPP.LastCause").parse().toInt(), 90);
		assertEquals(ns.data.find("Extended Status.PPP.Status").parse().toInt(), 1);
		assertEquals(ns.data.find("Extended Status.Status").parse().toInt(), 0);
	}

	@Test
	public void shouldExecuteNcShow() throws IOException {
		TestProcessor p = TestProcess.processor(TestUtil.resource("show-output.txt"));
		var result = ScUtil.of(p).nc.show("test");
		p.assertParameters("scutil", "--nc", "show", "test");
		NcShow ns = result.parse();
		assertEquals(ns.item,
			NcListItem.builder().enabled(true).state("Connecting")
				.passwordHash("00000000-0000-0000-0000-000000000000").protocol("I2C")
				.device("FT232R USB UART").name("FT232R USB UART").type("Device").build());
		assertEquals(ns.data.find("Extended Status.PPP.DeviceLastCause").parse().toInt(), 99);
		assertEquals(ns.data.find("Extended Status.PPP.LastCause").parse().toInt(), 90);
		assertEquals(ns.data.find("Extended Status.PPP.Status").parse().toInt(), 1);
		assertEquals(ns.data.find("Extended Status.Status").parse().toInt(), 0);
	}

	@Test
	public void shouldExecuteNcStatistics() throws IOException {
		TestProcessor p = TestProcess.processor(TestUtil.resource("statistics-output.txt"));
		var result = ScUtil.of(p).nc.statistics("test");
		p.assertParameters("scutil", "--nc", "statistics", "test");
		NcStatistics ns = result.parse();
		assertEquals(ns, NcStatistics.builder().add(Map.of("BytesIn", 20337, "BytesOut", 16517,
			"ErrorsIn", 10, "PacketsIn", 77, "PacketsOut", 118)).build());
	}

	@Test
	public void shouldExecuteNcStart() throws IOException {
		TestProcessor p = TestProcess.processor("output");
		var result = ScUtil.of(p).nc.start("test", "user", "pwd", "secret");
		p.assertParameters("scutil", "--nc", "start", "test", "--user", "user", "--password", "pwd",
			"--secret", "secret");
		assertEquals(result, "output");

		p = TestProcess.processor("output");
		result = ScUtil.of(p).nc.start("test", null, null, null);
		p.assertParameters("scutil", "--nc", "start", "test");
		assertEquals(result, "output");
	}

	@Test
	public void shouldExecuteNcStop() throws IOException {
		TestProcessor p = TestProcess.processor("output");
		var result = ScUtil.of(p).nc.stop("test");
		p.assertParameters("scutil", "--nc", "stop", "test");
		assertEquals(result, "output");
	}

}
