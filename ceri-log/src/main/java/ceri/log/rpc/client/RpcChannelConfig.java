package ceri.log.rpc.client;

import static ceri.common.net.NetUtil.LOCALHOST;
import ceri.common.net.NetUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class RpcChannelConfig {
	public static final RpcChannelConfig NULL = builder().build();
	public final String host;
	public final Integer port;

	public static RpcChannelConfig localhost(int port) {
		return builder().host(LOCALHOST).port(port).build();
	}

	public static RpcChannelConfig of(String host, int port) {
		return builder().host(host).port(port).build();
	}

	public static class Builder {
		String host = null;
		Integer port = null;

		Builder() {}

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public RpcChannelConfig build() {
			return new RpcChannelConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	RpcChannelConfig(Builder builder) {
		host = builder.host;
		port = builder.port;
	}

	public boolean enabled() {
		return host != null && port != null;
	}

	public boolean isLocalhost() {
		return NetUtil.isLocalhost(host);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(host, port);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof RpcChannelConfig)) return false;
		RpcChannelConfig other = (RpcChannelConfig) obj;
		if (!EqualsUtil.equals(host, other.host)) return false;
		if (port != other.port) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, host, port).toString();
	}

}
