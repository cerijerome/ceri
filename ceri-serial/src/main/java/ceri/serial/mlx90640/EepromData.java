package ceri.serial.mlx90640;

import static ceri.serial.mlx90640.Mlx90640.COLUMNS;
import static ceri.serial.mlx90640.Mlx90640.ROWS;

/**
 * Wrapper for EEPROM data, used to restore calibration data.
 * <p/>
 * Magic numbers taken from MLX90640 datasheet. For powers of 2, use 1L << N if N could be >= 31.
 */
public class EepromData extends MlxBuffer {
	public static final int ADDRESS = 0x2400;
	public static final int WORDS = 0x340;

	/**
	 * Returns true if address is an EEPROM register.
	 */
	public static boolean isEeprom(int register) {
		return register >= ADDRESS && register < ADDRESS + WORDS;
	}

	static EepromData of(byte[] data) {
		return new EepromData(data);
	}

	private EepromData(byte[] data) {
		super(data, 0, WORDS * Short.BYTES);
	}

	/**
	 * Extracts calibration data from EEPROM data.
	 */
	public CalibrationData restoreCalibrationData() {
		CalibrationData.Builder cal = CalibrationData.builder();
		restoreVddSensorParameters(cal); // datasheet 11.1.1
		restoreTaSensorParameters(cal); // datasheet 11.1.2 (partial)
		restoreOffset(cal); // datasheet 11.1.3
		restoreOffsetForInterleavedPattern(cal); // datasheet 11.1.3.1 (partial)
		restoreSensitivityAlpha(cal); // datasheet 11.1.4
		restoreKvCoefficient(cal); // datasheet 11.1.5
		restoreKtaCoefficient(cal); // datasheet 11.1.6
		restoreGainCoefficient(cal); // datasheet 11.1.7
		restoreKsTaCoefficient(cal); // datasheet 11.1.8
		restoreCornerTemperatures(cal); // datasheet 11.1.9
		restoreKsToCoefficient(cal); // datasheet 11.1.10
		restoreSensitivityCorrectionCoefficients(cal); // datasheet 11.1.11
		restoreSensitivityAlphaCp(cal); // datasheet 11.1.12
		restoreCompensationPixelOffset(cal); // datasheet 11.1.13
		restoreKvCpCoefficient(cal); // datasheet 11.1.14
		restoreKtaCpCoefficient(cal); // datasheet 11.1.15
		restoreTgcCoefficient(cal); // datasheet 11.1.16
		restoreResolutionControlCoefficient(cal); // datasheet 11.1.17
		return cal.build();
	}

	/**
	 * Datasheet 11.1.1, 11.2.2.2.
	 */
	private void restoreVddSensorParameters(CalibrationData.Builder cal) {
		cal.kVdd = bits(ee(0x2433), 8, 8) << 5;
		cal.vdd25 = ((ubits(ee(0x2433), 0, 8) - 256) << 5) - (1 << 13);
	}

	/**
	 * Datasheet 11.1.2 (partial), 11.2.2.3: except ta, dV, vPtatArt, vPtat, vBe (from RAM).
	 */
	private void restoreTaSensorParameters(CalibrationData.Builder cal) {
		cal.kvPtat = (double) bits(ee(0x2432), 10, 6) / (1 << 12);
		cal.ktPtat = (double) bits(ee(0x2432), 0, 10) / (1 << 3);
		cal.vPtat25 = value(ee(0x2431));
		cal.alphaPtat = (double) ubits(ee(0x2410), 12, 4) / 4 + 8;
	}

