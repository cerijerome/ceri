package ceri.home.io.pcirlinc;

import java.io.IOException;
import java.util.Properties;
import org.junit.Test;
import ceri.common.property.PropertyUtil;

public class PcIrLincTest {

	//	public void testPreset() throws IOException, InterruptedException {
	//		PcIrLinc pcIrLinc = create();
	//		short tvVendor = 0;
	//		for (int i = 10; i > 0; i--)
	//			pcIrLinc.sendPreset(PcIrLincType.TYPE_TV, tvVendor,
	//				PcIrLincButton.BTN_VOLUME_DOWN);
	//		pcIrLinc.close();
	//	}

	@Test
	public void testLearning() throws IOException, InterruptedException {
		try (PcIrLinc pcIrLinc = create()) {
			byte[] code = pcIrLinc.learnIr(10000);
			System.out.println(PcIrLinc.bytesToHex(code));
			//String s = "00C604012919A704A401300252012D0129012B951223332332322233233200";
			//byte[] code = PcIrLinc.hexToBytes(s);
			Thread.sleep(500);
			pcIrLinc.sendLearnedIr(code, 1);
			Thread.sleep(500);
		}
	}

	private PcIrLinc create() throws IOException {
		Properties properties = PropertyUtil.load(getClass(), "test.properties");
		PcIrLincProperties pcIrLincProperties = new PcIrLincProperties(properties, "pcIrLinc");
		return new PcIrLinc(pcIrLincProperties);
	}

}
