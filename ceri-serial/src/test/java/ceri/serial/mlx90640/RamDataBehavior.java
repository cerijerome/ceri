package ceri.serial.mlx90640;

import static ceri.serial.mlx90640.Mlx90640.COLUMNS;
import static ceri.serial.mlx90640.ReadingPattern.chess;
import static ceri.serial.mlx90640.ReadingPattern.interleaved;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;

public class RamDataBehavior {
	private static CalibrationData cal;
	private static RamData frame;
	private static final int px = Mlx90640.px(12, 16);

	@BeforeClass
	public static void init() throws MlxException {
		cal = EepromDataBehavior.calibrationTestData();
		frame = RamData.of(MlxTestUtil.bytes(frameData()), cal, 3.3, 25, -8);
		frame.init(0, ControlRegister1.of(0x0901));
	}

	@Test
	public void shouldDetermineMode() {
		assertThat(frame.mode(), is(interleaved));
	}

	@Test
	public void shouldCalculateSupplyVoltage() {
		assertThat(frame.vdd(), is(3.3186237373737373));
	}

	@Test
	public void shouldCalculateAmbientTemperature() {
		// datasheet ===========> 39.18440152
		assertThat(frame.ta(), is(39.18442378914584));
	}

	@Test
	public void shouldCalculateGain() {
		assertThat(frame.kGain(), is(1.0175354694723417));
	}

	@Test
	public void shouldCalculatePixelOffset() {
		assertThat(frame.pixOs(px, 0, chess), is(700.8824956908766));
		assertThat(frame.pixOs(px, 1, chess), is(700.8824956908766));
		assertThat(frame.pixOs(px, 0, interleaved), is(697.7574956908766));
		assertThat(frame.pixOs(px, 1, interleaved), is(704.0074956908766));
	}

	@Test
	public void shouldCalculateCpGain() {
		assertThat(frame.pixGainCpSp(0), is(-54.946915351506455));
		assertThat(frame.pixGainCpSp(1), is(-56.98198629045114));
	}

	@Test
	public void shouldCalculateCpOffsetTaVdd() {
		assertThat(frame.pixOsCpSp(0), is(25.66665750599561));
		// datasheet ===================> 21.6315865670509
		assertThat(frame.pixOsCpSp(1), is(24.437722295625953));
	}

	@Test
	public void shouldCalculatePattern() {
		assertThat(RamData.pattern(px, chess), is(0));
		assertThat(RamData.pattern(px + 1, chess), is(1));
		assertThat(RamData.pattern(px + COLUMNS, chess), is(1));
		assertThat(RamData.pattern(px, interleaved), is(1));
		assertThat(RamData.pattern(px + 1, interleaved), is(1));
		assertThat(RamData.pattern(px + COLUMNS, interleaved), is(0));
	}

	@Test
	public void shouldCalculateIrCompensation() {
		assertThat(frame.vIrCompensated(px, 0, chess, 1.0), is(675.2158381848809));
		// datasheet ========================================> 679.250909123826
		assertThat(frame.vIrCompensated(px, 1, chess, 1.0), is(676.4447733952506));
		assertThat(frame.vIrCompensated(px, 0, interleaved, 1.0), is(672.0908381848809));
		assertThat(frame.vIrCompensated(px, 1, interleaved, 1.0), is(679.5697733952506));
	}

	@Test
	public void shouldNormalizeSensitivity() {
		double ta = 39.184;
		assertThat(frame.aComp(px, 0, ta), is(1.1876487360495958E-7));
	}

	@Test
	public void shouldCalculateTar() {
		double ta = 39.184;
		double tr = 31;
		assertThat(RamData.tar(ta, tr, 1.0), is(9516495632.564133));
	}

	@Test
	public void shouldCalculateSx() {
		double aComp = 1.1876487360496E-7;
		double tar = 9516495632.56;
		double vIrCompensated = 679.250909123826;
		assertThat(frame.sx(aComp, tar, vIrCompensated), is(-3.3425938235744884E-8));
	}

	@Test
	public void shouldCalculateTo() {
		double aComp = 1.1876487360496E-7;
		double tar = 9516495632.56;
		double vIrCompensated = 679.250909123826;
		assertThat(frame.to(aComp, tar, vIrCompensated), is(80.36331250659185));
	}

	@Test
	public void shouldCalculateToExtraRange() {
		double aComp = 1.1876487360496E-7;
		double tar = 9516495632.56;
		double vIrCompensated = 679.250909123826;
		double to = 80.36331250659185;
		assertThat(frame.toExtraRange(to, aComp, tar, vIrCompensated), is(80.42781415006192));
	}

	/**
	 * Test data from datasheet.
	 */
	private static int[] frameData() {
		int[] frameData = new int[RamData.WORDS];
		frameData[ram(0x56f)] = 0x0261; // pix(12,16)
		frameData[ram(0x700)] = 0x4bf2; // Vbe
		frameData[ram(0x708)] = 0xffca; // CP subpage 0
		frameData[ram(0x70a)] = 0x1881; // GAIN
		frameData[ram(0x720)] = 0x06af; // PTAT
		frameData[ram(0x728)] = 0xffc8; // CP subpage 1
		frameData[ram(0x72a)] = 0xccc5; // VDD
		return frameData;
	}

	/**
	 * Gets the buffer offset from RAM address.
	 */
	private static int ram(int address) {
		return address - RamData.ADDRESS;
	}
}
