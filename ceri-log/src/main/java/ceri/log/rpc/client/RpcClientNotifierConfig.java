package ceri.log.rpc.client;

import java.util.Objects;
import ceri.common.text.ToString;

public class RpcClientNotifierConfig {
	public static final RpcClientNotifierConfig DEFAULT = builder().build();
	public final int resetDelayMs;

	public static RpcClientNotifierConfig of() {
		return builder().build();
	}

	public static RpcClientNotifierConfig of(int resetDelayMs) {
		return builder().resetDelayMs(resetDelayMs).build();
	}

	public static class Builder {
		int resetDelayMs = 3000;

		Builder() {}

		public Builder resetDelayMs(int resetDelayMs) {
			this.resetDelayMs = resetDelayMs;
			return this;
		}

		public RpcClientNotifierConfig build() {
			return new RpcClientNotifierConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	RpcClientNotifierConfig(Builder builder) {
		resetDelayMs = builder.resetDelayMs;
	}

	@Override
	public int hashCode() {
		return Objects.hash(resetDelayMs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof RpcClientNotifierConfig)) return false;
		RpcClientNotifierConfig other = (RpcClientNotifierConfig) obj;
		if (resetDelayMs != other.resetDelayMs) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, resetDelayMs);
	}

}
