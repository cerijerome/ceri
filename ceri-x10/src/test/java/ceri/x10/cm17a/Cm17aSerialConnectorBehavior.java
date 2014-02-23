package ceri.x10.cm17a;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import org.junit.Before;
import org.junit.Test;

public class Cm17aSerialConnectorBehavior {
	private CommPortIdentifier identifier;
	private SerialPort serial;

	@Before
	public void init() throws PortInUseException {
		identifier = mock(CommPortIdentifier.class);
		serial = mock(SerialPort.class);
		when(identifier.open(anyString(), anyInt())).thenReturn(serial);
	}

	
	@Test(expected = IOException.class)
	public void shouldFailForPortInUse() throws IOException, PortInUseException {
		final CommPortIdentifier identifier = this.identifier;
		when(identifier.open(anyString(), anyInt())).thenThrow(new PortInUseException(null));
		try (Cm17aSerialConnector connector = new Cm17aSerialConnector("test", 0) {
			@Override
			CommPortIdentifier portIdentifier(String commPort) throws NoSuchPortException {
				return identifier;
			}
		}) {
			// Do nothing
		}
	}


	@Test(expected = IOException.class)
	public void shouldFailForInvalidPort() throws IOException {
		try (Cm17aSerialConnector connector = new Cm17aSerialConnector("test", 0) {
			@Override
			CommPortIdentifier portIdentifier(String commPort) throws NoSuchPortException {
				throw new NoSuchPortException("test", null);
			}
		}) {
			// Do nothing
		}
	}


	@Test
	public void shouldSendDtrAndRtsSerialSignals() throws IOException {
		final CommPortIdentifier identifier = this.identifier;
		try (Cm17aSerialConnector connector = new Cm17aSerialConnector("test", 0) {
			@Override
			CommPortIdentifier portIdentifier(String commPort) throws NoSuchPortException {
				return identifier;
			}
		}) {
			connector.setDtr(true);
			connector.setRts(false);
			verify(serial).setDTR(true);
			verify(serial).setRTS(false);
		}
	}

}
