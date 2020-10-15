package ceri.common.data;

import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
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
		assertThat(CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).isValid(0x31c3), is(true));
		assertThat(CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).isValid(0xf4), is(true));
		assertThat(CRC16_XMODEM.start().add(CrcAlgorithm.checkBytes).isValid(0x31c2), is(false));
		assertThat(CRC8_SMBUS.start().add(CrcAlgorithm.checkBytes).isValid(0xf5), is(false));
	}

	@Test
	public void shouldAddBytes() {
		CrcAlgorithm ca = CrcAlgorithm.of(8, 1);
		assertThat(ca.start().add(1, 2).crcByte(), is((byte) 0x3));
		assertThat(ca.start().add(ArrayUtil.bytes(1, 2)).crcByte(), is((byte) 0x3));
		assertThat(ca.start().add(ArrayUtil.bytes(0, 1, 2), 1).crcByte(), is((byte) 0x3));
		ByteProvider.Reader r = Immutable.wrap(1, 2).reader(0);
		assertThat(ca.start().add(r, 2).crcByte(), is((byte) 0x3));
	}

	@Test
	public void shouldProvideCastCrcValues() {
		CrcAlgorithm ca = CrcAlgorithm.of(64, 1);
		assertThat(ca.start().add(1, 2, 3, 4, 5).crc(), is(0x102030405L));
		assertThat(ca.start().add(1, 2, 3, 4, 5).crcByte(), is((byte) 0x5));
		assertThat(ca.start().add(1, 2, 3, 4, 5).crcShort(), is((short) 0x405));
		assertThat(ca.start().add(1, 2, 3, 4, 5).crcInt(), is(0x2030405));
	}

}
