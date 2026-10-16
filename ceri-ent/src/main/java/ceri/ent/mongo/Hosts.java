package ceri.ent.mongo;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.collect.Sets;
import ceri.common.net.HostPort;
import ceri.common.net.Net;
import ceri.common.text.Joiner;

public record Hosts(List<HostPort> hosts) {
	public static final Hosts LOCALHOST = from(Net.LOCALHOST);

	public record Group(String name, Hosts hosts) {

		public List<HostPort> hostPorts() {
			return hosts().hosts();
		}

		@Override
		public String toString() {
			return name + "/" + hosts;
		}
	}

	public static Group group(String name, HostPort... hosts) {
		return group(name, Lists.wrap(hosts));
	}

	public static Group group(String name, Iterable<HostPort> hosts) {
		return new Group(name, Hosts.of(hosts));
	}

	public static Hosts from(String... hosts) {
		return from(Arrays.asList(hosts));
	}

	public static Hosts from(Iterable<String> hosts) {
		return builder().addFrom(hosts).build();
	}

	public static Hosts of(HostPort... hosts) {
		return of(Arrays.asList(hosts));
	}

	public static Hosts of(Iterable<HostPort> hosts) {
		return builder().add(hosts).build();
	}

	public static class Builder {
		final Collection<HostPort> hosts = Sets.link();

		Builder() {}

		public Builder addFrom(String... hosts) {
			return addFrom(Arrays.asList(hosts));
		}

		public Builder addFrom(Iterable<String> hosts) {
			hosts.forEach(host -> this.hosts.add(HostPort.parse(host)));
			return this;
		}

		public Builder add(HostPort... hosts) {
			return add(Arrays.asList(hosts));
		}

		public Builder add(Iterable<HostPort> hosts) {
			hosts.forEach(this.hosts::add);
			return this;
		}

		public Hosts build() {
			return new Hosts(Immutable.list(hosts));
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String toString() {
		return Joiner.COMMA_COMPACT.join(hosts);
	}
}
