package ceri.ent.mongo;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import ceri.common.collect.Immutable;
import ceri.common.collect.Sets;
import ceri.common.net.HostPort;
import ceri.common.net.Net;
import ceri.common.text.Joiner;

public class HostList {
	public static final HostList LOCALHOST = from(Net.LOCALHOST);
	public final List<HostPort> hosts;

	public static HostList from(String... hosts) {
		return from(Arrays.asList(hosts));
	}

	public static HostList from(Iterable<String> hosts) {
		return builder().addFrom(hosts).build();
	}

	public static HostList of(HostPort... hosts) {
		return of(Arrays.asList(hosts));
	}

	public static HostList of(Iterable<HostPort> hosts) {
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

		public HostList build() {
			return new HostList(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	HostList(Builder builder) {
		hosts = Immutable.list(builder.hosts);
	}

	@Override
	public int hashCode() {
		return Objects.hash(hosts);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof HostList other)) return false;
		return Objects.equals(hosts, other.hosts);
	}

	@Override
	public String toString() {
		return Joiner.COMMA.join(hosts);
	}
}
