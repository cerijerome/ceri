package x10;

import java.io.IOException;
import org.junit.Test;
import ceri.common.util.BasicUtil;

/**
 * Manual tests for MAC OS X with usb to rs232 adapter
 */
public class CM11ASerialControllerTest {
	private static final String COMM_PORT = "/dev/cu.usbserial";
	
	@Test
	public void testSequence() throws IOException {
		try (CM11ASerialController x10 = new CM11ASerialController(COMM_PORT)) {
			for (int i = 0; i < 3; i++) {
				x10.addCommand(new Command("K6", Command.ON));
				BasicUtil.delay(1000);
				x10.addCommand(new Command("K6", Command.OFF));
				BasicUtil.delay(1000);
			}
		}
	}

	@Test
	public void testSendToTwoHouseCodes() throws IOException {
		try (CM11ASerialController cm11a = new CM11ASerialController(COMM_PORT)) {
			cm11a.addCommand(new Command("A1", Command.ALL_LIGHTS_ON));
			cm11a.addCommand(new Command("B2", Command.ON));
			cm11a.addCommand(new Command("A1", Command.ALL_UNITS_OFF));
			cm11a.addCommand(new Command("B1", Command.ALL_UNITS_OFF));
		}
	}
	
	@Test
	public void testSendOnOff() throws IOException {
		try (CM11ASerialController cm11a = new CM11ASerialController(COMM_PORT)) {
			cm11a.addCommand(new Command("A1", Command.OFF));
			cm11a.addCommand(new Command("A1", Command.ON));
			cm11a.addCommand(new Command("A1", Command.OFF));

		}
	}
	
	@Test
	public void testSendAllUnitsOff() throws IOException {
		try (CM11ASerialController cm11a = new CM11ASerialController(COMM_PORT)) {
			cm11a.addCommand(new Command("A1", Command.OFF));
			cm11a.addCommand(new Command("A1", Command.ON));
			cm11a.addCommand(new Command("A2", Command.ALL_UNITS_OFF));
		}
	}
	
}
