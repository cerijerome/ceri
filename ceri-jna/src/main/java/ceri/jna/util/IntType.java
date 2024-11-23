package ceri.jna.util;

import java.util.function.LongUnaryOperator;
import java.util.function.Supplier;
import com.sun.jna.IntegerType;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

/**
 * Extends standard integer type, exposing size and sign, and providing read/write pointer access.
 */
@SuppressWarnings("serial")
public abstract class IntType extends IntegerType {
	public int size;
	public boolean unsigned;

	public static <T extends IntType> T set(T intType, long value) {
		if (intType != null) intType.setValue(value);
		return intType;
	}
	
	public static <T extends IntType> T read(PointerType p, Supplier<T> supplier) {
		return read(p.getPointer(), 0, supplier);
	}

	public static <T extends IntType> T read(Pointer p, long offset, Supplier<T> supplier) {
		var intType = supplier == null ? null : supplier.get();
		if (intType != null) intType.read(p, offset);
		return intType;
	}

	public static <T extends IntType> T write(PointerType p, T intType) {
		return write(p.getPointer(), 0, intType);
	}

	public static <T extends IntType> T write(Pointer p, long offset, T intType) {
		if (intType != null) intType.write(p, offset);
		return intType;
	}

	protected IntType(int size, long value, boolean unsigned) {
		super(size, value, unsigned);
		this.size = size;
		this.unsigned = unsigned;
	}

	/**
	 * Returns the underlying number type.
	 */
	public Number number() {
		return (Number) toNative();
	}

	/**
	 * Perform a bitwise-and.
	 */
	public IntType and(long value) {
		return JnaUtil.and(this, value);
	}

	/**
	 * Perform a bitwise-or.
	 */
	public IntType or(long value) {
		return JnaUtil.or(this, value);
	}

	/**
	 * Perform a bitwise-and followed by bitwise-or.
	 */
	public IntType andOr(long and, long or) {
		return JnaUtil.andOr(this, and, or);
	}

	/**
	 * Apply and set the native long value.
	 */
	public IntType apply(LongUnaryOperator operator) {
		return JnaUtil.apply(this, operator);
	}

	/**
	 * Writes the value to the pointer offset.
	 */
	public IntType read(Pointer p, long offset) {
		switch (size) {
			case 1 -> setValue(p.getByte(offset));
			case 2 -> setValue(p.getShort(offset));
			case 4 -> setValue(p.getInt(offset));
			default -> setValue(p.getLong(offset));
		}
		return this;
	}

	/**
	 * Reads the value from the pointer offset.
	 */
	public IntType write(Pointer p, long offset) {
		switch (size) {
			case 1 -> p.setByte(offset, byteValue());
			case 2 -> p.setShort(offset, shortValue());
			case 4 -> p.setInt(offset, intValue());
			default -> p.setLong(offset, longValue());
		}
		return this;
	}
}
