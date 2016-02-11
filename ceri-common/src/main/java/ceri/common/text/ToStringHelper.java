package ceri.common.text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Helps create string representations of objects.
 *
 * Output takes the form: Name(Value1,Value2,...)[Field1,Field2,...]
 *
 * Fields can be of the form "Key=Value" Also has support for classes such as
 * ClassName(Value,...)[Field,...] and Name(Value,...)[ClassName=Value,...]
 *
 * Typical usage is Value for intrinsic or unnamed data, Field for supplementary or named data.
 */
public class ToStringHelper {
	private static final String DATE_FORMAT_DEF = "yyyy-MM-dd HH:mm:ss z";
	private static final String CHILD_INDENT_DEF = "  ";
	private final String name;
	private List<Object> values = Collections.emptyList();
	private List<Field> fields = Collections.emptyList();
	private String childIndent = CHILD_INDENT_DEF;
	private List<Object> children = Collections.emptyList();
	private TimeZone timeZone = null;
	private String dateFormatStr = null;
	private DateFormat dateFormat = null;

	private static class Field {
		final String name;
		final Object value;

		Field(String name, Object value) {
			this.name = name;
			this.value = value;
		}
	}

	private ToStringHelper(String name) {
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
		return create(name).values(values);
	}

	/**
	 * Creates an instance with name from the simple class name of the given object.
	 */
	public static ToStringHelper createByClass(Object obj) {
		return create(obj.getClass().getSimpleName());
	}

	/**
	 * Creates an instance with name from the simple class name of the given object, and values.
	 * Name(Value1,Value2,...)
	 */
	public static ToStringHelper createByClass(Object obj, Object... values) {
		return create(obj.getClass().getSimpleName()).values(values);
	}

	/**
	 * Sets the date format for value and field string representations.
	 */
	public ToStringHelper dateFormat(String format) {
		dateFormatStr = format;
		return this;
	}

	/**
	 * Sets the time zone for value and field string representations.
	 */
	public ToStringHelper dateFormat(TimeZone timeZone) {
		this.timeZone = timeZone;
		return this;
	}

	/**
	 * Sets the date format and time zone for value and field string representations.
	 */
	public ToStringHelper dateFormat(String format, TimeZone timeZone) {
		dateFormat(format);
		return dateFormat(timeZone);
	}

	/**
	 * Adds values to the string representation. ...(Value1,Value2,...)
	 */
	public ToStringHelper values(Object... values) {
		if (values.length == 0) return this;
		if (this.values.isEmpty()) this.values = new ArrayList<>();
		Collections.addAll(this.values, values);
		return this;
	}

	/**
	 * Adds fields to the string representation. ...[Field1,Field2,...]
	 */
	public ToStringHelper fields(Object... fields) {
		for (Object field : fields)
			field((String) null, field);
		return this;
	}

	/**
	 * Add a key-value field to the string representation. ...[Key=Value]
	 */
	public ToStringHelper field(String key, Object value) {
		if (this.fields.isEmpty()) this.fields = new ArrayList<>();
		this.fields.add(new Field(key, value));
		return this;
	}

	/**
	 * Add a key-value field to the string representation, class name as key. ...[ClassName=Value]
	 */
	public ToStringHelper field(Class<?> key, Object value) {
		return field(key.getSimpleName(), value);
	}

	/**
	 * Add a key-value fields to the string representation, with the class name of each given value
	 * as the key. ...[ClassName1=Value1,ClassName2=Value2,...]
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
		if (!values.isEmpty()) appendValues(b, values);
		if (!fields.isEmpty()) appendFields(b, fields);
		if (children.isEmpty()) return b.toString();
		b.append(" {").append(System.lineSeparator());
		for (Object child : children) {
			String childStr = StringUtil.prefixLines(childIndent, stringValue(child));
			b.append(childStr).append(System.lineSeparator());
		}
		b.append('}');
		return b.toString();
	}

	private String stringValue(Object obj) {
		if (obj instanceof Date) return dateFormatter().format((Date) obj);
		return String.valueOf(obj);
	}

	private DateFormat dateFormatter() {
		if (dateFormat != null) return dateFormat;
		if (dateFormatStr == null) dateFormatStr = DATE_FORMAT_DEF;
		dateFormat = new SimpleDateFormat(dateFormatStr);
		if (timeZone != null) dateFormat.setTimeZone(timeZone);
		return dateFormat;
	}

	private void appendValues(StringBuilder b, Iterable<Object> values) {
		b.append('(');
		boolean first = true;
		for (Object value : values) {
			if (!first) b.append(',');
			b.append(stringValue(value));
			first = false;
		}
		b.append(')');
	}

	private void appendFields(StringBuilder b, Iterable<Field> fields) {
		b.append('[');
		boolean first = true;
		for (Field field : fields) {
			if (!first) b.append(',');
			if (field.name != null) b.append(field.name).append('=');
			b.append(stringValue(field.value));
			first = false;
		}
		b.append(']');
	}

}
