package ceri.common.data;

import ceri.common.function.Fluent;
import ceri.common.reflect.Reflect;
import ceri.common.util.Validate;

/**
 * Interface that writes longs sequentially. Type T must be the sub-class type; this allows fluent
 * method calls without redefining all the methods with covariant return types.
 * <p>
 * For bulk efficiency, consider overriding these methods that process one long at a time, or make a
 * sub-array copy:
 *
 * <pre>
 * T skip(int length); [1-long]
 * T fill(int length, int value); [1-long]
 * T writeFrom(long[] dest, int offset, int length); [1-long]
 * T writeFrom(LongProvider provider, int offset, int length); [1-long]
 * </pre>
 *
 * @see ceri.common.data.LongReceiver.Writer
 */
public interface LongWriter<T extends LongWriter<T>> extends Fluent<T> {

	/**
	 * Writes a long. May throw unchecked exception if no capacity to write.
	 */
	T writeLong(long value);

	/**
	 * Skips longs. By default this writes ints with value 0.
	 */
	default T skip(int length) {
		return fill(length, 0);
	}

	/**
	 * Writes double as long.
	 */
	default T writeDouble(double value) {
		return writeLong(Double.doubleToLongBits(value));
	}

	/**
	 * Fill longs with same value. Default implementation fills one long at a time; efficiency may
	 * be improved by overriding.
	 */
	default T fill(int length, long value) {
		while (length-- > 0)
			writeLong(value);
		return Reflect.unchecked(this);
	}

	/**
	 * Writes longs from array.
	 */
	default T writeLongs(long... array) {
		return writeFrom(array, 0);
	}

	/**
	 * Writes longs from array.
	 */
	default T writeFrom(long[] array, int offset) {
		return writeFrom(array, offset, array.length - offset);
	}

	/**
	 * Writes longs from array. Default implementation writes one long at a time; efficiency may be
	 * improved by overriding.
	 */
	default T writeFrom(long[] array, int offset, int length) {
		Validate.slice(array.length, offset, length);
		for (int i = 0; i < length; i++)
			writeLong(array[offset + i]);
		return Reflect.unchecked(this);
	}

	/**
	 * Writes longs from long provider.
	 */
	default T writeFrom(LongProvider provider) {
		return writeFrom(provider, 0);
	}

	/**
	 * Writes longs from long provider.
	 */
	default T writeFrom(LongProvider provider, int offset) {
		return writeFrom(provider, offset, provider.length() - offset);
	}

	/**
	 * Writes longs from long provider. Default implementation writes one long at a time; efficiency
	 * may be improved by overriding.
	 */
	default T writeFrom(LongProvider provider, int offset, int length) {
		Validate.slice(provider.length(), offset, length);
		for (int i = 0; i < length; i++)
			writeLong(provider.getLong(offset + i));
		return Reflect.unchecked(this);
	}
}
