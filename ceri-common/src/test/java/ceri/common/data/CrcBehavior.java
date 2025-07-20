package ceri.common.data;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteArray.Immutable;

public class CrcBehavior {
	public static final CrcAlgorithm CRC16_XMODEM = CrcAlgorithm.of(16, 0x1021);
	public static final CrcAlgorithm CRC8_SMBUS = CrcAlgorithm.of(8, 0x07);

	@Test
	public void shouldVerifyCrc() {
		CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).verify(0x31c3);
		CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).verify((short) 0x31c3);
		CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).verify(0xf4);
		CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).verify((byte) 0xf4);
		assertThrown(() -> CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).verify(0x31c2));
		assertThrown(() -> CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).verify(0xf5));
	}

	@Test
	public void shouldDetermineIfCrcIsValid() {
		assertTrue(CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).isValid(0x31c3));
		assertTrue(CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).isValid(0xf4));
		assertFalse(CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).isValid(0x31c2));
		assertFalse(CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).isValid(0xf5));
	}

	@Test
	public void shouldAddBytes() {
		CrcAlgorithm ca = CrcAlgorithm.of(8, 1);
		assertEquals(ca.start().add(1, 2).crcByte(), (byte) 0x3);
		assertEquals(ca.start().add(ArrayUtil.bytes.of(1, 2)).crcByte(), (byte) 0x3);
		assertEquals(ca.start().add(ArrayUtil.bytes.of(0, 1, 2), 1).crcByte(), (byte) 0x3);
		ByteProvider.Reader<?> r = Immutable.wrap(1, 2).reader(0);
		assertEquals(ca.start().add(r, 2).crcByte(), (byte) 0x3);
	}

	@Test
	public void shouldProvideCastCrcValues() {
		CrcAlgorithm ca = CrcAlgorithm.of(64, 1);
		assertEquals(ca.start().add(1, 2, 3, 4, 5).crc(), 0x102030405L);
		assertEquals(ca.start().add(1, 2, 3, 4, 5).crcByte(), (byte) 0x5);
		assertEquals(ca.start().add(1, 2, 3, 4, 5).crcShort(), (short) 0x405);
		assertEquals(ca.start().add(1, 2, 3, 4, 5).crcInt(), 0x2030405);
	}

}
