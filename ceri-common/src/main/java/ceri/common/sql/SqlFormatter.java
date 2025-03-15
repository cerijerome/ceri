package ceri.common.sql;

public interface SqlFormatter {
	SqlFormatter DEFAULT = String::valueOf;

	/**
	 * Format field by class type
	 */
	String format(Object field);

	/**
	 * Format field by sql type. By default, the type is ignored and object class is used.
	 * @param type
	 */
	default String format(Object field, SqlType type) {
		return format(field);
	}

	/**
	 * Format field by sql type
	 */
	default String format(Object field, int sqlType) {
		return format(field, SqlType.from(sqlType));
	}

}
