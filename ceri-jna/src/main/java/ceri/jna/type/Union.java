package ceri.jna.type;

import com.sun.jna.Pointer;
import ceri.jna.type.Struct.Align;

/**
 * Extends JNA Union to provide extra support.
 */
public class Union extends com.sun.jna.Union {

	/**
	 * Set active union field by name.
	 */
	public static <U extends com.sun.jna.Union> U type(U u, String name) {
		if (u != null) u.setType(name);
		return u;
	}

	/**
	 * Constructor for a new union without initialization.
	 */
	protected Union() {
		this(null);
	}

	/**
	 * Use this constructor to initialize from a pointer.
	 */
	protected Union(Pointer p) {
		this(p, Align.platform);
	}

	/**
	 * Use this constructor to initialize from a pointer.
	 */
	protected Union(Pointer p, Align align) {
		super(p, align.value);
	}

	@Override
	public String toString() {
		return Struct.toString(this, getFieldOrder(), this::fieldOffset);
	}
}
