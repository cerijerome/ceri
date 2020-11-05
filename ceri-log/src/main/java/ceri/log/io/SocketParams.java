package ceri.log.io;

import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import ceri.common.text.ToString;

/**
 * Socket configuration parameters.
 */
public class SocketParams {
	public static final SocketParams DEFAULT = builder().build();
	private static final int SO_LINGER_DISABLED = -1;
	public final Integer receiveBufferSize;
	public final Integer sendBufferSize;
	public final Boolean tcpNoDelay;
	public final Integer soTimeoutSeconds;
	public final Integer soLingerSeconds;
	public final Boolean keepAlive;

	public static class Builder {
		Integer receiveBufferSize;
		Integer sendBufferSize;
		Boolean tcpNoDelay;
		Integer soTimeoutSeconds;
		Integer soLingerSeconds;
		Boolean keepAlive;

		Builder() {}

		public Builder receiveBufferSize(int receiveBufferSize) {
			this.receiveBufferSize = receiveBufferSize;
			return this;
		}

		public Builder sendBufferSize(int sendBufferSize) {
			this.sendBufferSize = sendBufferSize;
			return this;
		}

		public Builder soTimeout(int soTimeoutSeconds) {
			this.soTimeoutSeconds = soTimeoutSeconds;
			return this;
		}

		public Builder soLingerOff() {
			return soLinger(SO_LINGER_DISABLED);
		}

		public Builder soLinger(int soLingerSeconds) {
			this.soLingerSeconds = soLingerSeconds;
			return this;
		}

		public Builder tcpNoDelay(boolean enabled) {
			tcpNoDelay = enabled;
			return this;
		}

		public Builder keepAlive(boolean enabled) {
			keepAlive = enabled;
			return this;
		}

		public SocketParams build() {
			return new SocketParams(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	SocketParams(Builder builder) {
		receiveBufferSize = builder.receiveBufferSize;
		sendBufferSize = builder.sendBufferSize;
		tcpNoDelay = builder.tcpNoDelay;
		soTimeoutSeconds = builder.soTimeoutSeconds;
		soLingerSeconds = builder.soLingerSeconds;
		keepAlive = builder.keepAlive;
	}

	public boolean isSoLingerOff() {
		return Objects.equals(soLingerSeconds, SO_LINGER_DISABLED);
	}

	public void applyTo(Socket socket) throws SocketException {
		if (socket == null) return;
		if (receiveBufferSize != null) socket.setReceiveBufferSize(receiveBufferSize);
		if (sendBufferSize != null) socket.setSendBufferSize(sendBufferSize);
		if (keepAlive != null) socket.setKeepAlive(keepAlive);
		if (tcpNoDelay != null) socket.setTcpNoDelay(tcpNoDelay);
		if (soTimeoutSeconds != null) socket.setSoTimeout(soTimeoutSeconds);
		if (soLingerSeconds != null) socket.setSoLinger(!isSoLingerOff(), soLingerSeconds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(receiveBufferSize, sendBufferSize, tcpNoDelay, soTimeoutSeconds,
			soLingerSeconds, keepAlive);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SocketParams)) return false;
		SocketParams other = (SocketParams) obj;
		if (!Objects.equals(receiveBufferSize, other.receiveBufferSize)) return false;
		if (!Objects.equals(sendBufferSize, other.sendBufferSize)) return false;
		if (!Objects.equals(keepAlive, other.keepAlive)) return false;
		if (!Objects.equals(tcpNoDelay, other.tcpNoDelay)) return false;
		if (!Objects.equals(soTimeoutSeconds, other.soTimeoutSeconds)) return false;
		if (!Objects.equals(soLingerSeconds, other.soLingerSeconds)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, receiveBufferSize, sendBufferSize, tcpNoDelay,
			soTimeoutSeconds, soLingerSeconds, keepAlive);
	}

}
