package ceri.serial.jna;

import java.util.List;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Encapsulates an object reference token for use with RefStore.
 */
public class RefToken extends Structure {
	private static final List<String> FIELDS = List.of("value");

	public static class ByValue extends RefToken implements Structure.ByValue {}

	public static class ByReference extends RefToken implements Structure.ByReference {}

	public int value;

	public RefToken() {}

	public RefToken(Pointer p) {
		super(p);
	}

	@Override
	protected List<String> getFieldOrder() {
		return FIELDS;
	}
}