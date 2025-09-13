package ceri.common.data;

import static ceri.common.data.CrcBehavior.CRC16_XMODEM;
import static ceri.common.data.CrcBehavior.CRC8_SMBUS;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import org.junit.Test;
import ceri.common.test.TestUtil;

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
		TestUtil.exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7);
	}

	@Test
	public void shouldDetermineStorageBytes() {
		assertEquals(CrcAlgorithm.of(3, 0).bytes(), 1);
		assertEquals(CrcAlgorithm.of(9, 0).bytes(), 2);
		assertEquals(CrcAlgorithm.of(32, 0).bytes(), 4);
		assertEquals(CrcAlgorithm.of(33, 0).bytes(), 5);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertEquals(CrcAlgorithm.of(8, 0x3, 0x1, true, false, 0xff).toString(),
			"CRC-8[0x3,0x1,T,F,0xff]");
	}

	@Test
	public void shouldGenerateCheckValue() {
		assertEquals(CRC16_XMODEM.check(), 0x31c3L);
		assertEquals(CRC8_SMBUS.check(), 0xf4L);
	}

	@Test
	public void shouldProvideCheckValue() {
		assertEquals(CrcAlgorithm.Std.none.check(), 0L);
		assertEquals(CrcAlgorithm.Std.crc8Smbus.check(), 0xf4L);
		assertEquals(CrcAlgorithm.Std.crc16Ibm3740.check(), 0x29b1L);
		assertEquals(CrcAlgorithm.Std.crc16Kermit.check(), 0x2189L);
		assertEquals(CrcAlgorithm.Std.crc16Xmodem.check(), 0x31c3L);
		assertEquals(CrcAlgorithm.Std.crc24Ble.check(), 0xc25a56L);
		assertEquals(CrcAlgorithm.Std.crc32Bzip2.check(), 0xfc891918L);
		assertEquals(CrcAlgorithm.Std.crc32Cksum.check(), 0x765e7680L);
		assertEquals(CrcAlgorithm.Std.crc32IsoHdlc.check(), 0xcbf43926L);
		assertEquals(CrcAlgorithm.Std.crc32Mpeg2.check(), 0x0376e6e7L);
		assertEquals(CrcAlgorithm.Std.crc64GoIso.check(), 0xb90956c775a41001L);
		assertEquals(CrcAlgorithm.Std.crc64Xz.check(), 0x995dc9bbdf1939faL);
	}

	@Test
	public void shouldStartCrc() {
		assertEquals(CrcAlgorithm.Std.none.start().crc(), 0L);
		assertEquals(CrcAlgorithm.Std.crc8Smbus.start().crc(), 0L);
		assertEquals(CrcAlgorithm.Std.crc16Xmodem.start().crc(), 0L);
	}

	@Test
	public void shouldFindByName() {
		assertNull(CrcAlgorithm.Std.from("CRC8"));
		assertEquals(CrcAlgorithm.Std.from("CRC-8"), CrcAlgorithm.Std.crc8Smbus);
		assertEquals(CrcAlgorithm.Std.from("xmodem"), CrcAlgorithm.Std.crc16Xmodem);
	}

}
