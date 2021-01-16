package ceri.serial.jna;

import com.sun.jna.Pointer;

/**
 * Extends Struct to provide support for a variable-length (0+) array as the last field in the
 * structure.
 */
public abstract class VarStruct extends Struct {

	protected VarStruct() {
		this(null);
	}

	protected VarStruct(Pointer p) {
		this(p, Align.platform);
	}

	protected VarStruct(Pointer p, Align align) {
		super(p, align);
	}

	/**
	 * Create the var array of given size. 
	 */
	protected abstract void setVarArray(int count);

	/**
	 * Provide the size of the array. Counted as 0 if less than 0.
	 */
	protected abstract int varCount();
	
	@Override
	protected void ensureAllocated() {
		if (safeVarCount() == 0) setVarArray(1);
		super.ensureAllocated();
		if (safeVarCount() == 0) setVarArray(0);
	}

	@Override
	protected void writeField(StructField structField) {
		if (structField.name.equals(lastName()) && safeVarCount() == 0) return;
		super.writeField(structField);
	}

	@Override
	protected Object readField(StructField structField) {
		if (structField.name.equals(lastName())) {
			int count = safeVarCount();
			setVarArray(count);
			if (count == 0) return null;
		}
		return super.readField(structField);
	}
	
	private int safeVarCount() {
		return Math.max(0, varCount());
	}
}
