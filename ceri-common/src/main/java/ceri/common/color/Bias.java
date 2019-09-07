package ceri.common.color;

/**
 * Used to adjust fades between two colors. Should return a biased ratio value 0.0 to 1.0 from a
 * given ratio value 0.0 to 1.0.
 */
public interface Bias {
	double MIN_RATIO = 0.0;
	double MAX_RATIO = 1.0;

	double bias(double ratio);

}
