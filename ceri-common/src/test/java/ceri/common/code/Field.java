package ceri.common.code;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Pattern;
import ceri.common.collection.ImmutableUtil;
import ceri.common.text.RegexUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Field {
	private static final Pattern PRIMITIVE_EQUALS_REGEX =
		Pattern.compile("^(boolean|char|byte|short|int|long)$");
	public final String name;
	public final String type;
	public final CollectionType collection;
	public final MapType map;
	public final boolean primitiveEquals;

	public static Field of(String name, String type) {
		return new Field(name, type);
	}

	private Field(String name, String type) {
		this.name = name;
		this.type = type;
		collection = CollectionType.of(type);
		map = MapType.of(type);
		primitiveEquals = RegexUtil.found(PRIMITIVE_EQUALS_REGEX, type) != null;
	}

	/**
	 * Returns name
	 */
	public String name() {
		return name;
	}

	/**
	 * Returns name without trailing s
	 */
	public String nonPluralName() {
		if (!name.endsWith("s")) return name;
		return name.substring(0, name.length() - 1);
	}

	/**
	 * Returns the following depending on type:
	 * 
	 * <pre>
	 * null
	 * T... name
	 * </pre>
	 */
	public String asVarArg() {
		if (collection == null) return null;
		return String.format("%s... %s", collection.itemType, name);
	}

	/**
	 * Returns the following depending on type:
	 * 
	 * <pre>
	 * null
	 * Arrays.asList(name)
	 * </pre>
	 */
	public String fromVarArg(Context con) {
		if (collection == null) return null;
		con.imports(Arrays.class);
		return String.format("Arrays.asList(%s)", name);
	}

	/**
	 * Returns the following depending on type:
	 * 
	 * <pre>
	 * name
	 * Arrays.asList(name)
	 * </pre>
	 */
	public String fromVarArgParam(Context con, String varArg) {
		if (collection == null || !name.equals(varArg)) return name;
		return fromVarArg(con);
	}
	
	/**
	 * Returns one of the following depending on type and if matching vararg name:
	 * 
	 * <pre>
	 * type name
	 * Collection<T> name
	 * T... name
	 * </pre>
	 */
	public String asLooseArg(Context con, String varArg) {
		if (collection == null) return toString();
		if (name.equals(varArg)) return asVarArg();
		return asCollection(con);
	}
	
	/**
	 * Returns the following depending on tyep:
	 * 
	 * <pre>
	 * null
	 * Collection{@literal<}T> name
	 * </pre>
	 */
	public String asCollection(Context con) {
		if (collection == null) return null;
		con.imports(Collection.class);
		return String.format("Collection<%s> %s", collection.itemType, name);
	}

	/**
	 * Returns the following depending on type:
	 * 
	 * <pre>
	 * null
	 * Map{@literal<}K, V> name
	 * </pre>
	 */
	public String asMap(Context con) {
		if (map == null) return null;
		con.imports(Map.class);
		return String.format("Map<%s, %s> %s", map.keyType, map.valueType, name);
	}

	/**
	 * Returns the following:
	 * 
	 * <pre>
	 * prefix-field.name = prefix-arg.name
	 * </pre>
	 */
	public String asAssignment(Prefix prefix) {
		return String.format("%s%s = %s%2$s", prefix.field, name, prefix.argument);
	}

	/**
	 * Returns one of the following depending on type:
	 * 
	 * <pre>
	 * prefix-field.name = prefix-arg.name
	 * prefix-field.name = ImmutableUtil.copyAsList(prefix-arg.name)
	 * prefix-field.name = ImmutableUtil.copyAsSet(prefix-arg.name)
	 * prefix-field.name = ImmutableUtil.copyAsMap(prefix-arg.name)
	 * </pre>
	 */
	public String asCopyAssignment(Context con, Prefix prefix) {
		return String.format("%s%s = %s", prefix.field, name, asCopy(con, prefix.argument));
	}

	/**
	 * Returns one of the following depending on type:
	 * 
	 * <pre>
	 * prefix.name
	 * ImmutableUtil.copyAsList(prefix.name)
	 * ImmutableUtil.copyAsSet(prefix.name)
	 * ImmutableUtil.copyAsMap(prefix.name)
	 * </pre>
	 */
	public String asCopy(Context con, String prefix) {
		String copy = asCollectionCopy(con, prefix);
		if (copy == null) copy = asMapCopy(con, prefix);
		if (copy == null) copy = prefix + name;
		return copy;
	}

	private String asCollectionCopy(Context con, String prefix) {
		if (collection == null) return null;
		con.imports(ImmutableUtil.class, collection.typeClass());
		if (collection.isList())
			return String.format("ImmutableUtil.copyAsList(%s%s)", prefix, name);
		return String.format("ImmutableUtil.copyAsSet(%s%s)", prefix, name);
	}

	private String asMapCopy(Context con, String prefix) {
		if (map == null) return null;
		con.imports(ImmutableUtil.class, map.typeClass());
		return String.format("ImmutableUtil.copyAsMap(%s%s)", prefix, name);
	}

	/**
	 * Returns one of the following depending on type:
	 * 
	 * <pre>
	 * type name
	 * final Collection{@literal<}T> name = new LinkedHashSet{@literal<}>()
	 * final Map{@literal<}K, V> map = new LinkedHAshMap{@literal<}>()
	 * </pre>
	 */
	public String asBuilderField(Context con) {
		String builderField = asBuilderCollectionField(con);
		if (builderField == null) builderField = asBuilderMapField(con);
		if (builderField == null) builderField = toString();
		return builderField;
	}

	private String asBuilderCollectionField(Context con) {
		if (collection == null) return null;
		con.imports(Collection.class, LinkedHashSet.class);
		return String.format("final %s = new LinkedHashSet<>()", asCollection(con));
	}

	private String asBuilderMapField(Context con) {
		if (map == null) return null;
		con.imports(Map.class, LinkedHashMap.class);
		return String.format("final %s = new LinkedHashMap<>()", asMap(con));
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(name, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Field)) return false;
		Field other = (Field) obj;
		if (!EqualsUtil.equals(name, other.name)) return false;
		if (!EqualsUtil.equals(type, other.type)) return false;
		return true;
	}

	@Override
	public String toString() {
		return type + " " + name;
	}

}
