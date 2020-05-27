package ceri.serial.mlx90640;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
private final int kVdd; // int16_t kVdd
private final int vdd25; // int16_t vdd25
private final double KvPtat; // float KvPTAT
private final double KtPtat; // float KtPTAT
private final int vPtat25; // uint16_t vPTAT25
private final double alphaPtat; // float alphaPTAT
private final int gainEe; // int16_t gainEE
private final double tgc; // float tgc
private final double cpKv; // float cpKv
private final double cpKta; // float cpKta
private final int resolutionEe; // uint8_t resolutionEE
private final int calibrationModeEe; // uint8_t calibrationModeEE
private final double ksTa; // float KsTa
private final double[] ksTo; // float ksTo[5]
private final int[] ct; // int16_t ct[5]
private final int[] alpha; // uint16_t alpha[768]
private final int alphaScale; // uint8_t alphaScale
private final int[] offset; // int16_t offset[768]
private final byte[] kta; // int8_t kta[768]
private final int ktaScale; // uint8_t ktaScale
private final byte[] kv; // int8_t kv[768]
private final int kvScale; // uint8_t kvScale
private final double[] cpAlpha; // float cpAlpha[2]
private final int[] cpOffset; // int16_t cpOffset[2]
private final double[] ilChessC; // float ilChessC[3]
private final int[] brokenPixels; // uint16_t brokenPixels[5]
private final int[] outlierPixels; // uint16_t outlierPixels[5]
*/
public class MlxParameters {
	private final int kVdd;
	private final int vdd25;
	private final double KvPtat;
	private final double KtPtat;
	private final int vPtat25;
	private final double alphaPtat;
	private final int gainEe;
	private final double tgc;
	private final double cpKv;
	private final double cpKta;
	private final int resolutionEe;
	private final int calibrationModeEe;
	private final double ksTa;
	private final double[] ksTo;
	private final int[] ct;
	private final int[] alpha;
	private final int alphaScale;
	private final int[] offset;
	private final int[] kta;
	private final int ktaScale;
	private final int[] kv;
	private final int kvScale;
	private final double[] cpAlpha;
	private final int[] cpOffset;
	private final double[] ilChessC;
	private final int[] brokenPixels;
	private final int[] outlierPixels;

	public static class Builder {
		int kVdd;
		int vdd25;
		double KvPtat;
		double KtPtat;
		int vPtat25;
		double alphaPtat;
		int gainEe;
		double tgc;
		double cpKv;
		double cpKta;
		int resolutionEe;
		int calibrationModeEe;
		double ksTa;
		double[] ksTo;
		int[] ct;
		int[] alpha;
		int alphaScale;
		int[] offset;
		int[] kta;
		int ktaScale;
		int[] kv;
		int kvScale;
		double[] cpAlpha;
		int[] cpOffset;
		double[] ilChessC;
		int[] brokenPixels;
		int[] outlierPixels;

		Builder() {}

		public Builder kVdd(int kVdd) {
			this.kVdd = kVdd;
			return this;
		}

		public Builder vdd25(int vdd25) {
			this.vdd25 = vdd25;
			return this;
		}

		public Builder KvPtat(double KvPtat) {
			this.KvPtat = KvPtat;
			return this;
		}

		public Builder KtPtat(double KtPtat) {
			this.KtPtat = KtPtat;
			return this;
		}

		public Builder vPtat25(int vPtat25) {
			this.vPtat25 = vPtat25;
			return this;
		}

		public Builder alphaPtat(double alphaPtat) {
			this.alphaPtat = alphaPtat;
			return this;
		}

		public Builder gainEe(int gainEe) {
			this.gainEe = gainEe;
			return this;
		}

		public Builder tgc(double tgc) {
			this.tgc = tgc;
			return this;
		}

		public Builder cpKv(double cpKv) {
			this.cpKv = cpKv;
			return this;
		}

		public Builder cpKta(double cpKta) {
			this.cpKta = cpKta;
			return this;
		}

		public Builder resolutionEe(int resolutionEe) {
			this.resolutionEe = resolutionEe;
			return this;
		}

		public Builder calibrationModeEe(int calibrationModeEe) {
			this.calibrationModeEe = calibrationModeEe;
			return this;
		}

		public Builder ksTa(double ksTa) {
			this.ksTa = ksTa;
			return this;
		}

		public Builder ksTo(double[] ksTo) {
			this.ksTo = ksTo;
			return this;
		}

		public Builder ct(int[] ct) {
			this.ct = ct;
			return this;
		}

		public Builder alpha(int[] alpha) {
			this.alpha = alpha;
			return this;
		}

		public Builder alphaScale(int alphaScale) {
			this.alphaScale = alphaScale;
			return this;
		}

		public Builder offset(int[] offset) {
			this.offset = offset;
			return this;
		}

		public Builder kta(int[] kta) {
			this.kta = kta;
			return this;
		}