	/**
	 * Datasheet 11.1.3, 11.1.3.1, 11.2.2.5.2, 11.2.2.5.3.
	 */
	private void restoreOffset(CalibrationData.Builder cal) {
		int offsetAverage = value(ee(0x2411));
		int occScaleRow = ubits(ee(0x2410), 8, 4);
		int occScaleColumn = ubits(ee(0x2410), 4, 4);
		int occScaleRemnant = ubits(ee(0x2410), 0, 4);

		int[] occRow = new int[ROWS];
		for (int i = 0; i < occRow.length; i++)
			occRow[i] = bitsIndex(ee(0x2412), i, 4);

		int[] occColumn = new int[COLUMNS];
		for (int i = 0; i < occColumn.length; i++)
			occColumn[i] = bitsIndex(ee(0x2418), i, 4);

		for (int p = 0, i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++, p++) {
				int offset = bits(ee(0x2440) + p, 10, 6);
				cal.pixOsRef[p] = offsetAverage //
					+ (occRow[i] << occScaleRow) //
					+ (occColumn[j] << occScaleColumn) //
					+ (offset << occScaleRemnant);
			}
		}
	}

	/**
	 * Datasheet 11.1.3.1, 11.2.2.6.2.
	 */
	private void restoreOffsetForInterleavedPattern(CalibrationData.Builder cal) {
		cal.ilChessC[0] = (double) bits(ee(0x2435), 0, 6) / (1 << 4);
		cal.ilChessC[1] = (double) bits(ee(0x2435), 6, 5) / 2;
		cal.ilChessC[2] = (double) bits(ee(0x2435), 11, 5) / (1 << 3);
	}

	/**
	 * Datasheet 11.1.4, 11.2.2.8.
	 */
	private void restoreSensitivityAlpha(CalibrationData.Builder cal) {
		int aReference = uvalue(ee(0x2421));
		int aScale = ubits(ee(0x2420), 12, 4) + 30;
		int accScaleRow = ubits(ee(0x2420), 8, 4);
		int accScaleColumn = ubits(ee(0x2420), 4, 4);
		int accScaleRemnant = ubits(ee(0x2420), 0, 4);

		int[] accRow = new int[ROWS];
		for (int i = 0; i < accRow.length; i++)
			accRow[i] = bitsIndex(ee(0x2422), i, 4);

		int[] accColumn = new int[COLUMNS];
		for (int i = 0; i < accColumn.length; i++)
			accColumn[i] = bitsIndex(ee(0x2428), i, 4);

		for (int p = 0, i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++, p++) {
				int aPixel = bits(ee(0x2440) + p, 4, 6); // unsigned instead?
				cal.a[p] = (double) (aReference //
					+ (accRow[i] << accScaleRow) //
					+ (accColumn[j] << accScaleColumn) //
					+ (aPixel << accScaleRemnant)) / (1L << aScale);
			}
		}
	}

	/**
	 * Datasheet 11.1.3.1, 11.1.5, 11.2.2.5.3.
	 */
	private void restoreKvCoefficient(CalibrationData.Builder cal) {
		int kvScale = ubits(ee(0x2438), 8, 4);
		int[] kv = new int[4];
		kv[0] = bits(ee(0x2434), 12, 4);
		kv[1] = bits(ee(0x2434), 4, 4);
		kv[2] = bits(ee(0x2434), 8, 4);
		kv[3] = bits(ee(0x2434), 0, 4);

		for (int p = 0, i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++, p++) {
				int k = 2 * (i % 2) + (j % 2);
				cal.kv[p] = (double) kv[k] / (1L << kvScale);
			}
		}
	}

	/**
	 * Datasheet 11.1.3.1, 11.1.6, 11.2.2.5.3.
	 */
	private void restoreKtaCoefficient(CalibrationData.Builder cal) {
		int ktaScale1 = ubits(ee(0x2438), 4, 4) + 8;
		int ktaScale2 = ubits(ee(0x2438), 0, 4);
		int[] ktaRcEe = new int[4];
		ktaRcEe[0] = bits(ee(0x2436), 8, 8);
		ktaRcEe[1] = bits(ee(0x2437), 8, 8);
		ktaRcEe[2] = bits(ee(0x2436), 0, 8);
		ktaRcEe[3] = bits(ee(0x2437), 0, 8);

		for (int p = 0, i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++, p++) {
				int k = 2 * (i % 2) + (j % 2);
				int ktaEe = bits(ee(0x2440) + p, 1, 3);
				cal.kta[p] = (double) (ktaRcEe[k] + (ktaEe << ktaScale2)) / (1L << ktaScale1);
			}
		}
	}

	/**
	 * Datasheet 11.1.7, 11.2.2.4.
	 */
	private void restoreGainCoefficient(CalibrationData.Builder cal) {
		cal.gain = value(ee(0x2430));
	}

	/**
	 * Datasheet 11.1.8, 11.2.2.8.
	 */
	private void restoreKsTaCoefficient(CalibrationData.Builder cal) {
		cal.ksTa = (double) bits(ee(0x243c), 8, 8) / (1 << 13);
	}

	/**
	 * Datasheet 11.1.9, 11.1.11, 11.2.2.9.1, 11.2.2.9.1.1, 11.2.2.9.1.2, 11.2.2.9.1.3.
	 * 
	 * <pre>
	 * Ranges: ct[0] < ct[1] < ct[2] < ct[3] < 
	 *         -40C     0C      CT3     CT4
	 * </pre>
	 */
	private void restoreCornerTemperatures(CalibrationData.Builder cal) {
		int step = ubits(ee(0x243f), 12, 2) * 10;
		cal.ct[0] = -40;
		cal.ct[1] = 0;
		cal.ct[2] = ubits(ee(0x243f), 4, 4) * step;
		cal.ct[3] = ubits(ee(0x243f), 8, 4) * step + cal.ct[2];
	}

	/**
	 * Datasheet 11.1.10, 11.1.11, 11.2.2.9, 11.2.2.9.1, 11.2.2.9.1.2, 11.2.2.9.1.3.
	 */
	private void restoreKsToCoefficient(CalibrationData.Builder cal) {
		long ksToScale = 1L << (ubits(ee(0x243f), 0, 4) + 8);
		cal.ksTo[0] = (double) bits(ee(0x243d), 0, 8) / ksToScale;
		cal.ksTo[1] = (double) bits(ee(0x243d), 8, 8) / ksToScale;
		cal.ksTo[2] = (double) bits(ee(0x243e), 0, 8) / ksToScale;
		cal.ksTo[3] = (double) bits(ee(0x243e), 8, 8) / ksToScale;
	}

	/**
	 * Datasheet 11.1.11, 11.2.2.9.1.2, 11.2.2.9.1.3.
	 */
	private void restoreSensitivityCorrectionCoefficients(CalibrationData.Builder cal) {
		cal.alphaCorrRange[0] = 1 / (1 + cal.ksTo[0] * (cal.ct[1] - cal.ct[0]));
		cal.alphaCorrRange[1] = 1;
		cal.alphaCorrRange[2] = 1 + cal.ksTo[1] * (cal.ct[2] - cal.ct[1]);
		cal.alphaCorrRange[3] = cal.alphaCorrRange[2] * (1 + cal.ksTo[2] * (cal.ct[3] - cal.ct[2]));
	}

	/**
	 * Datasheet 11.1.12, 11.2.2.8.
	 */
	private void restoreSensitivityAlphaCp(CalibrationData.Builder cal) {
		int aScaleCp = ubits(ee(0x2420), 12, 4) + 27;
		double cpP1P0Ratio = bits(ee(0x2439), 10, 6);
		cal.aCpSubpage[0] = (double) ubits(ee(0x2439), 0, 10) / (1L << aScaleCp);
		cal.aCpSubpage[1] = cal.aCpSubpage[0] * (1 + cpP1P0Ratio / (1 << 7));
	}

	/**
	 * Datasheet 11.1.13, 11.2.2.6.2.
	 */
	private void restoreCompensationPixelOffset(CalibrationData.Builder cal) {
		cal.offCpSubpage[0] = bits(ee(0x243a), 0, 10);
		cal.offCpSubpage[1] = cal.offCpSubpage[0] + bits(ee(0x243a), 10, 6);
	}

	/**
	 * Datasheet 11.1.14, 11.2.2.6.2.
	 */
	private void restoreKvCpCoefficient(CalibrationData.Builder cal) {
		int kvScale = ubits(ee(0x2438), 8, 4);
		cal.kvCp = (double) bits(ee(0x243b), 8, 8) / (1L << kvScale);
	}

	/**
	 * Datasheet 11.1.15, 11.2.2.6.2.
	 */
	private void restoreKtaCpCoefficient(CalibrationData.Builder cal) {
		int ktaScale1 = ubits(ee(0x2438), 4, 4) + 8;
		cal.ktaCp = (double) bits(ee(0x243b), 0, 8) / (1L << ktaScale1);
	}

	/**
	 * Datasheet 11.1.16, 11.2.2.7, 11.2.2.8.
	 * <p/>
	 * Note1: for MLX90640ESF–BAx–000-TU devices, TGC coefficient is 0 and must not be changed.
	 * <p/>
	 * Note2: for MLX90640ESF–BCx–000-TU devices, EEPROM contains a typical TGC coefficient; user
	 * may choose to adjust the value for a specific application. Using the TGC increases noise in
	 * the temperature calculations, which can be reduced by external averaging of the CP sensor
	 * data. A TGC coefficient of 0 bypasses gradient compensation.
	 */
	private void restoreTgcCoefficient(CalibrationData.Builder cal) {
		cal.tgc = (double) bits(ee(0x243c), 0, 8) / (1 << 5);
	}

	/**
	 * Datasheet 11.1.17, 11.2.2.1.
	 */
	private void restoreResolutionControlCoefficient(CalibrationData.Builder cal) {
		cal.resolutionEe = ubits(ee(0x2438), 12, 2);
	}

	/**
	 * Gets the buffer offset from EEPROM address.
	 */
	private static int ee(int address) {
		return address - ADDRESS;
	}

}
