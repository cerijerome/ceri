package ceri.common.data;

import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.test.Assert;

public class CrcBehavior {
	public static final CrcAlgorithm CRC16_XMODEM = CrcAlgorithm.of(16, 0x1021);
	public static final CrcAlgorithm CRC8_SMBUS = CrcAlgorithm.of(8, 0x07);

	@Test
	public void shouldVerifyCrc() {
		CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).verify(0x31c3);
		CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).verify((short) 0x31c3);
		CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).verify(0xf4);
		CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).verify((byte) 0xf4);
		Assert.thrown(() -> CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).verify(0x31c2));
		Assert.thrown(() -> CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).verify(0xf5));
	}

	@Test
	public void shouldDetermineIfCrcIsValid() {
		Assert.yes(CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).isValid(0x31c3));
		Assert.yes(CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).isValid(0xf4));
		Assert.no(CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).isValid(0x31c2));
		Assert.no(CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).isValid(0xf5));
	}

	@Test
	public void shouldAddBytes() {
		CrcAlgorithm ca = CrcAlgorithm.of(8, 1);
		Assert.equal(ca.start().add(1, 2).crcByte(), (byte) 0x3);
		Assert.equal(ca.start().add(ArrayUtil.bytes.of(1, 2)).crcByte(), (byte) 0x3);
		Assert.equal(ca.start().add(ArrayUtil.bytes.of(0, 1, 2), 1).crcByte(), (byte) 0x3);
		ByteProvider.Reader<?> r = Immutable.wrap(1, 2).reader(0);
		Assert.equal(ca.start().add(r, 2).crcByte(), (byte) 0x3);
	}

	@Test
	public void shouldProvideCastCrcValues() {
		CrcAlgorithm ca = CrcAlgorithm.of(64, 1);
		Assert.equal(ca.start().add(1, 2, 3, 4, 5).crc(), 0x102030405L);
		Assert.equal(ca.start().add(1, 2, 3, 4, 5).crcByte(), (byte) 0x5);
		Assert.equal(ca.start().add(1, 2, 3, 4, 5).crcShort(), (short) 0x405);
		Assert.equal(ca.start().add(1, 2, 3, 4, 5).crcInt(), 0x2030405);
	}

}
