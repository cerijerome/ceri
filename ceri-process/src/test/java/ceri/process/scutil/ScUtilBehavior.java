package ceri.process.scutil;

import static ceri.common.test.TestUtil.assertIterable;
import static ceri.process.util.ProcessTestUtil.assertParameters;
import static ceri.process.util.ProcessTestUtil.mockProcessor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import ceri.common.process.Processor;
import ceri.common.test.TestUtil;

public class ScUtilBehavior {

	@Test
	public void shouldHaveDefaultProcessor() {
		assertNotNull(ScUtil.of().nc);
	}

	@Test
	public void shouldExecuteNcList() throws IOException {
		Processor p = mockProcessor(TestUtil.resource("list-output.txt"));
		var result = ScUtil.of(p).nc.list();
		assertParameters(p, "scutil", "--nc", "list");
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
		Processor p = mockProcessor(TestUtil.resource("status-output.txt"));
		ScUtil scUtil = ScUtil.of(p);
		var result = scUtil.nc.status("test");
		assertParameters(p, "scutil", "--nc", "status", "test");
		NcStatus ns = result.parse();
		assertThat(ns.state, is(NcServiceState.connected));
		assertThat(ns.data.find("Extended Status.PPP.DeviceLastCause").asInt(), is(99));
		assertThat(ns.data.find("Extended Status.PPP.LastCause").asInt(), is(90));
		assertThat(ns.data.find("Extended Status.PPP.Status").asInt(), is(1));
		assertThat(ns.data.find("Extended Status.Status").asInt(), is(0));
	}

	@Test
	public void shouldExecuteNcShow() throws IOException {
		Processor p = mockProcessor(TestUtil.resource("show-output.txt"));
		var result = ScUtil.of(p).nc.show("test");
		assertParameters(p, "scutil", "--nc", "show", "test");
		NcShow ns = result.parse();
		assertThat(ns.item,
			is(NcListItem.builder().enabled(true).state("Connecting")
				.passwordHash("00000000-0000-0000-0000-000000000000").protocol("I2C")
				.device("FT232R USB UART").name("FT232R USB UART").type("Device").build()));
		assertThat(ns.data.find("Extended Status.PPP.DeviceLastCause").asInt(), is(99));
		assertThat(ns.data.find("Extended Status.PPP.LastCause").asInt(), is(90));
		assertThat(ns.data.find("Extended Status.PPP.Status").asInt(), is(1));
		assertThat(ns.data.find("Extended Status.Status").asInt(), is(0));
	}

	@Test
	public void shouldExecuteNcStatistics() throws IOException {
		Processor p = mockProcessor(TestUtil.resource("statistics-output.txt"));
		var result = ScUtil.of(p).nc.statistics("test");
		assertParameters(p, "scutil", "--nc", "statistics", "test");
		NcStatistics ns = result.parse();
		assertThat(ns, is(NcStatistics.builder().add(Map.of("BytesIn", 20337, "BytesOut", 16517,
			"ErrorsIn", 10, "PacketsIn", 77, "PacketsOut", 118)).build()));
	}

	@Test
	public void shouldExecuteNcStart() throws IOException {
		Processor p = mockProcessor("output");
		var result = ScUtil.of(p).nc.start("test", "user", "pwd", "secret");
		assertParameters(p, "scutil", "--nc", "start", "test", "--user", "user", "--password",
			"pwd", "--secret", "secret");
		assertThat(result, is("output"));

		p = mockProcessor("output");
		result = ScUtil.of(p).nc.start("test", null, null, null);
		assertParameters(p, "scutil", "--nc", "start", "test");
		assertThat(result, is("output"));
	}

	@Test
	public void shouldExecuteNcStop() throws IOException {
		Processor p = mockProcessor("output");
		var result = ScUtil.of(p).nc.stop("test");
		assertParameters(p, "scutil", "--nc", "stop", "test");
		assertThat(result, is("output"));
	}

}