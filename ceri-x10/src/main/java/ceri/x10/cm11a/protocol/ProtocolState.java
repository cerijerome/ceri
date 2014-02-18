package ceri.x10.cm11a.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProtocolState {
	private static final Logger logger = LogManager.getLogger();
	private final DataInputStream in;
	private Byte unexpectedByte = null;
	
	public ProtocolState(DataInputStream in) {
		this.in = in;
	}
	
	public Protocol protocol() throws IOException {
		if (in.available() == 0) return null;
		byte b;
		try {
			if (unexpectedByte == null) b = in.readByte();
			else b = unexpectedByte;
			unexpectedByte = null;
			return Protocol.fromValue(b);
		} catch (IllegalArgumentException e) {
			logger.catching(Level.WARN, e);
			return null;
		}
	}
		
	public void unexpected(byte b) {
		unexpectedByte = b;
	}
	
}
