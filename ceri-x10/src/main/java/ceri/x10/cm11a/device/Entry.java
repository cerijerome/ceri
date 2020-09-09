package ceri.x10.cm11a.device;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.util.Objects;
import ceri.common.util.HashCoder;
import ceri.x10.type.Address;
import ceri.x10.type.BaseFunction;
import ceri.x10.type.DimFunction;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.Function;

/**
 * Holder for one of multiple types - address, function, dim function or extended function.
 */
public class Entry {
	public final Type type;
	private final Object object;

	/**
	 * Enum to identify the wrapped type.
	 */
	public enum Type {
		address,
		function,
		dim,
		ext;
	}

	/**
	 * Creates an entry that wraps an address.
	 */
	public static Entry of(Address address) {
		validateNotNull(address);
		return new Entry(Type.address, address);
	}

	/**
	 * Creates an entry that wraps a function.
	 */
	public static Entry of(Function function) {
		validateNotNull(function);
		return new Entry(Type.function, function);
	}

	/**
	 * Creates an entry that wraps a dim function.
	 */
	public static Entry of(DimFunction function) {
		validateNotNull(function);
		return new Entry(Type.dim, function);
	}

	/**
	 * Creates an entry that wraps an extended function.
	 */
	public static Entry of(ExtFunction function) {
		validateNotNull(function);
		return new Entry(Type.ext, function);
	}

	private Entry(Type type, Object obj) {
		this.type = type;
		this.object = obj;
	}

	/**
	 * Casts wrapped object. Returns null if no match.
	 */
	public Address asAddress() {
		if (type != Type.address) return null;
		return (Address) object;
	}

	/**
	 * Casts wrapped object. Returns null if no match.
	 */
	public Function asFunction() {
		if (type != Type.function) return null;
		return (Function) object;
	}

	/**
	 * Casts wrapped object. Returns null if no match.
	 */
	public DimFunction asDimFunction() {
		if (type != Type.dim) return null;
		return (DimFunction) object;
	}

	/**
	 * Casts wrapped object. Returns null if no match.
	 */
	public ExtFunction asExtFunction() {
		if (type != Type.ext) return null;
		return (ExtFunction) object;
	}

	/**
	 * Casts wrapped object. Returns null if no match.
	 */
	public BaseFunction asBaseFunction() {
		if (type == Type.address) return null;
		return (BaseFunction) object;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(type, object);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Entry)) return false;
		Entry other = (Entry) obj;
		if (type != other.type) return false;
		if (!Objects.equals(object, other.object)) return false;
		return true;
	}

	@Override
	public String toString() {
		return object.toString();
	}

}
