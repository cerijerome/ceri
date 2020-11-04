package ceri.process.nmcli;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestProcess;
import ceri.common.test.TestProcess.TestProcessor;
import ceri.common.test.TestUtil;

public class NmcliBehavior {

	@Test
	public void shouldHaveDefaultProcessor() {
		assertNotNull(Nmcli.of().con);
	}

	@Test
	public void shouldExecuteNmcliConShow() throws IOException {
		TestProcessor p = TestProcess.processor(TestUtil.resource("nmcli-con-show.txt"));
		var output = Nmcli.of(p).con.show();
		p.assertParameters("nmcli", "con", "show");
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
		TestProcessor p = TestProcess.processor(TestUtil.resource("nmcli-con-show-id.txt"));
		var output = Nmcli.of(p).con.show("test");
		p.assertParameters("nmcli", "con", "show", "id", "test");
		ConShowIdResult result = output.parse();
		assertEquals(result.connectionId(), "eth2");
		assertEquals(result.connectionUuid(), "186053d4-9369-4a4e-87b8-d1f9a419f985");
		assertEquals(result.connectionInterfaceName(), "eth2");
		assertEquals(result.connectionType(), "802-3-ethernet");
		assertEquals(result.generalState(), "activated");
		assertTrue(result.generalVpn());
		assertEquals(result.value("connection.stable-id"), "");
		assertFalse(result.isNull());
	}

	@Test
	public void shouldExecuteNmcliConUp() throws IOException {
		TestProcessor p = TestProcess.processor("output");
		var output = Nmcli.of(p).con.up("test");
		p.assertParameters("nmcli", "con", "up", "id", "test");
		assertEquals(output, "output");

		p = TestProcess.processor("output");
		output = Nmcli.of(p).con.up("test", 10);
		p.assertParameters("nmcli", "con", "up", "id", "test", "--wait", "10");
		assertEquals(output, "output");
	}

	@Test
	public void shouldExecuteNmcliConDown() throws IOException {
		TestProcessor p = TestProcess.processor("output");
		var output = Nmcli.of(p).con.down("test");
		p.assertParameters("nmcli", "con", "down", "id", "test");
		assertEquals(output, "output");
	}
}
