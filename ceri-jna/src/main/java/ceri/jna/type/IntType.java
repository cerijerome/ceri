package ceri.jna.type;

import com.sun.jna.IntegerType;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.ByReference;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.jna.util.JnaArgs;
import ceri.jna.util.JnaUtil;

/**
 * Extends standard integer type, exposing size and sign, and providing read/write pointer access.
 * Must be used as {@code class T extends IntType<T>}
 */
@SuppressWarnings("serial")
public abstract class IntType<T extends IntType<T>> extends IntegerType {
	public final int size;
	public final boolean unsigned;

	public abstract static class ByRef<T extends IntType<T>> extends ByReference {
		private final Functions.Supplier<T> supplier;

		protected ByRef(Functions.Supplier<T> supplier) {
			this(supplier, Pointer.NULL);
		}

		protected ByRef(Functions.Supplier<T> supplier, Pointer p) {
			this(supplier, supplier.get(), p);
		}

		protected ByRef(Functions.Supplier<T> supplier, T value) {
			this(supplier, def(value, supplier), null);
		}

		private ByRef(Functions.Supplier<T> supplier, T value, Pointer p) {
			super(value.size);
			this.supplier = supplier;
			if (p != null) setPointer(p);
			setValue(value);
		}

		public void setValue(T value) {
			IntType.write(value, this);
		}

		public void setValue(long value) {
			var t = supplier.get();
			t.setValue(value);
			setValue(t);
		}

		public T getValue() {
			return readInto(supplier.get(), this);
		}

		public long longValue() {
			return getValue().longValue();
		}

		@Override
		public String toString() {
			return String.valueOf(getValue()) + JnaArgs.string(getPointer());
		}

		private static <T> T def(T value, Functions.Supplier<T> supplier) {
			return value != null ? value : supplier.get();
		}
	}

	public static <T extends IntType<T>> T set(T t, long value) {
		if (t != null) t.setValue(value);
		return t;
	}

	public static Long get(IntType<?> t) {
		return t == null ? null : t.longValue();
	}

	public static <T extends IntType<T>> T readInto(T t, PointerType p) {
		if (t != null) t.read(p);
		return t;
	}

	public static <T extends IntType<T>> T readInto(T t, Pointer p, long offset) {
		if (t != null) t.read(p, offset);
		return t;
	}

	public static <T extends IntType<T>> T write(T t, PointerType p) {
		if (t != null) t.write(p);
		return t;
	}

	public static <T extends IntType<T>> T write(T t, Pointer p, long offset) {
		if (t != null) t.write(p, offset);
		return t;
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
	public T and(long value) {
		JnaUtil.and(this, value);
		return typedThis();
	}

	/**
	 * Perform a bitwise-or.
	 */
	public T or(long value) {
		JnaUtil.or(this, value);
		return typedThis();
	}

	/**
	 * Perform a bitwise-and followed by bitwise-or.
	 */
	public T andOr(long and, long or) {
		JnaUtil.andOr(this, and, or);
		return typedThis();
	}

	/**
	 * Apply and set the native long value.
	 */
	public T apply(Functions.LongOperator operator) {
		JnaUtil.apply(this, operator);
		return typedThis();
	}

	/**
	 * Reads the value from the pointer type.
	 */
	public T read(PointerType p) {
		return read(p.getPointer(), 0L);
	}

	/**
	 * Reads the value from the pointer offset.
	 */
	public T read(Pointer p, long offset) {
		switch (size) {
			case 1 -> setValue(p.getByte(offset));
			case 2 -> setValue(p.getShort(offset));
			case 4 -> setValue(p.getInt(offset));
			default -> setValue(p.getLong(offset));
		}
		return typedThis();
	}

	/**
	 * Writes the value to the pointer type.
	 */
	public T write(PointerType p) {
		return write(p.getPointer(), 0L);
	}

	/**
	 * Writes the value to the pointer offset.
	 */
	public T write(Pointer p, long offset) {
		switch (size) {
			case 1 -> p.setByte(offset, byteValue());
			case 2 -> p.setShort(offset, shortValue());
			case 4 -> p.setInt(offset, intValue());
			default -> p.setLong(offset, longValue());
		}
		return typedThis();
	}

	@Override
	public String toString() {
		return JnaArgs.stringInt(number());
	}

	protected T typedThis() {
		return Reflect.unchecked(this);
	}
}
