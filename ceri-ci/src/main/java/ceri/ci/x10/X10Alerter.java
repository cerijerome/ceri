package ceri.ci.x10;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Builds;
import ceri.ci.common.Alerter;
import ceri.common.collection.ImmutableUtil;
import ceri.x10.command.Address;
import ceri.x10.command.Command;
import ceri.x10.util.X10Controller;

/**
 * Alerter that turns on X10 devices for build failures.
 */
public class X10Alerter implements Alerter {
	private static final Logger logger = LogManager.getLogger();
	private final X10Controller x10;
	private final Map<String, Address> addresses;
	private final Set<Address> activeAddresses = new HashSet<>();

	public static class Builder {
		final Map<String, Address> addresses = new HashMap<>();
		final X10Controller x10;

		Builder(X10Controller x10) {
			if (x10 == null) throw new NullPointerException("Controller cannot be null");
			this.x10 = x10;
		}

		public Builder address(String name, String address) {
			if (name == null) throw new NullPointerException("Name cannot be null");
			addresses.put(name, Address.from(address));
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
	}

	@Override
	public void update(Builds builds) {
		Collection<String> breakNames = BuildUtil.summarizedBreakNames(builds);
		alert(breakNames);
	}

	public void alert(String... keys) {
		alert(Arrays.asList(keys));
	}

	public void alert(Collection<String> keys) {
		logger.info("Alerting for {}", keys);
		Collection<Address> addresses = keysToAddresses(keys);
		for (Address address : new HashSet<>(activeAddresses)) {
			if (!addresses.contains(address)) deviceOff(address);
		}
		for (Address address : addresses) {
			if (!activeAddresses.contains(address)) deviceOn(address);
		}
	}

	@Override
	public void clear() {
		logger.info("Clearing state");
		for (Address address : addresses.values())
			deviceOff(address);
	}

	@Override
	public void remind() {
		// Do nothing
	}

	private Collection<Address> keysToAddresses(Collection<String> keys) {
		Collection<Address> addresses = new HashSet<>();
		for (String key : keys) {
			Address address = this.addresses.get(key);
			if (address != null) addresses.add(address);
		}
		return addresses;
	}

	private void deviceOn(Address address) {
		try {
			logger.debug("Turning on {}", address);
			x10.command(Command.on(address));
			activeAddresses.add(address);
		} catch (RuntimeException e) {
			logger.catching(e);
		}
	}

	private void deviceOff(Address address) {
		try {
			logger.debug("Turning off {}", address);
			x10.command(Command.off(address));
			activeAddresses.remove(address);
		} catch (RuntimeException e) {
			logger.catching(e);
		}
	}

}
