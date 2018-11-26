package ceri.serial.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Extends Structure to read fields when constructed from a pointer.
 * Also makes array handling more robust/typed.
 */
public abstract class Struct extends Structure {

	public Struct() {}
	
	public Struct(Pointer p) {
		super(p);
		read();
	}

	@Override
	public Structure[] toArray(Structure[] array) {
		if (array.length == 0) return array;
		return super.toArray(array);
	}

}
