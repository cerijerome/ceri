package ceri.process.nmcli;

import static ceri.common.test.TestUtil.assertIterable;
import static ceri.process.util.ProcessTestUtil.assertParameters;
import static ceri.process.util.ProcessTestUtil.mockProcessor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Test;
import ceri.common.process.Processor;
import ceri.common.test.TestUtil;

public class NmcliBehavior {

	@Test
	public void shouldHaveDefaultProcessor() {
		assertNotNull(Nmcli.of().con);
	}

	@Test
	public void shouldExecuteNmcliConShow() throws IOException {
		Processor p = mockProcessor(TestUtil.resource("nmcli-con-show.txt"));
		var output = Nmcli.of(p).con.show();
		assertParameters(p, "nmcli", "con", "show");
		var results = output.parse();
		assertIterable(results,
			ConShowItem.of("eth1", "01fa0bf4-b6bd-484f-a9a3-2b10ff701dcd", "ethernet", "eth1"),
			ConShowItem.of("eth0", "2e9f0cdd-ea2f-4b63-b146-3b9a897c9e45", "ethernet", "eth0"),
			ConShowItem.of("eth2", "186053d4-9369-4a4e-87b8-d1f9a419f985", "ethernet", "eth2"));
		for (var item : results)
			assertFalse(item.isNull());
	}

	@Test
	public void shouldExecuteNmcliConShowId() throws IOException {
		Processor p = mockProcessor(TestUtil.resource("nmcli-con-show-id.txt"));
		var output = Nmcli.of(p).con.show("test");
		assertParameters(p, "nmcli", "con", "show", "id", "test");
		ConShowIdResult result = output.parse();
		assertThat(result.connectionId(), is("eth2"));
		assertThat(result.connectionUuid(), is("186053d4-9369-4a4e-87b8-d1f9a419f985"));
		assertThat(result.connectionInterfaceName(), is("eth2"));
		assertThat(result.connectionType(), is("802-3-ethernet"));
		assertThat(result.generalState(), is("activated"));
		assertThat(result.generalVpn(), is(true));
		assertThat(result.value("connection.stable-id"), is(""));
		assertFalse(result.isNull());
	}

	@Test
	public void shouldExecuteNmcliConUp() throws IOException {
		Processor p = mockProcessor("output");
		var output = Nmcli.of(p).con.up("test");
		assertParameters(p, "nmcli", "con", "up", "id", "test");
		assertThat(output, is("output"));

		p = mockProcessor("output");
		output = Nmcli.of(p).con.up("test", 10);
		assertParameters(p, "nmcli", "con", "up", "id", "test", "--wait", "10");
		assertThat(output, is("output"));
	}

	@Test
	public void shouldExecuteNmcliConDown() throws IOException {
		Processor p = mockProcessor("output");
		var output = Nmcli.of(p).con.down("test");
		assertParameters(p, "nmcli", "con", "down", "id", "test");
		assertThat(output, is("output"));
	}
}
