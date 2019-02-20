package ceri.log.rpc;

import static ceri.common.net.NetUtil.LOCALHOST;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class RpcClientConfig {
	public final String host;
	public final int port;

	public static RpcClientConfig localhost(int port) {
		return of(LOCALHOST, port);
	}

	public static RpcClientConfig of(String host, int port) {
		return new RpcClientConfig(host, port);
	}

	private RpcClientConfig(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(host, port);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof RpcClientConfig)) return false;
		RpcClientConfig other = (RpcClientConfig) obj;
		if (!EqualsUtil.equals(host, other.host)) return false;
		if (port != other.port) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, host, port).toString();
	}

}
