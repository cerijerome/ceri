package ceri.serial.javax;

import java.io.FileDescriptor;

public class CommPortIdentifier {
	public static final int CONNECTION_TIMEOUT_MS_DEF = 3000;
	private final purejavacomm.CommPortIdentifier identifier;

	public static CommPortIdentifier getPortIdentifier(String s) throws NoSuchPortException {
		try {
			purejavacomm.CommPortIdentifier identifier =
				purejavacomm.CommPortIdentifier.getPortIdentifier(s);
			if (identifier == null) return null;
			return new CommPortIdentifier(identifier);
		} catch (purejavacomm.NoSuchPortException e) {
			throw new NoSuchPortException(s, e);
		}
	}

	private CommPortIdentifier(purejavacomm.CommPortIdentifier identifier) {
		this.identifier = identifier;
	}

	public CommPort open(String owner) throws PortInUseException {
		return open(owner, CONNECTION_TIMEOUT_MS_DEF);
	}

	public CommPort open(String owner, int timeoutMs) throws PortInUseException {
		try {
			return commPort(identifier.open(owner, timeoutMs));
		} catch (purejavacomm.PortInUseException | purejavacomm.PureJavaIllegalStateException e) {
			throw new PortInUseException(identifier.getName(), e);
		}
	}

	public String getCurrentOwner() {
		return identifier.getCurrentOwner();
	}

	public String getName() {
		return identifier.getName();
	}

	public int getPortType() {
		return identifier.getPortType();
	}

	public boolean isCurrentlyOwned() {
		return identifier.isCurrentlyOwned();
	}

	public CommPort open(FileDescriptor arg0) throws UnsupportedCommOperationException {
		try {
			return commPort(identifier.open(arg0));
		} catch (purejavacomm.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}

	private CommPort commPort(purejavacomm.CommPort commPort) {
		if (commPort == null) return null;
		if (commPort instanceof purejavacomm.SerialPort)
			return new SerialPort((purejavacomm.SerialPort) commPort);
		throw new UnsupportedOperationException(
			"Unsupported comm port type: " + commPort.getClass());
	}

}
