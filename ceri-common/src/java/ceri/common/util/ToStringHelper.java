package ceri.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Helps create string representations of objects.
 * 
 * Output takes the form:
 *   Name(Value1,Value2,...)[Field1,Field2,...]
 *   
 * Fields can be of the form "Key=Value"
 * Also has support for classes such as ClassName(Value,...)[Field,...]
 * and Name(Value,...)[ClassName=Value,...]  
 */
public class ToStringHelper {
	private final String name;
	private final List<String> fields = new ArrayList<>();

	protected ToStringHelper(String name) {
		this.name = name;
	}

	/**
	 * Creates an instance with given simple name.
	 */
	public static ToStringHelper create(String name) {
		return new ToStringHelper(name);
	}

	/**
	 * Creates an instance with given name and values.
	 * Name(Value1,Value2,...)
	 */
	public static ToStringHelper create(String name, Object...values) {
		return create(name + StringUtil.toString("(", ")", ",", values));
	}

	/**
	 * Creates an instance with name from the simple class name of the given object.
	 */
	public static ToStringHelper createByClass(Object obj) {
		return create(obj.getClass().getSimpleName());
	}

	/**
	 * Creates an instance with name from the simple class name of the given object,
	 * and values.
	 * Name(Value1,Value2,...)
	 */
	public static ToStringHelper createByClass(Object obj, Object...values) {
		return create(obj.getClass().getSimpleName(), values);
	}

	/**
	 * Adds fields to the string representation.
	 * ...[Field1,Field2,...]
	 */
	public ToStringHelper add(Object...fields) {
		for (Object field : fields) this.fields.add(String.valueOf(field));
		return this;
	}

	/**
	 * Add a key-value field to the string representation.
	 * ...[Key=Value]
	 */
	public ToStringHelper add(String key, Object value) {
		fields.add(key + "=" + value);
		return this;
	}

	/**
	 * Add a key-value field to the string representation, class name as key.
	 * ...[ClassName=Value]
	 */
	public ToStringHelper add(Class<?> key, Object value) {
		return add(key.getSimpleName(), value);
	}

	/**
	 * Add a key-value fields to the string representation,
	 * with the class name of each given value as the key.
	 * ...[ClassName1=Value1,ClassName2=Value2,...]
	 */
	public ToStringHelper addByClass(Object...fields) {
		for (Object field : fields) {
			if (field == null) add(field);
			else add(field.getClass(), field);
		}
		return this;
	}

	/**
	 * Returns the string representation.
	 */
	@Override
	public String toString() {
		if (fields.isEmpty()) return name;
		return name + StringUtil.toString("[", "]", ",", fields);
	}

}
