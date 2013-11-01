package ceri.ci.x10;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import x10.CM11ASerialController;
import x10.Command;
import x10.Controller;
import ceri.ci.common.Alerter;
import ceri.common.collection.ImmutableUtil;

public class X10Alerter implements Alerter, Closeable {
	private static final String CONFIG_FILE = "x10.properties";
	private static final String COMM_PORT = "comm.port";
	private static final String ADDRESS_PREFIX = "address.";
	private static final int ADDRESS_OFFSET = ADDRESS_PREFIX.length();
	private static final int DEVICE_DEF = 1;
	private final Controller x10;
	private final Map<String, String> addresses;
	private final Set<String> houseCodes;

	X10Alerter(Properties properties, Controller x10) {
		addresses = ImmutableUtil.copyAsMap(addresses(properties));
		houseCodes = ImmutableUtil.copyAsSet(houseCodes(addresses));
		this.x10 = x10;
	}

	public static X10Alerter create(File rootDir) throws IOException {
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
	
	private static CM11ASerialController createController(Properties config) throws IOException {
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
