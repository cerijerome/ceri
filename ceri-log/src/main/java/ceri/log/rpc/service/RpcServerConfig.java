package ceri.log.rpc.service;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.log.rpc.client.RpcChannelConfig;

public class RpcServerConfig {
	public static final RpcServerConfig NULL = builder().build();
	public final Integer port;
	public final int shutdownTimeoutMs;

	/**
	 * Default settings with server-chosen port.
	 */
	public static RpcServerConfig of() {
		return of(0);
	}

	/**
	 * Default settings with given port.
	 */
	public static RpcServerConfig of(int port) {
		return builder().port(port).build();
	}

	public static class Builder {
		Integer port = null;
		int shutdownTimeoutMs = 5000;

		Builder() {}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder shutdownTimeoutMs(int shutdownTimeoutMs) {
			this.shutdownTimeoutMs = shutdownTimeoutMs;
			return this;
		}

		public RpcServerConfig build() {
			return new RpcServerConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	RpcServerConfig(Builder builder) {
		port = builder.port;
		shutdownTimeoutMs = builder.shutdownTimeoutMs;
	}

	public boolean enabled() {
		return port != null;
	}

	/**
	 * Checks if the channel will connect to this server based on configuration.
	 */
	public boolean isLoop(RpcChannelConfig channel) {
		if (!enabled() || channel == null || !channel.isLocalhost()) return false;
		return EqualsUtil.equals(channel.port, port);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(port, shutdownTimeoutMs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof RpcServerConfig)) return false;
		RpcServerConfig other = (RpcServerConfig) obj;
		if (!EqualsUtil.equals(port, other.port)) return false;
		if (shutdownTimeoutMs != other.shutdownTimeoutMs) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, port, shutdownTimeoutMs).toString();
	}

}
