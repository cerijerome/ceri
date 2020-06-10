package ceri.serial.mlx90640.data;

import static ceri.common.math.MathUtil.average;
import static ceri.common.math.MathUtil.median;
import static ceri.common.validation.ValidationUtil.validateMinL;
import static ceri.serial.mlx90640.Mlx90640.COLUMNS;
import static ceri.serial.mlx90640.Mlx90640.PIXELS;
import static ceri.serial.mlx90640.Mlx90640.RAM_AUX;
import static ceri.serial.mlx90640.Mlx90640.RAM_START;
import static ceri.serial.mlx90640.Mlx90640.RAM_WORDS;
import static ceri.serial.mlx90640.Mlx90640.ROWS;
import static ceri.serial.mlx90640.Mlx90640.SUBPAGES;
import static ceri.serial.mlx90640.register.ReadingPattern.chess;
import ceri.serial.mlx90640.register.ControlRegister1;
import ceri.serial.mlx90640.register.ReadingPattern;

/**
 * Wrapper for RAM frame data, used to generate temperature data.
 * <p/>
 * Magic numbers taken from MLX90640 datasheet.
 */
public class RamData extends MlxBuffer {
	public static final int BYTES = RAM_WORDS * Short.BYTES;
	public static final int BAD_VALUE = 0x7fff;
	private static final double C0K = 273.15; // 0C in Kelvin
	private final CalibrationData cal;
	// Data populated each init()
	private int subPage;
	private ReadingPattern mode;
	private double vdd;
	private double ta;
	private double kGain;
	private final double[] pixGainCpSp = new double[SUBPAGES];
	private final double[] pixOsCpSp = new double[SUBPAGES];

	public static RamData of(byte[] data, CalibrationData cal) {
		validateMinL(data.length, BYTES);
		return new RamData(data, cal);
	}

	private RamData(byte[] data, CalibrationData cal) {
		super(data, 0, BYTES);
		this.cal = cal;
	}

	/**
	 * Called when new frame data has loaded.
	 */
	public void init(int subPage, ControlRegister1 control1) throws MlxDataException {
		this.subPage = subPage;
		mode = control1.pattern();
		validateFrameData();
		validateAuxData();
		calculateSupplyVoltage(control1);
		calculateAmbientTemperature();
		calculateGain();
		calculateCpGain();
		calculateCpOffsetTaVdd();
	}

	/**
	 * Reading pattern used
	 */
	public ReadingPattern mode() {
		return mode;
	}

	/**
	 * Ambient temperature of the sensor.
	 */
	public double ta() {
		return ta;
	}

	/**
	 * Approximate reflective temperature based on sensor ambient temperature.
	 */
	public double tr() {
		return cal.tr(ta);
	}

	/**
	 * Supply voltage.
	 */
	public double vdd() {
		return vdd;
	}

	/**
	 * Calculate absolute object temperature array.
	 */
	public void calculateTo(double[] values, double tr, double e) {
		double tar = tar(ta, tr, e);
		for (int p = 0; p < PIXELS; p++) {
			int pattern = pattern(p, mode);
			if (pattern != subPage) continue; // only process current sub-page
			double vIrCompensated = vIrCompensated(p, pattern, mode, e);
			double aComp = aComp(p, pattern, ta);
			double to = to(aComp, tar, vIrCompensated);
			to = cal.limit(to); // make sure >= -40C
			values[p] = toExtraRange(to, aComp, tar, vIrCompensated);
		}
	}

	/**
	 * Fill in broken/outlier pixels identified in EEPROM data.
	 */
	public void fixBadPixels(double[] values) {
		for (int px : cal.badPixels) {
			int i = px / COLUMNS;
			int j = px % COLUMNS;
			values[px] = mode == ReadingPattern.chess ? fixChessPixel(values, px, i, j) :
				fixInterleavedPixel(values, px, j);
		}
	}

	/* Frame initialization */

	/**
	 * Datasheet 11.2.2.1, 11.2.2.2.
	 */
	private void calculateSupplyVoltage(ControlRegister1 control1) {
		int resolutionReg = control1.resolution().id;
		double resolutionCorr = (double) (1 << cal.resolutionEe) / (1 << resolutionReg);
		int vddPix = value(ram(0x72a));
		vdd = (resolutionCorr * vddPix - cal.vdd25) / cal.kVdd + cal.vdd0;
	}

