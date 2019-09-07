package ceri.x10.cm11a;

import ceri.common.util.EqualsUtil;
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
	private final Object obj;
	private final int hashCode;

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
	public Entry(Address address) {
		obj = address;
		type = Type.address;
		hashCode = HashCoder.hash(type, obj);
	}

	/**
	 * Creates an entry that wraps a function.
	 */
	public Entry(Function function) {
		obj = function;
		type = Type.function;
		hashCode = HashCoder.hash(type, obj);
	}

	/**
	 * Creates an entry that wraps a dim function.
	 */
	public Entry(DimFunction function) {
		obj = function;
		type = Type.dim;
		hashCode = HashCoder.hash(type, obj);
	}

	/**
	 * Creates an entry that wraps an extended function.
	 */
	public Entry(ExtFunction function) {
		obj = function;
		type = Type.ext;
		hashCode = HashCoder.hash(type, obj);
	}

	/**
	 * Casts wrapped object. Returns null if no match.
	 */
	public Address asAddress() {
		if (type != Type.address) return null;
		return (Address) obj;
	}

	/**
	 * Casts wrapped object. Returns null if no match.
	 */
	public Function asFunction() {
		if (type != Type.function) return null;
		return (Function) obj;
	}

	/**
	 * Casts wrapped object. Returns null if no match.
	 */
	public DimFunction asDimFunction() {
		if (type != Type.dim) return null;
		return (DimFunction) obj;
	}

	/**
	 * Casts wrapped object. Returns null if no match.
	 */
	public ExtFunction asExtFunction() {
		if (type != Type.ext) return null;
		return (ExtFunction) obj;
	}

	/**
	 * Casts wrapped object. Returns null if no match.
	 */
	public BaseFunction asBaseFunction() {
		if (type == Type.address) return null;
		return (BaseFunction) obj;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Entry)) return false;
		Entry other = (Entry)obj;
		return type == other.type && EqualsUtil.equals(this.obj,  other.obj);
	}
	
	@Override
	public String toString() {
		return obj.toString();
	}
	
}
