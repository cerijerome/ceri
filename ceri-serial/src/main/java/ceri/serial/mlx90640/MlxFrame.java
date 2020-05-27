package ceri.serial.mlx90640;

import static ceri.common.validation.ValidationUtil.*;
import static ceri.serial.mlx90640.Mlx90640.COLUMNS;
import static ceri.serial.mlx90640.Mlx90640.PIXELS;
import static ceri.serial.mlx90640.Mlx90640.ROWS;
import ceri.common.math.MathUtil;

/**
 * Wrapper for pixel temperature settings. Populated by Mlx90640 and MlxData classes.
 */
public class MlxFrame {
	private double vdd = Double.NaN;
	private double ta = Double.NaN;
	private double tr = Double.NaN;
	private double emissivity = Double.NaN;
	private ReadingPattern mode = null;
	private final double[] values = new double[PIXELS];

	public static MlxFrame of() {
		return new MlxFrame();
	}
	
	private MlxFrame() {}

	public ReadingPattern mode() {
		return mode;
	}
	
	public double vdd() {
		return vdd;
	}
	
	public double ta() {
		return ta;
	}
	
	public double tr() {
		return tr;
	}
	
	public double emissivity() {
		return emissivity;
	}
	
	public double value(int row, int column) {
		validateRangeL(row, 0,  ROWS - 1);
		validateRangeL(column, 0,  COLUMNS - 1);
		return values[row * COLUMNS + column];
	}

	public int intValue(int row, int column) {
		return (int) Math.round(value(row, column));
	}

	public double max() {
		return MathUtil.max(values);
	}
	
	public double min() {
		return MathUtil.min(values);
	}
	
	public double average() {
		return MathUtil.average(values);
	}
	
	void setContext(ReadingPattern mode, double vdd, double ta, double tr, double emissivity) {
		this.mode = mode;
		this.vdd = vdd;
		this.ta = ta;
		this.tr = tr;
		this.emissivity = emissivity;
	}
	
	double[] values() {
		return values;
	}
	
}
