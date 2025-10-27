package ceri.process.nmcli;

import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestProcess;
import ceri.common.test.TestUtil;

public class NmcliBehavior {

	@Test
	public void shouldHaveDefaultProcessor() {
		Assert.notNull(Nmcli.of().con);
	}

	@Test
	public void shouldNotBreachConItemEqualsContract() {
		var t = new Nmcli.Con.Item("name", "uuid", "type", "device");
		var eq0 = new Nmcli.Con.Item("name", "uuid", "type", "device");
		var ne0 = Nmcli.Con.Item.NULL;
		var ne1 = new Nmcli.Con.Item("Name", "uuid", "type", "device");
		var ne2 = new Nmcli.Con.Item("name", "", "type", "device");
		var ne3 = new Nmcli.Con.Item("name", "uuid", "types", "device");
		var ne4 = new Nmcli.Con.Item("name", "uuid", "type", "dev");
		TestUtil.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldReturnNoConItemForHeaderOutputOnly() {
		Assert.ordered(Nmcli.Con.Item.fromOutput("A  B  C"));
	}

	@Test
	public void shouldDetermineIfConItemIsNull() {
		Assert.equal(Nmcli.Con.Item.NULL.isNull(), true);
		Assert.equal(new Nmcli.Con.Item("", null, null, null).isNull(), false);
		Assert.equal(new Nmcli.Con.Item(null, "", null, null).isNull(), false);
		Assert.equal(new Nmcli.Con.Item(null, null, "", null).isNull(), false);
		Assert.equal(new Nmcli.Con.Item(null, null, null, "").isNull(), false);
	}

	@Test
	public void shouldNotBreachConIdResultEqualsContract() {
		var t = new Nmcli.Con.IdResult(Map.of("A", "a", "B", "--"));
		var eq0 = new Nmcli.Con.IdResult(Map.of("A", "a", "B", "--"));
		var ne0 = Nmcli.Con.IdResult.NULL;
		var ne1 = new Nmcli.Con.IdResult(Map.of("A", "a", "B", ""));
		TestUtil.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1);
	}

	@Test
	public void shouldExposeConIdResultValues() {
		var t = new Nmcli.Con.IdResult(Map.of("A", "a", "B", "--"));
		Assert.map(t.values(), "A", "a", "B", "--");
	}

	@Test
	public void shouldSkipBadConIdResultOutputLines() {
		var output = "A: a\nB\nC: --";
		Assert.map(Nmcli.Con.IdResult.fromOutput(output).values(), "A", "a", "C", "--");
	}

	@Test
	public void shouldExecuteNmcliConShow() throws IOException {
		var p = TestProcess.processor(TestUtil.resource("nmcli-con-show.txt"));
		var output = Nmcli.of(p).con.show();
		p.assertParameters("nmcli", "con", "show");
		var results = output.parse();
		Assert.ordered(results,
			new Nmcli.Con.Item("eth1", "01fa0bf4-b6bd-484f-a9a3-2b10ff701dcd", "ethernet", "eth1"),
			new Nmcli.Con.Item("eth0", "2e9f0cdd-ea2f-4b63-b146-3b9a897c9e45", "ethernet", "eth0"),
			new Nmcli.Con.Item("eth2", "186053d4-9369-4a4e-87b8-d1f9a419f985", "ethernet", "eth2"));
		for (var item : results)
			Assert.no(item.isNull());
	}

	@Test
	public void shouldExecuteNmcliConShowId() throws IOException {
		var p = TestProcess.processor(TestUtil.resource("nmcli-con-show-id.txt"));
		var output = Nmcli.of(p).con.show("test");
		p.assertParameters("nmcli", "con", "show", "id", "test");
		var result = output.parse();
		Assert.equal(result.get(Nmcli.Con.IdResult.Key.conId), "eth2");
		Assert.equal(result.get(Nmcli.Con.IdResult.Key.conUuid),
			"186053d4-9369-4a4e-87b8-d1f9a419f985");
		Assert.equal(result.get(Nmcli.Con.IdResult.Key.conIfaceName), "eth2");
		Assert.equal(result.get(Nmcli.Con.IdResult.Key.conType), "802-3-ethernet");
		Assert.equal(result.get(Nmcli.Con.IdResult.Key.genState), "activated");
		Assert.equal(result.parse(Nmcli.Con.IdResult.Key.genVpn).toBool(), true);
		Assert.equal(result.value("connection.stable-id"), "");
		Assert.equal(result.isNull(), false);
	}

	@Test
	public void shouldExecuteNmcliConUp() throws IOException {
		var p = TestProcess.processor("output");
		var output = Nmcli.of(p).con.up("test");
		p.assertParameters("nmcli", "con", "up", "id", "test");
		Assert.equal(output, "output");
		p = TestProcess.processor("output");
		output = Nmcli.of(p).con.up("test", 10);
		p.assertParameters("nmcli", "con", "up", "id", "test", "--wait", "10");
		Assert.equal(output, "output");
	}

	@Test
	public void shouldExecuteNmcliConDown() throws IOException {
		var p = TestProcess.processor("output");
		var output = Nmcli.of(p).con.down("test");
		p.assertParameters("nmcli", "con", "down", "id", "test");
		Assert.equal(output, "output");
	}
}
