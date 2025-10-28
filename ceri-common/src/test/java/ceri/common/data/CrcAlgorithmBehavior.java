package ceri.common.data;

import static ceri.common.data.CrcBehavior.CRC16_XMODEM;
import static ceri.common.data.CrcBehavior.CRC8_SMBUS;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

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
		Testing.exerciseEquals(t, eq0, eq1, eq2);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7);
	}

	@Test
	public void shouldDetermineStorageBytes() {
		Assert.equal(CrcAlgorithm.of(3, 0).bytes(), 1);
		Assert.equal(CrcAlgorithm.of(9, 0).bytes(), 2);
		Assert.equal(CrcAlgorithm.of(32, 0).bytes(), 4);
		Assert.equal(CrcAlgorithm.of(33, 0).bytes(), 5);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.equal(CrcAlgorithm.of(8, 0x3, 0x1, true, false, 0xff).toString(),
			"CRC-8[0x3,0x1,T,F,0xff]");
	}

	@Test
	public void shouldGenerateCheckValue() {
		Assert.equal(CRC16_XMODEM.check(), 0x31c3L);
		Assert.equal(CRC8_SMBUS.check(), 0xf4L);
	}

	@Test
	public void shouldProvideCheckValue() {
		Assert.equal(CrcAlgorithm.Std.none.check(), 0L);
		Assert.equal(CrcAlgorithm.Std.crc8Smbus.check(), 0xf4L);
		Assert.equal(CrcAlgorithm.Std.crc16Ibm3740.check(), 0x29b1L);
		Assert.equal(CrcAlgorithm.Std.crc16Kermit.check(), 0x2189L);
		Assert.equal(CrcAlgorithm.Std.crc16Xmodem.check(), 0x31c3L);
		Assert.equal(CrcAlgorithm.Std.crc24Ble.check(), 0xc25a56L);
		Assert.equal(CrcAlgorithm.Std.crc32Bzip2.check(), 0xfc891918L);
		Assert.equal(CrcAlgorithm.Std.crc32Cksum.check(), 0x765e7680L);
		Assert.equal(CrcAlgorithm.Std.crc32IsoHdlc.check(), 0xcbf43926L);
		Assert.equal(CrcAlgorithm.Std.crc32Mpeg2.check(), 0x0376e6e7L);
		Assert.equal(CrcAlgorithm.Std.crc64GoIso.check(), 0xb90956c775a41001L);
		Assert.equal(CrcAlgorithm.Std.crc64Xz.check(), 0x995dc9bbdf1939faL);
	}

	@Test
	public void shouldStartCrc() {
		Assert.equal(CrcAlgorithm.Std.none.start().crc(), 0L);
		Assert.equal(CrcAlgorithm.Std.crc8Smbus.start().crc(), 0L);
		Assert.equal(CrcAlgorithm.Std.crc16Xmodem.start().crc(), 0L);
	}

	@Test
	public void shouldFindByName() {
		Assert.isNull(CrcAlgorithm.Std.from("CRC8"));
		Assert.equal(CrcAlgorithm.Std.from("CRC-8"), CrcAlgorithm.Std.crc8Smbus);
		Assert.equal(CrcAlgorithm.Std.from("xmodem"), CrcAlgorithm.Std.crc16Xmodem);
	}

}
