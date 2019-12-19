package ceri.process.scutil;

import java.io.IOException;
import java.util.List;
import ceri.common.process.Output;
import ceri.common.test.Debugger;

public class ScUtilTester {
	private final ScUtil scUtil;

	public static void main(String[] args) throws IOException {
		ScUtilTester tester = new ScUtilTester(ScUtil.of());
		List<NcListItem> list = tester.list();
		tester.status(list.get(0).name);
		tester.statistics(list.get(0).name);
		try {
			tester.show(list.get(0).name);
		} catch (IOException e) {
			System.out.println("show failed");
		}
	}

	private ScUtilTester(ScUtil scUtil) {
		this.scUtil = scUtil;
	}

	private NcShow show(String service) throws IOException {
		hr(true);
		Debugger.DBG.method(service);
		Output<NcShow> output = scUtil.nc.show(service);
		print(output);
		NcShow show = output.parse();
		System.out.println(show);
		return show;
	}

	private NcStatistics statistics(String service) throws IOException {
		hr(true);
		Debugger.DBG.method(service);
		Output<NcStatistics> output = scUtil.nc.statistics(service);
		print(output);
		NcStatistics statistics = output.parse();
		System.out.println(statistics);
		return statistics;
	}

	private NcStatus status(String service) throws IOException {
		hr(true);
		Debugger.DBG.method(service);
		Output<NcStatus> output = scUtil.nc.status(service);
		print(output);
		NcStatus status = output.parse();
		System.out.println(status);
		return status;
	}

	private List<NcListItem> list() throws IOException {
		hr(true);
		Debugger.DBG.method();
		Output<List<NcListItem>> output = scUtil.nc.list();
		print(output);
		List<NcListItem> items = output.parse();
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
