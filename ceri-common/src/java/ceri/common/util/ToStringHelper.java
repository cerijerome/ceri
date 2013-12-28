package ceri.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Helps create string representations of objects.
 * 
 * Output takes the form: Name(Value1,Value2,...)[Field1,Field2,...]
 * 
 * Fields can be of the form "Key=Value" Also has support for classes such as
 * ClassName(Value,...)[Field,...] and Name(Value,...)[ClassName=Value,...]
 * 
 * Typical usage is Value for intrinsic or unnamed data, Field for supplementary
 * or named data.
 */
public class ToStringHelper {
	private static final String CHILD_INDENT_DEF = "  ";
	private final String name;
	private List<String> fields = Collections.emptyList();
	private String childIndent = CHILD_INDENT_DEF;
	private List<Object> children = Collections.emptyList();

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
	 * Creates an instance with given name and values. Name(Value1,Value2,...)
	 */
	public static ToStringHelper create(String name, Object... values) {
		return create(name + StringUtil.toString("(", ")", ",", values));
	}

	/**
	 * Creates an instance with name from the simple class name of the given
	 * object.
	 */
	public static ToStringHelper createByClass(Object obj) {
		return create(obj.getClass().getSimpleName());
	}

	/**
	 * Creates an instance with name from the simple class name of the given
	 * object, and values. Name(Value1,Value2,...)
	 */
	public static ToStringHelper createByClass(Object obj, Object... values) {
		return create(obj.getClass().getSimpleName(), values);
	}

	/**
	 * Adds fields to the string representation. ...[Field1,Field2,...]
	 */
	public ToStringHelper fields(Object... fields) {
		if (fields.length > 0 && this.fields.isEmpty()) this.fields = new ArrayList<>();
		for (Object field : fields)
			this.fields.add(String.valueOf(field));
		return this;
	}

	/**
	 * Add a key-value field to the string representation. ...[Key=Value]
	 */
	public ToStringHelper field(String key, Object value) {
		return fields(key + "=" + value);
	}

	/**
	 * Add a key-value field to the string representation, class name as key.
	 * ...[ClassName=Value]
	 */
	public ToStringHelper field(Class<?> key, Object value) {
		return field(key.getSimpleName(), value);
	}

	/**
	 * Add a key-value fields to the string representation, with the class name
	 * of each given value as the key.
	 * ...[ClassName1=Value1,ClassName2=Value2,...]
	 */
	public ToStringHelper fieldsByClass(Object... fields) {
		for (Object field : fields) {
			if (field == null) fields(field);
			else field(field.getClass(), field);
		}
		return this;
	}

	public ToStringHelper childIndent(String childIndent) {
		this.childIndent = childIndent;
		return this;
	}

	public ToStringHelper children(Object... children) {
		return childrens(Arrays.asList(children));
	}

	public ToStringHelper childrens(Iterable<?> children) {
		if (this.children.isEmpty()) this.children = new ArrayList<>();
		for (Object child : children)
			this.children.add(child);
		return this;
	}

	/**
	 * Returns the string representation.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(name);
		if (!fields.isEmpty()) b.append(StringUtil.toString("[", "]", ",", fields));
		if (children.isEmpty()) return b.toString();
		b.append(" {").append(System.lineSeparator());
		for (Object child : children) {
			String childStr = StringUtil.prefixLines(childIndent, String.valueOf(child));
			b.append(childStr).append(System.lineSeparator());
		}
		b.append('}');
		return b.toString();
	}

}
