package ceri.ci.x10;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import ceri.common.collection.ImmutableUtil;
import ceri.x10.command.CommandFactory;
import ceri.x10.type.Address;
import ceri.x10.type.House;
import ceri.x10.util.X10Controller;

public class X10Alerter {
	private final X10Controller x10;
	private final Map<String, Address> addresses;
	private final Set<House> houseCodes;

	public static class Builder {
		final Map<String, Address> addresses = new HashMap<>();
		final X10Controller x10;

		Builder(X10Controller x10) {
			if (x10 == null) throw new NullPointerException("Controller cannot be null");
			this.x10 = x10;
		}

		public Builder address(String name, String address) {
			if (name == null) throw new NullPointerException("Name cannot be null");
			addresses.put(name, Address.fromString(address));
			return this;
		}

		public X10Alerter build() {
			return new X10Alerter(this);
		}
	}

	public static Builder builder(X10Controller x10) {
		return new Builder(x10);
	}

	X10Alerter(Builder builder) {
		x10 = builder.x10;
		addresses = ImmutableUtil.copyAsMap(builder.addresses);
		houseCodes = Collections.unmodifiableSet(houseCodes(addresses));
	}

	public void alert(String... keys) {
		alert(Arrays.asList(keys));
	}

	public void alert(Collection<String> keys) {
		clearAlerts();
		for (String key : keys)
			doAlert(key);
	}

	public void clear() {
		clearAlerts();
	}

	private void clearAlerts() {
		for (House houseCode : houseCodes)
			x10.command(CommandFactory.allUnitsOff(houseCode));
	}

	private void doAlert(String key) {
		Address address = addresses.get(key);
		if (address == null) return;
		x10.command(CommandFactory.on(address.house, address.unit));
	}

	private Set<House> houseCodes(Map<String, Address> addresses) {
		Set<House> houseCodes = new HashSet<>();
		for (Address address : addresses.values())
			houseCodes.add(address.house);
		return houseCodes;
	}

}
