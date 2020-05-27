package ceri.serial.mlx90640;

import static ceri.serial.mlx90640.Mlx90640.PIXELS;

/**
 * Calibration settings extracted from EEPROM data.
 */
public class CalibrationData {
	// TODO: make immutable with builder.
	// TODO: check double vs int for fields.
	// TODO: make array fields private, access with name(i) instead.
	public static final int SUBPAGES = 2;
	public static final int RANGES = 4;
	public int kVdd; // int16_t kVdd
	public int vdd25; // int16_t vdd25
	public double kvPtat; // float KvPTAT
	public double ktPtat; // float KtPTAT
	public int vPtat25; // uint16_t vPTAT25
	public double alphaPtat; // float alphaPTAT
	public int offsetAverage; //
	public final int[] pixOsRef = new int[PIXELS]; // int16_t offset[768]
	public final double[] ilChessC = new double[3]; // float ilChessC[3]
	public final double[] a = new double[PIXELS]; // uint16_t alpha[768]
	public final double[] kv = new double[PIXELS]; // int8_t kv[768]
	public final double[] kta = new double[PIXELS]; // int8_t kta[768]
	public int gain; // int16_t gainEE
	public double ksTa; // float KsTa
	public final int[] ct = new int[RANGES]; // int16_t ct[5]
	public final double[] ksTo = new double[RANGES]; // float ksTo[5]
	public final double[] alphaCorrRange = new double[RANGES]; // ?
	public final double[] aCpSubpage = new double[SUBPAGES]; // float cpAlpha[2]
	public final int[] offCpSubpage = new int[SUBPAGES]; // int16_t cpOffset[2]
	public double kvCp; // float cpKv
	public double ktaCp; // float cpKta
	public double tgc; // float tgc
	public int resolutionEe; // uint8_t resolutionEE

	/**
	 * Datasheet 11.2.2.9.1.3.
	 */
	public int range(double to) {
		for (int i = 1; i < ct.length; i++)
			if (to < ct[i]) return i - 1;
		return ct.length - 1;
	}

	/**
	 * Make sure object temperature is not below minimum range.
	 */
	public double limit(double to) {
		return Math.max(to, ct[0]);
	}

}
