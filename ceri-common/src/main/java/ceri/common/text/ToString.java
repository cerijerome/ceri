package ceri.common.text;

import static ceri.common.text.StringUtil.EOL;
import static ceri.common.text.StringUtil.NULL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import ceri.common.math.MathUtil;
import ceri.common.reflect.Reflect;

/**
 * Helps create string representations of objects. Output takes the form:
 * Name(Value1,Value2,...)[Field1,Field2,...] Fields can be of the form "Key=Value" Also has support
 * for classes such as ClassName(Value,...)[Field,...] and Name(Value,...)[ClassName=Value,...]
 * Typical usage is Value for intrinsic or unnamed data, Field for supplementary or named data.
 */
public class ToString {
	private static final Map<Class<?>, Function<Object, String>> toStringMap = toStringMap();
	private static final String CHILD_INDENT_DEF = "  ";
	private final String name;
	private List<Object> values = Collections.emptyList();
	private List<Field> fields = Collections.emptyList();
	private String childIndent = CHILD_INDENT_DEF;
	private List<Object> children = Collections.emptyList();

	private static class Field {
		final String name;
		final Object value;

		Field(String name, Object value) {
			this.name = name;
			this.value = value;
		}
	}

	/**
	 * Extends Arrays.deepToString to include any object type.
	 */
	public static String deep(Object obj) {
		if (obj == null) return NULL;
		Class<?> cls = obj.getClass();
		if (!cls.isArray()) return String.valueOf(obj);
		var fn = toStringMap.get(cls);
		if (fn != null) return fn.apply(obj);
		return Arrays.deepToString((Object[]) obj);
	}

	/**
	 * Provide unsigned hex string with prefix.
	 */
	public static String hex(Byte value) {
		return value == null ? NULL : hex(value.byteValue());
	}

	/**
	 * Provide unsigned hex string with prefix.
	 */
	public static String hex(Short value) {
		return value == null ? NULL : hex(value.shortValue());
	}

	/**
	 * Provide unsigned hex string with prefix.
	 */
	public static String hex(Integer value) {
		return value == null ? NULL : hex(value.intValue());
	}

	/**
	 * Provide unsigned hex string with prefix.
	 */
	public static String hex(Long value) {
		return value == null ? NULL : hex(value.longValue());
	}

	/**
	 * Provide unsigned hex string with prefix.
	 */
	public static String hex(byte value) {
		return "0x" + Integer.toHexString(MathUtil.ubyte(value));
	}

	/**
	 * Provide unsigned hex string with prefix.
	 */
	public static String hex(short value) {
		return "0x" + Integer.toHexString(MathUtil.ushort(value));
	}

	/**
	 * Provide unsigned hex string with prefix.
	 */
	public static String hex(int value) {
		return "0x" + Integer.toHexString(value);
	}

	/**
	 * Provide unsigned hex string with prefix.
	 */
	public static String hex(long value) {
		return "0x" + Long.toHexString(value);
	}

	/**
	 * Converts record string format to match.
	 */
	public static <T extends Record> String forRecord(T rec) {
		if (rec == null) return String.valueOf(rec);
		return ofClass(rec).recordValues(rec).toString();
	}

	/**
	 * Generates a string for given name and values. "Name(Value1,Value2,...)"
	 */
	public static String forName(String name, Object... values) {
		return ofName(name, values).toString();
	}

	/**
	 * Generates a string for simple class name and values. "Name(Value1,Value2,...)"
	 */
	public static String forClass(Object obj, Object... values) {
		Objects.requireNonNull(obj);
		return forName(Reflect.className(obj), values);
	}

	/**
	 * Creates an instance with given name and values. "Name(Value1,Value2,...)"
	 */
	public static ToString ofName(String name, Object... values) {
		Objects.requireNonNull(name);
		return new ToString(name).values(values);
	}

	/**
	 * Creates an instance with simple class name and values. "Name(Value1,Value2,...)"
	 */
	public static ToString ofClass(Object obj, Object... values) {
		Objects.requireNonNull(obj);
		return ofName(Reflect.className(obj), values);
	}

