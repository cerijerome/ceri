package ceri.process.nmcli;

import java.util.List;
import ceri.common.process.Output;

public class NmcliTester {

	public static void main(String[] args) throws Exception {
		Output<List<ConShowItem>> showOut = Nmcli.of().con.show();
		System.out.printf("nmcli show:\n%s\n", showOut);
		String name = showOut.parse().get(0).name;
		Output<?> showIdOut = Nmcli.of().con.show(name);
		System.out.printf("nmcli show id %s:\n%s\n", name, showIdOut);
	}

}
