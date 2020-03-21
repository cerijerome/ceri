package ceri.common.data;

import org.junit.Test;

public class CrcBehavior {
	public static final CrcAlgorithm CRC16_XMODEM = CrcAlgorithm.of(16, 0x1021);
	public static final CrcAlgorithm CRC8_SMBUS = CrcAlgorithm.of(8, 0x07);

	@Test
	public void shouldVerifyCrc() {
		CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).verify(0x31c3);
		CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).verify(0xf4);
	}

}
