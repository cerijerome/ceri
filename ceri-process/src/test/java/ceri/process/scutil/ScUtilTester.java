package ceri.process.scutil;

import java.io.IOException;
import java.util.List;
import ceri.common.collect.Collectable;
import ceri.common.process.Output;
import ceri.common.test.Debugger;

public class ScUtilTester {
	private final ScUtil scUtil;

	public static void main(String[] args) throws IOException {
		var tester = new ScUtilTester(ScUtil.of());
		var list = tester.list();
		if (Collectable.isEmpty(list)) System.out.println("No services");
		else try {
			tester.status(list.get(0).name());
			tester.statistics(list.get(0).name());
			tester.show(list.get(0).name());
		} catch (IOException e) {
			System.out.println("show failed");
		}
	}

	private ScUtilTester(ScUtil scUtil) {
		this.scUtil = scUtil;
	}

	private ScUtil.Nc.Show show(String service) throws IOException {
		hr(true);
		Debugger.DBG.method(service);
		var output = scUtil.nc.show(service);
		print(output);
		var show = output.parse();
		System.out.println(show);
		return show;
	}

	private ScUtil.Nc.Stats statistics(String service) throws IOException {
		hr(true);
		Debugger.DBG.method(service);
		var output = scUtil.nc.statistics(service);
		print(output);
		var statistics = output.parse();
		System.out.println(statistics);
		return statistics;
	}

	private ScUtil.Nc.Status status(String service) throws IOException {
		hr(true);
		Debugger.DBG.method(service);
		var output = scUtil.nc.status(service);
		print(output);
		var status = output.parse();
		System.out.println(status);
		return status;
	}

	private List<ScUtil.Nc.Item> list() throws IOException {
		hr(true);
		Debugger.DBG.method();
		var output = scUtil.nc.list();
		print(output);
		var items = output.parse();
		items.forEach(System.out::println);
		return items;
	}

	private static void print(Output<?> output) {
		System.out.println(output.out.trim());
		hr(false);
	}

	private static void hr(boolean bold) {
		System.out.println((bold ? "_" : "-").repeat(100));
	}
}
