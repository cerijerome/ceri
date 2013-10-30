package x10;

import x10.CM11ASerialController;
import x10.Command;
import ceri.common.io.IoUtil;

public class Cm11aTest {

	public static void main(String[] args) throws Exception {
		System.setProperty("DEBUG", "true");
		try (CM11ASerialController cm11a = new CM11ASerialController("/dev/cu.usbserial")) {
			while (true) {
				System.out.println("A1 on");
				cm11a.addCommand(new Command("A1", Command.ON));
				System.out.println("Sleeping");
				Thread.sleep(1000);
				System.out.println("A1 off");
				cm11a.addCommand(new Command("A1", Command.OFF));
				Thread.sleep(1000);
				if (IoUtil.getChar() == 'x') break;
			}
		}
		System.out.println("Done");
	}
}
