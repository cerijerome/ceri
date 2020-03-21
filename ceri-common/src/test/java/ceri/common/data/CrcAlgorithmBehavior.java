package ceri.common.data;

import static ceri.common.data.CrcBehavior.CRC16_XMODEM;
import static ceri.common.data.CrcBehavior.CRC8_SMBUS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class CrcAlgorithmBehavior {

	@Test
	public void shouldGenerateCheckValue() {
		assertThat(CRC16_XMODEM.check(), is(0x31c3L));
		assertThat(CRC8_SMBUS.check(), is(0xf4L));
	}

	@Test
	public void shouldProvideCheckValue() {
		assertThat(CrcAlgorithm.Std.none.check(), is(0L));
		assertThat(CrcAlgorithm.Std.crc8Smbus.check(), is(0xf4L));
		assertThat(CrcAlgorithm.Std.crc16Ibm3740.check(), is(0x29b1L));
		assertThat(CrcAlgorithm.Std.crc16Kermit.check(), is(0x2189L));
		assertThat(CrcAlgorithm.Std.crc16Xmodem.check(), is(0x31c3L));
		assertThat(CrcAlgorithm.Std.crc24Ble.check(), is(0xc25a56L));
		assertThat(CrcAlgorithm.Std.crc32Bzip2.check(), is(0xfc891918L));
		assertThat(CrcAlgorithm.Std.crc32Cksum.check(), is(0x765e7680L));
		assertThat(CrcAlgorithm.Std.crc32IsoHdlc.check(), is(0xcbf43926L));
		assertThat(CrcAlgorithm.Std.crc32Mpeg2.check(), is(0x0376e6e7L));
		assertThat(CrcAlgorithm.Std.crc64GoIso.check(), is(0xb90956c775a41001L));
		assertThat(CrcAlgorithm.Std.crc64Xz.check(), is(0x995dc9bbdf1939faL));
	}

	@Test
	public void shouldStartCrc() {
		assertThat(CrcAlgorithm.Std.none.start().crc(), is(0L));
		assertThat(CrcAlgorithm.Std.crc8Smbus.start().crc(), is(0L));
		assertThat(CrcAlgorithm.Std.crc16Xmodem.start().crc(), is(0L));
	}

	@Test
	public void shouldFindByName() {
		assertNull(CrcAlgorithm.Std.from("CRC8"));
		assertThat(CrcAlgorithm.Std.from("CRC-8"), is(CrcAlgorithm.Std.crc8Smbus));
		assertThat(CrcAlgorithm.Std.from("xmodem"), is(CrcAlgorithm.Std.crc16Xmodem));
	}

}
