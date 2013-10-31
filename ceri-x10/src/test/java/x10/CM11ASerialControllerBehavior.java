package x10;

import java.io.IOException;
import org.junit.Test;

/**
 * Tests for MAC OS X with 
 */
public class CM11ASerialControllerBehavior {
	private final String COMM_PORT = "/dev/cu.usbserial";
	
	@Test
	public void shouldSendToTwoHouseCodes() throws IOException {
		try (CM11ASerialController cm11a = new CM11ASerialController(COMM_PORT)) {
			cm11a.addCommand(new Command("A1", Command.ALL_LIGHTS_ON));
			cm11a.addCommand(new Command("B2", Command.ON));
			cm11a.addCommand(new Command("A1", Command.ALL_UNITS_OFF));
			cm11a.addCommand(new Command("B1", Command.ALL_UNITS_OFF));
		}
	}
	
	@Test
	public void shouldSendOnOff() throws IOException {
		try (CM11ASerialController cm11a = new CM11ASerialController(COMM_PORT)) {
			cm11a.addCommand(new Command("A1", Command.OFF));
			cm11a.addCommand(new Command("A1", Command.ON));
			cm11a.addCommand(new Command("A1", Command.OFF));

		}
	}
	
	@Test
	public void shouldSendAllUnitsOff() throws IOException {
		try (CM11ASerialController cm11a = new CM11ASerialController(COMM_PORT)) {
			cm11a.addCommand(new Command("A1", Command.OFF));
			cm11a.addCommand(new Command("A1", Command.ON));
			cm11a.addCommand(new Command("A2", Command.ALL_UNITS_OFF));
		}
	}
	
}