		public Builder ktaScale(int ktaScale) {
			this.ktaScale = ktaScale;
			return this;
		}

		public Builder kv(int[] kv) {
			this.kv = kv;
			return this;
		}

		public Builder kvScale(int kvScale) {
			this.kvScale = kvScale;
			return this;
		}

		public Builder cpAlpha(double[] cpAlpha) {
			this.cpAlpha = cpAlpha;
			return this;
		}

		public Builder cpOffset(int[] cpOffset) {
			this.cpOffset = cpOffset;
			return this;
		}

		public Builder ilChessC(double[] ilChessC) {
			this.ilChessC = ilChessC;
			return this;
		}

		public Builder brokenPixels(int[] brokenPixels) {
			this.brokenPixels = brokenPixels;
			return this;
		}

		public Builder outlierPixels(int[] outlierPixels) {
			this.outlierPixels = outlierPixels;
			return this;
		}

		public MlxParameters build() {
			return new MlxParameters(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	MlxParameters(Builder builder) {
		kVdd = builder.kVdd;
		vdd25 = builder.vdd25;
		KvPtat = builder.KvPtat;
		KtPtat = builder.KtPtat;
		vPtat25 = builder.vPtat25;
		alphaPtat = builder.alphaPtat;
		gainEe = builder.gainEe;
		tgc = builder.tgc;
		cpKv = builder.cpKv;
		cpKta = builder.cpKta;
		resolutionEe = builder.resolutionEe;
		calibrationModeEe = builder.calibrationModeEe;
		ksTa = builder.ksTa;
		ksTo = builder.ksTo;
		ct = builder.ct;
		alpha = builder.alpha;
		alphaScale = builder.alphaScale;
		offset = builder.offset;
		kta = builder.kta;
		ktaScale = builder.ktaScale;
		kv = builder.kv;
		kvScale = builder.kvScale;
		cpAlpha = builder.cpAlpha;
		cpOffset = builder.cpOffset;
		ilChessC = builder.ilChessC;
		brokenPixels = builder.brokenPixels;
		outlierPixels = builder.outlierPixels;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(kVdd, vdd25, KvPtat, KtPtat, vPtat25, alphaPtat, gainEe, tgc, cpKv, cpKta, resolutionEe, calibrationModeEe, ksTa, ksTo, ct, alpha, alphaScale, offset, kta, ktaScale, kv, kvScale, cpAlpha, cpOffset, ilChessC, brokenPixels, outlierPixels);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MlxParameters)) return false;
		MlxParameters other = (MlxParameters) obj;
		if (kVdd != other.kVdd) return false;
		if (vdd25 != other.vdd25) return false;
		if (!EqualsUtil.equals(KvPtat, other.KvPtat)) return false;
		if (!EqualsUtil.equals(KtPtat, other.KtPtat)) return false;
		if (vPtat25 != other.vPtat25) return false;
		if (!EqualsUtil.equals(alphaPtat, other.alphaPtat)) return false;
		if (gainEe != other.gainEe) return false;
		if (!EqualsUtil.equals(tgc, other.tgc)) return false;
		if (!EqualsUtil.equals(cpKv, other.cpKv)) return false;
		if (!EqualsUtil.equals(cpKta, other.cpKta)) return false;
		if (resolutionEe != other.resolutionEe) return false;
		if (calibrationModeEe != other.calibrationModeEe) return false;
		if (!EqualsUtil.equals(ksTa, other.ksTa)) return false;
		if (!EqualsUtil.equals(ksTo, other.ksTo)) return false;
		if (!EqualsUtil.equals(ct, other.ct)) return false;
		if (!EqualsUtil.equals(alpha, other.alpha)) return false;
		if (alphaScale != other.alphaScale) return false;
		if (!EqualsUtil.equals(offset, other.offset)) return false;
		if (!EqualsUtil.equals(kta, other.kta)) return false;
		if (ktaScale != other.ktaScale) return false;
		if (!EqualsUtil.equals(kv, other.kv)) return false;
		if (kvScale != other.kvScale) return false;
		if (!EqualsUtil.equals(cpAlpha, other.cpAlpha)) return false;
		if (!EqualsUtil.equals(cpOffset, other.cpOffset)) return false;
		if (!EqualsUtil.equals(ilChessC, other.ilChessC)) return false;
		if (!EqualsUtil.equals(brokenPixels, other.brokenPixels)) return false;
		if (!EqualsUtil.equals(outlierPixels, other.outlierPixels)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, kVdd, vdd25, KvPtat, KtPtat, vPtat25, alphaPtat, gainEe, tgc, cpKv, cpKta, resolutionEe, calibrationModeEe, ksTa, ksTo, ct, alpha, alphaScale, offset, kta, ktaScale, kv, kvScale, cpAlpha, cpOffset, ilChessC, brokenPixels, outlierPixels).toString();
	}

}
