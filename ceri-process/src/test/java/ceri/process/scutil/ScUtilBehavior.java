package ceri.process.scutil;

import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import ceri.common.collect.Node;
import ceri.common.process.Parameters;
import ceri.common.test.Assert;
import ceri.common.test.TestProcess;
import ceri.common.test.Testing;

public class ScUtilBehavior {
	private static final String ncStatsOutput = Testing.resource("statistics-output.txt");

	@Test
	public void shouldHaveDefaultProcessor() {
		Assert.notNull(ScUtil.of().nc);
	}

	@Test
	public void shouldNotBreachNcStatusEqualsContract() {
		var node0 = Node.<Void>tree().startGroup("grp", null).value("val", 7).build();
		var node1 = Node.<Void>tree().startGroup("grp", null).value("val", 8).build();
		var t = new ScUtil.Nc.Status(ScUtil.Nc.State.connecting, node0);
		var eq0 = new ScUtil.Nc.Status(ScUtil.Nc.State.connecting, node0);
		var ne0 = new ScUtil.Nc.Status(ScUtil.Nc.State.unknown, node0);
		var ne1 = new ScUtil.Nc.Status(ScUtil.Nc.State.connecting, node1);
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1);
	}

	@Test
	public void shouldCreateNcStatusFromOutput() {
		var ns = ScUtil.Nc.Status.from("No service");
		Assert.equal(ns.state(), ScUtil.Nc.State.noService);
		Assert.equal(ns.data(), Node.NULL);
	}

	@Test
	public void shouldNotBreachNcStatsEqualsContract() {
		var t = new ScUtil.Nc.Stats(Map.of("BytesIn", 100));
		var eq0 = new ScUtil.Nc.Stats(Map.of("BytesIn", 100));
		var eq1 = new ScUtil.Nc.Stats(Map.of("BytesIn", 100));
		var ne0 = new ScUtil.Nc.Stats(Map.of("BytesOut", 100));
		var ne1 = new ScUtil.Nc.Stats(Map.of("BytesIn", 99));
		Testing.exerciseEquals(t, eq0, eq1);
		Assert.notEqualAll(t, ne0, ne1);
	}

	@Test
	public void shouldCreateNcStatsFromOutput() {
		var ns = ScUtil.Nc.Stats.from(ncStatsOutput);
		Assert.equal(ns.value(ScUtil.Nc.Stats.Key.bytesIn), 20337);
		Assert.equal(ns.value(ScUtil.Nc.Stats.Key.bytesOut), 16517);
		Assert.equal(ns.value(ScUtil.Nc.Stats.Key.errorsIn), 10);
		Assert.equal(ns.value(ScUtil.Nc.Stats.Key.errorsOut), 0);
		Assert.equal(ns.value(ScUtil.Nc.Stats.Key.packetsIn), 77);
		Assert.equal(ns.value(ScUtil.Nc.Stats.Key.packetsOut), 118);
	}

	@Test
	public void shouldCalculateNcStatsErrorRate() {
		var ns = ScUtil.Nc.Stats.NULL;
		Assert.approx(ns.packetErrorRateIn(), 0.0);
		Assert.approx(ns.packetErrorRateOut(), 0.0);
		ns = ScUtil.Nc.Stats.from(ncStatsOutput);
		Assert.approx(ns.packetErrorRateIn(), 0.13);
		Assert.approx(ns.packetErrorRateOut(), 0.0);
	}

	@Test
	public void shouldNotBreachNcItemEqualsContract() {
		var t = ScUtil.Nc.Item.from("* (Connected)  X P --> D \"N\" [P:T]");
		var eq0 = ScUtil.Nc.Item.from("* (Connected)  X P --> D \"N\" [P:T]");
		var eq1 = new ScUtil.Nc.Item(true, ScUtil.Nc.State.connected, "X", "P", "D", "N", "T");
		var ne0 = ScUtil.Nc.Item.from("  (Connected)  X P --> D \"N\" [P:T]");
		var ne1 = ScUtil.Nc.Item.from("* (No Service) X P --> D \"N\" [P:T]");
		var ne2 = ScUtil.Nc.Item.from("* (Connected)  Y P --> D \"N\" [P:T]");
		var ne3 = ScUtil.Nc.Item.from("* (Connected)  X Q --> D \"N\" [P:T]");
		var ne4 = ScUtil.Nc.Item.from("* (Connected)  X P --> E \"N\" [P:T]");
		var ne5 = ScUtil.Nc.Item.from("* (Connected)  X P --> D \"O\" [P:T]");
		var ne6 = ScUtil.Nc.Item.from("* (Connected)  X P --> D \"N\" [P:U]");
		Testing.exerciseEquals(t, eq0, eq1);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6);
	}

	@Test
	public void shouldShowStateOnlyForNoService() {
		Assert.equal(ScUtil.Nc.Item.NULL.toString(), ScUtil.Nc.State.noService.toString());
	}

	@Test
	public void shouldShowNcItemEnabledStateInString() {
		Assert.match(new ScUtil.Nc.Item(true, ScUtil.Nc.State.unknown, "", "", "", "", ""),
			"\\* .*");
		Assert.match(new ScUtil.Nc.Item(false, ScUtil.Nc.State.unknown, "", "", "", "", ""), "  .*");
	}

	@Test
	public void shouldNotBreachNcShowEqualsContract() {
		var item0 = ScUtil.Nc.Item.from("* (Connecting) X P --> D \"N\" [P:T]");
		var item1 = ScUtil.Nc.Item.from("  (Connecting) X P --> D \"N\" [P:T]");
		var node0 = Node.<Void>tree().startGroup("grp", null).value("val", 7).build();
		var node1 = Node.<Void>tree().startGroup("grp", null).value("val", 8).build();
		var t = new ScUtil.Nc.Show(item0, node0);
		var eq0 = new ScUtil.Nc.Show(item0, node0);
		var ne0 = new ScUtil.Nc.Show(item1, node0);
		var ne1 = new ScUtil.Nc.Show(item0, node1);
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1);
	}

	@Test
	public void shouldCreateNcShowFromOutput() {
		var ns = ScUtil.Nc.Show.from("* (Connecting) X P --> D \"N\" [P:T]");
		Assert.equal(ns.item(),
			new ScUtil.Nc.Item(true, ScUtil.Nc.State.connecting, "X", "P", "D", "N", "T"));
		Assert.equal(ns.data(), Node.NULL);
	}

	@Test
	public void shouldExecuteNcList() throws IOException {
		var p = TestProcess.processor(Testing.resource("list-output.txt"));
		var result = ScUtil.of(p).nc.list();
		p.exec.assertAuto(Parameters.of("scutil", "--nc", "list"));
		Assert.ordered(result.parse(),
			new ScUtil.Nc.Item(false, ScUtil.Nc.State.disconnected,
				"XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX", "PPP", "FT245R USB FIFO", "FT245R USB FIFO",
				"Modem"),
			new ScUtil.Nc.Item(true, ScUtil.Nc.State.connecting,
				"00000000-0000-0000-0000-000000000000", "I2C", "FT232R USB UART", "FT232R USB UART",
				"Device"));
	}

	@Test
	public void shouldExecuteNcStatus() throws IOException {
		var p = TestProcess.processor(Testing.resource("status-output.txt"));
		var scUtil = ScUtil.of(p);
		var result = scUtil.nc.status("test");
		p.assertParameters("scutil", "--nc", "status", "test");
		var ns = result.parse();
		Assert.equal(ns.state(), ScUtil.Nc.State.connected);
		Assert.equal(ns.data().find("Extended Status.PPP.DeviceLastCause").parse().toInt(), 99);
		Assert.equal(ns.data().find("Extended Status.PPP.LastCause").parse().toInt(), 90);
		Assert.equal(ns.data().find("Extended Status.PPP.Status").parse().toInt(), 1);
		Assert.equal(ns.data().find("Extended Status.Status").parse().toInt(), 0);
	}

	@Test
	public void shouldExecuteNcShow() throws IOException {
		var p = TestProcess.processor(Testing.resource("show-output.txt"));
		var result = ScUtil.of(p).nc.show("test");
		p.assertParameters("scutil", "--nc", "show", "test");
		var ns = result.parse();
		Assert.equal(ns.item(),
			new ScUtil.Nc.Item(true, ScUtil.Nc.State.connecting,
				"00000000-0000-0000-0000-000000000000", "I2C", "FT232R USB UART", "FT232R USB UART",
				"Device"));
		Assert.equal(ns.data().find("Extended Status.PPP.DeviceLastCause").parse().toInt(), 99);
		Assert.equal(ns.data().find("Extended Status.PPP.LastCause").parse().toInt(), 90);
		Assert.equal(ns.data().find("Extended Status.PPP.Status").parse().toInt(), 1);
		Assert.equal(ns.data().find("Extended Status.Status").parse().toInt(), 0);
	}

	@Test
	public void shouldExecuteNcStatistics() throws IOException {
		var p = TestProcess.processor(Testing.resource("statistics-output.txt"));
		var result = ScUtil.of(p).nc.statistics("test");
		p.assertParameters("scutil", "--nc", "statistics", "test");
		var ns = result.parse();
		Assert.equal(ns, new ScUtil.Nc.Stats(Map.of("BytesIn", 20337, "BytesOut", 16517, "ErrorsIn",
			10, "PacketsIn", 77, "PacketsOut", 118)));
	}

	@Test
	public void shouldExecuteNcStart() throws IOException {
		var p = TestProcess.processor("output");
		var result = ScUtil.of(p).nc.start("test", "user", "pwd", "secret");
		p.assertParameters("scutil", "--nc", "start", "test", "--user", "user", "--password", "pwd",
			"--secret", "secret");
		Assert.equal(result, "output");
		p = TestProcess.processor("output");
		result = ScUtil.of(p).nc.start("test", null, null, null);
		p.assertParameters("scutil", "--nc", "start", "test");
		Assert.equal(result, "output");
	}

	@Test
	public void shouldExecuteNcStop() throws IOException {
		var p = TestProcess.processor("output");
		var result = ScUtil.of(p).nc.stop("test");
		p.assertParameters("scutil", "--nc", "stop", "test");
		Assert.equal(result, "output");
	}
}