	private ToString(String name) {
		this.name = name;
	}

	/**
	 * Adds values to the string representation. ...(Value1,Value2,...)
	 */
	public ToString values(Object... values) {
		if (values.length == 0) return this;
		if (this.values.isEmpty()) this.values = new ArrayList<>();
		Collections.addAll(this.values, values);
		return this;
	}

	/**
	 * Adds record field values.
	 */
	public <T extends Record> ToString recordValues(T rec) {
		for (var component : rec.getClass().getRecordComponents()) {
			var method = component.getAccessor();
			method.setAccessible(true);
			values(Reflect.<Object>invoke(method, rec));
		}
		return this;
	}

	/**
	 * Adds fields to the string representation. ...[Field1,Field2,...]
	 */
	public ToString fields(Object... fields) {
		for (Object field : fields)
			field((String) null, field);
		return this;
	}

	/**
	 * Add a key-value field to the string representation. ...[Key=Value]
	 */
	public ToString field(String key, Object value) {
		if (this.fields.isEmpty()) this.fields = new ArrayList<>();
		this.fields.add(new Field(key, value));
		return this;
	}

	/**
	 * Add a key-value field to the string representation, class name as key. ...[ClassName=Value]
	 */
	public ToString field(Class<?> key, Object value) {
		return field(Reflect.name(key), value);
	}

	/**
	 * Add a key-value fields to the string representation, with the class name of each given value
	 * as the key. ...[ClassName1=Value1,ClassName2=Value2,...]
	 */
	public ToString fieldsByClass(Object... fields) {
		for (Object field : fields) {
			if (field == null) fields(field);
			else field(field.getClass(), field);
		}
		return this;
	}

	/**
	 * Specify the nested line indent.
	 */
	public ToString childIndent(String childIndent) {
		this.childIndent = childIndent;
		return this;
	}

	/**
	 * Add child to nested line if not null.
	 */
	public ToString child(Object child) {
		if (child == null) return this;
		return children(child);
	}

	/**
	 * Add collection of items to nested line if not empty.
	 */
	public ToString childs(Collection<?> collection) {
		if (collection == null || collection.isEmpty()) return this;
		return children(collection);
	}

	/**
	 * Add items to nested lines.
	 */
	public ToString children(Object... children) {
		return childrens(Arrays.asList(children));
	}

	/**
	 * Add items to nested lines.
	 */
	public ToString childrens(Map<?, ?> children) {
		if (children == null) children = Collections.emptyMap();
		return childrens(children.entrySet());
	}

	/**
	 * Add items to nested lines.
	 */
	public ToString childrens(Iterable<?> children) {
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
		b.append(" {").append(EOL);
		for (Object child : children) {
			String childStr = StringUtil.prefixLines(childIndent, childStringValue(child));
			b.append(childStr).append(EOL);
		}
		return b.append('}').toString();
	}

	private String childStringValue(Object obj) {
		if (!(obj instanceof Map.Entry<?, ?> entry)) return stringValue(obj);
		return stringValue(entry.getKey()) + ": " + stringValue(entry.getValue());
	}

	private String stringValue(Object obj) {
		if (obj == null) return String.valueOf(obj);
		if (obj instanceof Date date) return toString(date);
		return deep(obj);
	}

	private String toString(Date date) {
		LocalDateTime local = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return local.toString();
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

	private static Map<Class<?>, Function<Object, String>> toStringMap() {
		return Map.of( //
			boolean[].class, obj -> Arrays.toString((boolean[]) obj), //
			char[].class, obj -> Arrays.toString((char[]) obj), //
			byte[].class, obj -> Arrays.toString((byte[]) obj), //
			short[].class, obj -> Arrays.toString((short[]) obj), //
			int[].class, obj -> Arrays.toString((int[]) obj), //
			long[].class, obj -> Arrays.toString((long[]) obj), //
			float[].class, obj -> Arrays.toString((float[]) obj), //
			double[].class, obj -> Arrays.toString((double[]) obj));
	}
}
