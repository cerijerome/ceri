package ceri.process.nmcli;

public class NmcliTester {

	public static void main(String[] args) throws Exception {
		var showOut = Nmcli.of().con.show();
		System.out.printf("nmcli show:\n%s\n", showOut);
		var name = showOut.parse().get(0).name();
		var showIdOut = Nmcli.of().con.show(name);
		System.out.printf("nmcli show id %s:\n%s\n", name, showIdOut);
	}
}
