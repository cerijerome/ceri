package x10;

import java.io.IOException;
import ceri.common.util.BasicUtil;

/**
 * Tests for MAC OS X with usb to rs232 adapter
 */
public class CM11ASerialControllerTest {
	private static final String COMM_PORT = "/dev/cu.usbserial";
	
	//@Test
	public void test() throws IOException {
		System.out.println("1");
		try (CM11ASerialController x10 = new CM11ASerialController(COMM_PORT)) {
			System.out.println("2");
			for (int i = 0; i < 3; i++) {
				x10.addCommand(new Command("K6", Command.ON));
				System.out.println("3");
				BasicUtil.delay(1000);
				x10.addCommand(new Command("K6", Command.OFF));
				System.out.println("4");
				BasicUtil.delay(1000);
			}
		}
	}

	public void sendToTwoHouseCodes() throws IOException {
		try (CM11ASerialController cm11a = new CM11ASerialController(COMM_PORT)) {
			cm11a.addCommand(new Command("A1", Command.ALL_LIGHTS_ON));
			cm11a.addCommand(new Command("B2", Command.ON));
			cm11a.addCommand(new Command("A1", Command.ALL_UNITS_OFF));
			cm11a.addCommand(new Command("B1", Command.ALL_UNITS_OFF));
		}
	}
	
	public void sendOnOff() throws IOException {
		try (CM11ASerialController cm11a = new CM11ASerialController(COMM_PORT)) {
			cm11a.addCommand(new Command("A1", Command.OFF));
			cm11a.addCommand(new Command("A1", Command.ON));
			cm11a.addCommand(new Command("A1", Command.OFF));

		}
	}
	
	public void sendAllUnitsOff() throws IOException {
		try (CM11ASerialController cm11a = new CM11ASerialController(COMM_PORT)) {
			cm11a.addCommand(new Command("A1", Command.OFF));
			cm11a.addCommand(new Command("A1", Command.ON));
			cm11a.addCommand(new Command("A2", Command.ALL_UNITS_OFF));
		}
	}
	
}
