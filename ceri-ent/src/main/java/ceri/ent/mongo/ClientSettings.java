package ceri.ent.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import ceri.common.net.HostPort;

/**
 * Simplified mongo client settings.
 */
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class ClientSettings {
	public final HostList hosts;
	public final Integer connectionTimeoutMs;
	public final Integer readTimeoutMs;

	public static ClientSettings of(int timeoutMs) {
		return of(HostPort.LOCALHOST, timeoutMs);
	}

	public static ClientSettings of(HostPort host, int timeoutMs) {
		return builder().hostPorts(host).connectionTimeoutMs(timeoutMs).readTimeoutMs(timeoutMs)
			.build();
	}

	public static class Builder {
		final List<HostPort> hosts = new ArrayList<>();
		Integer connectionTimeoutMs;
		Integer readTimeoutMs;

		Builder() {}

		public Builder hostPorts(HostPort... hosts) {
			return hostPorts(Arrays.asList(hosts));
		}

		public Builder hostPorts(Collection<HostPort> hosts) {
			this.hosts.addAll(hosts);
			return this;
		}

		public Builder hosts(HostList hosts) {
			return hostPorts(hosts.hosts);
		}

		public Builder hosts(String... hosts) {
			return hosts(Arrays.asList(hosts));
		}

		public Builder hosts(Collection<String> hosts) {
			hosts.forEach(host -> this.hosts.add(HostPort.parse(host)));
			return this;
		}

		public Builder connectionTimeoutMs(int connectionTimeoutMs) {
			this.connectionTimeoutMs = connectionTimeoutMs;
			return this;
		}

		public Builder readTimeoutMs(int readTimeoutMs) {
			this.readTimeoutMs = readTimeoutMs;
			return this;
		}

		public HostList hostList() {
			return hosts.isEmpty() ? HostList.LOCALHOST : HostList.of(hosts);
		}

		public ClientSettings build() {
			return new ClientSettings(this);
		}
	}

	public static Builder builder(String connection) {
		return builder().hosts(new ConnectionString(connection).getHosts());
	}

	public static Builder builder() {
		return new Builder();
	}

	ClientSettings(Builder builder) {
		hosts = builder.hostList();
		connectionTimeoutMs = builder.connectionTimeoutMs;
		readTimeoutMs = builder.readTimeoutMs;
	}

	public MongoClientSettings toMongo() {
		MongoClientSettings.Builder b = MongoClientSettings.builder();
		b.applyConnectionString(new ConnectionString(MongoUtil.connection(hosts)));
		if (connectionTimeoutMs != null) b.applyToSocketSettings(
			s -> s.connectTimeout(connectionTimeoutMs, TimeUnit.MILLISECONDS));
		if (readTimeoutMs != null)
			b.applyToSocketSettings(s -> s.readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS));
		return b.build();
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(hosts, connectionTimeoutMs, readTimeoutMs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ClientSettings)) return false;
		ClientSettings other = (ClientSettings) obj;
		if (!EqualsUtil.equals(hosts, other.hosts)) return false;
		if (!EqualsUtil.equals(connectionTimeoutMs, other.connectionTimeoutMs)) return false;
		if (!EqualsUtil.equals(readTimeoutMs, other.readTimeoutMs)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, hosts, connectionTimeoutMs, readTimeoutMs)
			.toString();
	}

}