	/**
	 * Datasheet 11.2.2.3.
	 */
	private void calculateAmbientTemperature() {
		int vPtat = value(ram(0x720));
		int vBe = value(ram(0x700));
		double dV = vdd - cal.vdd0;
		// double dV = (double) (vddPix - cal.vdd25) / cal.kVdd; // ?
		double vPtatArt = ((long) vPtat << 18) / (vPtat * cal.alphaPtat + vBe);
		ta = (vPtatArt / (1 + cal.kvPtat * dV) - cal.vPtat25) / cal.ktPtat + cal.ta0;
	}

	/**
	 * Datasheet 11.2.2.4.
	 */
	private void calculateGain() {
		int gainRam = value(ram(0x70a));
		kGain = (double) cal.gain / gainRam;
	}

	/**
	 * Datasheet 11.2.2.6.1.
	 */
	private void calculateCpGain() {
		pixGainCpSp[0] = value(ram(0x708)) * kGain;
		pixGainCpSp[1] = value(ram(0x728)) * kGain;
		// TODO: implement moving average filter of length 16+
	}

	/**
	 * Datasheet 11.2.2.6.2.
	 */
	private void calculateCpOffsetTaVdd() {
		double multiplier = (1 + cal.ktaCp * (ta - cal.ta0)) * (1 + cal.kvCp * (vdd - cal.vdd0));
		pixOsCpSp[0] = pixGainCpSp[0] - cal.offCpSubpage(0) * multiplier;
		pixOsCpSp[1] = mode == chess ? //
			pixGainCpSp[1] - cal.offCpSubpage(1) * multiplier :
			pixGainCpSp[1] - (cal.offCpSubpage(1) + cal.ilChessC(0)) * multiplier;
	}

	/* Initialized field exposure */

	double kGain() {
		return kGain;
	}

	double pixGainCpSp(int subPage) {
		return pixGainCpSp[subPage];
	}

	double pixOsCpSp(int subPage) {
		return pixOsCpSp[subPage];
	}

	/* Calculations */

	/**
	 * Datasheet 11.1.3.1, 11.2.2.5.1, 11.2.2.5.3.
	 */
	double pixOs(int n, int pattern, ReadingPattern mode) {
		double pixGain = value(ram(0x400) + n) * kGain;
		if (mode == chess) return pixOsChess(n, pixGain, ta, vdd);
		return pixOsIl(n, pixGain, ta, vdd, pattern);
	}

	/**
	 * Datasheet 11.2.2.5.3.
	 */
	private double pixOsChess(int n, double pixGain, double ta, double vdd) {
		return pixGain - cal.pixOsRef(n) * (1 + cal.kta(n) * (ta - cal.ta0)) *
			(1 + cal.kv(n) * (vdd - cal.vdd0));
	}

	/**
	 * Datasheet 11.1.3.1.
	 */
	private double pixOsIl(int n, double pixGain, double ta, double vdd, int pattern) {
		int conversionPattern = conversionPattern(n, pattern);
		return pixOsChess(n, pixGain, ta, vdd) //
			+ cal.ilChessC(2) * (2 * pattern - 1) //
			- cal.ilChessC(1) * conversionPattern;
	}

	/**
	 * Datasheet 11.1.3.1. Interleaved conversion pattern for pixel n (0-based).
	 */
	private int conversionPattern(int n, int pattern) {
		// { 0, -1, 0, 1, .. }, { 0, 1, 0, -1, .. }, { 0, -1, 0, 1, .. }, ..
		return (((n + 2) >>> 2) - ((n + 3) >>> 2) + ((n + 1) >>> 2) - (n >>> 2)) *
			(1 - 2 * pattern);
	}

	/**
	 * Datasheet 11.2.2.5.4, 11.2.2.7.
	 */
	double vIrCompensated(int n, int pattern, ReadingPattern mode, double e) {
		double pixOs = pixOs(n, pattern, mode);
		double vIrEmissivityCompensated = pixOs / e;
		return vIrEmissivityCompensated -
			cal.tgc * ((1 - pattern) * pixOsCpSp[0] + pattern * pixOsCpSp[1]);
	}

	/**
	 * Datasheet 11.2.2.7.
	 */
	static int pattern(int n, ReadingPattern mode) {
		return mode == chess ?
			// { 0, 1, 0, 1, .. }, { 1, 0, 1, 0, .. }, { 0, 1, 0, 1, .. }, ..
			((n >>> 5) & 1) ^ (n & 1) :
			// { 0, 0, 0, 0, .. }, { 1, 1, 1, 1, .. }, { 0, 0, 0, 0, .. }, ..
			(n >>> 5) & 1;
	}

