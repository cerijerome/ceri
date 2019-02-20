package ceri.log.rpc;

import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

public class RpcServiceConfig {
	public final int port;

	public static RpcServiceConfig of(int port) {
		return new RpcServiceConfig(port);
	}

	private RpcServiceConfig(int port) {
		this.port = port;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(port);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof RpcServiceConfig)) return false;
		RpcServiceConfig other = (RpcServiceConfig) obj;
		if (port != other.port) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, port).toString();
	}

}
