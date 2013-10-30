package javax.comm;

public class CommPortIdentifier {
	private final gnu.io.CommPortIdentifier identifier;

	public static CommPortIdentifier getPortIdentifier(String s) throws NoSuchPortException {
		try {
			gnu.io.CommPortIdentifier identifier = gnu.io.CommPortIdentifier.getPortIdentifier(s);
			if (identifier == null) return null;
			return new CommPortIdentifier(identifier);
		} catch (gnu.io.NoSuchPortException e) {
			throw new NoSuchPortException(e);
		}
	}

	private CommPortIdentifier(gnu.io.CommPortIdentifier identifier) {
		this.identifier = identifier;
	}

	public CommPort open(String theOwner, int i) throws PortInUseException {
		try {
			gnu.io.CommPort commPort = identifier.open(theOwner, i);
			if (commPort == null) return null;
			if (commPort instanceof gnu.io.SerialPort) return new SerialPort(
				(gnu.io.SerialPort) commPort);
			throw new UnsupportedOperationException("Unsupported comm port type: " +
				commPort.getClass());
		} catch (gnu.io.PortInUseException e) {
			throw new PortInUseException(e);
		}
	}

}