	/**
	 * Datasheet 11.2.2.8.
	 */
	double aComp(int n, int pattern, double ta) {
		return (cal.a(n) -
			cal.tgc * ((1 - pattern) * cal.aCpSubpage(0) + pattern * cal.aCpSubpage(1))) *
			(1 + cal.ksTa * (ta - cal.ta0));
	}

	/**
	 * Datasheet 11.2.2.9.
	 */
	static double tar(double ta, double tr, double e) {
		double taK4 = Math.pow(ta + C0K, 4);
		double trK4 = Math.pow(tr + C0K, 4);
		return trK4 - (trK4 - taK4) / e;
	}

	/**
	 * Datasheet 11.2.2.9.
	 */
	double sx(double aComp, double tar, double vIrCompensated) {
		double aComp3 = aComp * aComp * aComp;
		double aComp4 = aComp3 * aComp;
		return cal.ksTo(1) * Math.pow(aComp3 * vIrCompensated + aComp4 * tar, 0.25);
	}

	/**
	 * Datasheet 11.2.2.9.
	 */
	double to(double aComp, double tar, double vIrCompensated) {
		double sx = sx(aComp, tar, vIrCompensated);
		double ksTo2 = cal.ksTo(1);
		return Math.pow(vIrCompensated / (aComp * (1 - ksTo2 * C0K) + sx) + tar, 0.25) - C0K;
	}

	/**
	 * Datasheet 11.2.2.9.1.3.
	 */
	double toExtraRange(double to, double aComp, double tar, double vIrCompensated) {
		int x = cal.range(to);
		return Math.pow(
			vIrCompensated /
				(aComp * cal.alphaCorrRange(x) * (1 + cal.ksTo(x) * (to - cal.ct(x)))) + tar,
			0.25) - C0K;
	}

	/* Validation and error correction */

	private double fixChessPixel(double[] values, int px, int i, int j) {
		if (i == 0 && j == 0) return values[px + COLUMNS + 1];
		if (i == 0 && j == COLUMNS - 1) return values[px + COLUMNS - 1];
		if (i == ROWS - 1 && j == 0) return values[px - COLUMNS + 1];
		if (i == ROWS - 1 && j == COLUMNS - 1) return values[px - COLUMNS - 1];
		if (i == 0) return average(values[px + COLUMNS - 1], values[px + COLUMNS + 1]);
		if (i == ROWS - 1) return average(values[px - COLUMNS - 1], values[px - COLUMNS + 1]);
		if (j == 0) return average(values[px - COLUMNS + 1], values[px + COLUMNS + 1]);
		if (j == COLUMNS - 1) return average(values[px - COLUMNS - 1], values[px + COLUMNS - 1]);
		return median(values[px - COLUMNS - 1], values[px - COLUMNS + 1], values[px + COLUMNS - 1],
			values[px + COLUMNS + 1]);
	}

	private double fixInterleavedPixel(double[] values, int px, int j) {
		if (j == 0) return values[px + 1];
		if (j == COLUMNS - 1) return values[px - 1];
		if (j == 1 || j == COLUMNS - 2 || cal.isBadPixel(j - 2) || cal.isBadPixel(j + 2))
			return average(values[px - 1], values[px + 1]);
		double lDiff = values[px - 1] - values[px + 2];
		double rDiff = values[px + 1] - values[px + 2];
		if (Math.abs(lDiff) < Math.abs(rDiff)) return values[px - 1] + lDiff;
		return values[px + 1] + rDiff;
	}

	private void validateFrameData() throws MlxDataException {
		for (int line = 0, i = 0; i < PIXELS; i += COLUMNS, line++)
			if (line % 2 == subPage) validateData(i);
	}

	private void validateAuxData() throws MlxDataException {
		// not checked: 1-7, 19, 23, 33-39, 51, 55
		validateData(aux(0));
		validateData(aux(8), 11);
		validateData(aux(20), 3);
		validateData(aux(24), 9);
		validateData(aux(40), 11);
		validateData(aux(52), 3);
		validateData(aux(56), 8);
	}

	private void validateData(int index, int count) throws MlxDataException {
		for (int i = 0; i < count; i++, index++)
			validateData(index);
	}

	private void validateData(int index) throws MlxDataException {
		int value = value(index);
		if (value == BAD_VALUE) throw MlxDataException.of("RAM[0x%x] Bad data: 0x%x (%d)",
			RAM_START + index, (short) value, value);
	}

	/* Data access */

	/**
	 * Gets the buffer offset from RAM address.
	 */
	private static int ram(int address) {
		return address - RAM_START;
	}

	/**
	 * Gets the buffer offset from aux data index.
	 */
	private static int aux(int index) {
		return ram(RAM_AUX + index);
	}

}
