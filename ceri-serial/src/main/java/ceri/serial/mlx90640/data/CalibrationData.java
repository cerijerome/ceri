package ceri.serial.mlx90640.data;

import static ceri.common.collection.ArrayUtil.copy;
import static ceri.serial.mlx90640.Mlx90640.PIXELS;
import static ceri.serial.mlx90640.Mlx90640.SUBPAGES;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Calibration settings extracted from EEPROM data.
 */
public class CalibrationData {
	private static final int RANGES = 4;
	public final double vdd0;
	public final double ta0;
	private final double trTaOffset;
	public final int kVdd; // int16_t kVdd
	public final int vdd25; // int16_t vdd25
	public final double kvPtat; // float KvPTAT
	public final double ktPtat; // float KtPTAT
	public final int vPtat25; // uint16_t vPTAT25
	public final double alphaPtat; // float alphaPTAT
	private final int[] pixOsRef = new int[PIXELS]; // int16_t offset[768]
	private final double[] ilChessC = new double[3]; // float ilChessC[3]
	private final double[] a = new double[PIXELS]; // uint16_t alpha[768]
	private final double[] kv = new double[PIXELS]; // int8_t kv[768]
	private final double[] kta = new double[PIXELS]; // int8_t kta[768]
	public final int gain; // int16_t gainEE
	public final double ksTa; // float KsTa
	private final int[] ct = new int[RANGES]; // int16_t ct[5]
	private final double[] ksTo = new double[RANGES]; // float ksTo[5]
	private final double[] alphaCorrRange = new double[RANGES]; // ?
	private final double[] aCpSubpage = new double[SUBPAGES]; // float cpAlpha[2]
	private final int[] offCpSubpage = new int[SUBPAGES]; // int16_t cpOffset[2]
	public final double kvCp; // float cpKv
	public final double ktaCp; // float cpKta
	public final double tgc; // float tgc
	public final int resolutionEe; // uint8_t resolutionEE
	public final Set<Integer> badPixels;

	public static class Builder {
		public double vdd0 = 3.3;
		public double ta0 = 25;
		public double trTaOffset = -8;
		public int kVdd;
		public int vdd25;
		public double kvPtat;
		public double ktPtat;
		public int vPtat25;
		public double alphaPtat;
		public final int[] pixOsRef = new int[PIXELS];
		public final double[] ilChessC = new double[3];
		public final double[] a = new double[PIXELS];
		public final double[] kv = new double[PIXELS];
		public final double[] kta = new double[PIXELS];
		public int gain;
		public double ksTa;
		public final int[] ct = new int[RANGES];
		public final double[] ksTo = new double[RANGES];
		public final double[] alphaCorrRange = new double[RANGES];
		public final double[] aCpSubpage = new double[SUBPAGES];
		public final int[] offCpSubpage = new int[SUBPAGES];
		public double kvCp;
		public double ktaCp;
		public double tgc;
		public int resolutionEe;
		public final Set<Integer> broken = new TreeSet<>();
		public final Set<Integer> outliers = new TreeSet<>();

		public Builder broken(int px) {
			broken.add(px);
			return this;
		}

		public Builder outlier(int px) {
			outliers.add(px);
			return this;
		}

		public CalibrationData build() {
			return new CalibrationData(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private CalibrationData(Builder builder) {
		vdd0 = builder.vdd0;
		ta0 = builder.ta0;
		trTaOffset = builder.trTaOffset;
		kVdd = builder.kVdd;
		vdd25 = builder.vdd25;
		kvPtat = builder.kvPtat;
		ktPtat = builder.ktPtat;
		vPtat25 = builder.vPtat25;
		alphaPtat = builder.alphaPtat;
		copy(builder.pixOsRef, 0, pixOsRef, 0);
		copy(builder.ilChessC, 0, ilChessC, 0);
		copy(builder.a, 0, a, 0);
		copy(builder.kv, 0, kv, 0);
		copy(builder.kta, 0, kta, 0);
		gain = builder.gain;
		ksTa = builder.ksTa;
		copy(builder.ct, 0, ct, 0);
		copy(builder.ksTo, 0, ksTo, 0);
		copy(builder.alphaCorrRange, 0, alphaCorrRange, 0);
		copy(builder.aCpSubpage, 0, aCpSubpage, 0);
		copy(builder.offCpSubpage, 0, offCpSubpage, 0);
		kvCp = builder.kvCp;
		ktaCp = builder.ktaCp;
		tgc = builder.tgc;
		resolutionEe = builder.resolutionEe;
		Set<Integer> badPixels = new TreeSet<>();
		badPixels.addAll(builder.broken);
		badPixels.addAll(builder.outliers);
		this.badPixels = Collections.unmodifiableSet(badPixels);
	}

	/**
	 * Approximate reflective temperature based on sensor ambient temperature.
	 */
	public double tr(double ta) {
		return ta + trTaOffset;
	}

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

	/**
	 * Field access.
	 */
	public int pixOsRef(int i) {
		return pixOsRef[i];
	}

	/**
	 * Field access.
	 */
	public double ilChessC(int i) {
		return ilChessC[i];
	}

	/**
	 * Field access.
	 */
	public double a(int i) {
		return a[i];
	}

	/**
	 * Field access.
	 */
	public double kv(int i) {
		return kv[i];
	}

	/**
	 * Field access.
	 */
	public double kta(int i) {
		return kta[i];
	}

	/**
	 * Field access.
	 */
	public int ct(int i) {
		return ct[i];
	}

	/**
	 * Field access.
	 */
	public double ksTo(int i) {
		return ksTo[i];
	}

	/**
	 * Field access.
	 */
	public double alphaCorrRange(int i) {
		return alphaCorrRange[i];
	}

	/**
	 * Field access.
	 */
	public double aCpSubpage(int i) {
		return aCpSubpage[i];
	}

	/**
	 * Field access.
	 */
	public int offCpSubpage(int i) {
		return offCpSubpage[i];
	}

	/**
	 * Checks if pixel is a broken or an outlier.
	 */
	public boolean isBadPixel(int i) {
		return badPixels.contains(i);
	}

}
