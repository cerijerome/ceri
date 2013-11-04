package ceri.ci.veralite;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import x10.CM11ASerialController;
import x10.Command;
import x10.Controller;
import ceri.ci.x10.X10Alerter;
import ceri.common.collection.ImmutableUtil;
import ceri.common.util.BasicUtil;

public class VeraLiteAlerter implements Alerter {
	private static final String CONFIG_FILE = "veralite.properties";
	private static final String SERVER_DOMAIN = "server.domain";
	private static final String DEVICE_PREFIX = "device.";
	private final Controller x10;
	private final Map<String, String> addresses;
	private final Set<String> houseCodes;

	public static void main(String[] args) throws IOException {
		String url =
			"http://192.168.0.109:3480/data_request?id=action&DeviceNum=5&"
				+ "serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=";
		//http://192.168.0.109:3480/data_request?id=user_data
		Content content = Request.Get(url + "100").execute().returnContent();
		System.out.println(content.asString());
		BasicUtil.delay(3000);
		content = Request.Get(url + "0").execute().returnContent();
		System.out.println(content.asString());
	}
	
	VeraLiteAlerter(Properties properties, Controller x10) {
		addresses = ImmutableUtil.copyAsMap(addresses(properties));
		houseCodes = ImmutableUtil.copyAsSet(houseCodes(addresses));
		this.x10 = x10;
	}

	public static VeraLiteAlerter create(File rootDir) throws IOException {
		Properties properties = loadConfig(rootDir);
		Controller x10 = createController(properties);
		return new X10Alerter(properties, x10);
	}
	
	@Override
	public void alert(String... keys) {
		clearAlerts();
		for (String key : keys) doAlert(key);
	}

	@Override
	public void clear(String... keys) {
		clearAlerts();
	}

	@Override
	public void close() throws IOException {
		if (x10 != null) x10.close();
	}

	private void clearAlerts() {
		for (String houseCode : houseCodes)
			x10.addCommand(new Command(houseCode, Command.ALL_UNITS_OFF));
	}
	
	private void doAlert(String key) {
		String address = addresses.get(key);
		if (address == null) return;
		x10.addCommand(new Command(address, Command.ON));
	}

	private Set<String> houseCodes(Map<String, String> addresses) {
		Set<String> houseCodes = new HashSet<>();
		for (String address : addresses.values())
			houseCodes.add("" + address.charAt(0) + DEVICE_DEF);
		return houseCodes;
	}
	
	private Map<String, String> addresses(Properties config) {
		Map<String, String> addresses = new HashMap<>();
		for (String s : config.stringPropertyNames()) {
			String key = key(s);
			if (key == null) continue;
			String address = config.getProperty(s);
			if (!isValidAddress(address)) throw new IllegalArgumentException(
				"Invalid address for " + s + ": " + address);
			addresses.put(key, address);
		}
		return addresses;
	}

	private String key(String addressKey) {
		if (addressKey == null) return null;
		String s = addressKey.trim();
		if (!s.startsWith(ADDRESS_PREFIX)) return null;
		return s.substring(ADDRESS_OFFSET);
	}
	
	private boolean isValidAddress(String address) {
		return  address != null && address.length() > 1 && Command.isValid(address);
	}
	
	private static VeraLite createController(Properties config) throws IOException {
		String commPort = config.getProperty(COMM_PORT);
		if (commPort == null) throw new IllegalArgumentException(COMM_PORT + " not specified");
		return new CM11ASerialController(commPort);
	}

	private static Properties loadConfig(File rootDir) throws IOException {
		Properties properties = new Properties();
		properties.load(new FileReader(new File(rootDir, CONFIG_FILE)));
		return properties;
	}

}
