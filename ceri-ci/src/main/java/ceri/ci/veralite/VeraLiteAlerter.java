package ceri.ci.veralite;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import ceri.common.collection.ImmutableUtil;
import ceri.common.property.PropertyUtil;
import ceri.common.util.BasicUtil;
import ceri.zwave.veralite.VeraLite;

public class VeraLiteAlerter {
	private final Map<String, Integer> devices;
	private final VeraLite veraLite;

	public static void main(String[] args) {
		String host = "192.168.0.109:3480";
		VeraLite veraLite = new VeraLite(host);
		VeraLiteAlerter vlAlerter =
			VeraLiteAlerter.builder(veraLite).device("ceri", 5).device("cj", 6).build();
		vlAlerter.alert("ceri", "cj");
		BasicUtil.delay(5000);
		vlAlerter.clear();
	}

	public static VeraLiteAlerter create(File propertyFile, String prefix) throws IOException {
		return create(PropertyUtil.load(propertyFile), prefix);
	}

	public static VeraLiteAlerter create(Properties properties, String prefix) {
		VeraLiteAlerterProperties vlProperties = new VeraLiteAlerterProperties(properties, prefix);
		String host = vlProperties.host();
		VeraLite veraLite = new VeraLite(host);
		// TODO: check connectivity
		Builder builder = builder(veraLite);
		for (String name : vlProperties.names()) {
			Integer device = vlProperties.device(name);
			builder.device(name, device);
		}
		return builder.build();
	}

	public static class Builder {
		final Map<String, Integer> devices = new HashMap<>();
		final VeraLite veraLite;

		Builder(VeraLite veraLite) {
			if (veraLite == null) throw new NullPointerException("VeraLite cannot be null");
			this.veraLite = veraLite;
		}

		public Builder device(String name, int device) {
			if (name == null) throw new NullPointerException("Name cannot be null");
			if (device <= 0) throw new IllegalArgumentException("Not a valid device for " + name +
				": " + device);
			devices.put(name, device);
			return this;
		}

		public VeraLiteAlerter build() {
			return new VeraLiteAlerter(this);
		}
	}

	public static Builder builder(VeraLite veraLite) {
		return new Builder(veraLite);
	}

	VeraLiteAlerter(Builder builder) {
		veraLite = builder.veraLite;
		devices = ImmutableUtil.copyAsMap(builder.devices);
	}

	public void alert(String... keys) {
		//clear();
		for (String key : keys) {
			Integer device = devices.get(key);
			if (device != null) doAlert(device);
		}
	}

	public void clear() {
		for (int device : devices.values())
			clearAlert(device);
	}

	private void clearAlert(int device) {
		try {
			veraLite.switchPower.off(device);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void doAlert(int device) {
		try {
			veraLite.switchPower.on(device);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
