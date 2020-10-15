package ceri.common.data;

import static ceri.common.data.CrcBehavior.CRC16_XMODEM;
import static ceri.common.data.CrcBehavior.CRC8_SMBUS;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class CrcAlgorithmBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		CrcAlgorithm t = CrcAlgorithm.Std.crc8Smbus.algorithm();
		CrcAlgorithm eq0 = CrcAlgorithm.Std.crc8Smbus.algorithm();
		CrcAlgorithm eq1 = CrcAlgorithm.of(8, 0x7);
		CrcAlgorithm eq2 = CrcAlgorithm.builder(8).powers(0, 1, 2).build();
		CrcAlgorithm ne0 = CrcAlgorithm.Std.none.algorithm();
		CrcAlgorithm ne1 = CrcAlgorithm.Std.crc16Xmodem.algorithm();
		CrcAlgorithm ne2 = CrcAlgorithm.of(7, 0x7);
		CrcAlgorithm ne3 = CrcAlgorithm.of(8, 0x3);
		CrcAlgorithm ne4 = CrcAlgorithm.builder(8).poly(0x7).init(1).build();
		CrcAlgorithm ne5 = CrcAlgorithm.builder(8).poly(0x7).ref(true, false).build();
		CrcAlgorithm ne6 = CrcAlgorithm.builder(8).poly(0x7).ref(false, true).build();
		CrcAlgorithm ne7 = CrcAlgorithm.builder(8).poly(0x7).xorOut(0xff).build();
		exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7);
	}

	@Test
	public void shouldDetermineStorageBytes() {
		assertThat(CrcAlgorithm.of(3, 0).bytes(), is(1));
		assertThat(CrcAlgorithm.of(9, 0).bytes(), is(2));
		assertThat(CrcAlgorithm.of(32, 0).bytes(), is(4));
		assertThat(CrcAlgorithm.of(33, 0).bytes(), is(5));
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertThat(CrcAlgorithm.of(8, 0x3, 0x1, true, false, 0xff).toString(),
			is("CRC-8[0x3,0x1,T,F,0xff]"));
	}

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
