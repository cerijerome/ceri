package ceri.common.sql;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import ceri.common.data.Xcoder;
import ceri.common.except.ExceptionAdapter;
import ceri.common.stream.Stream;

public class Sql {
	private static final int TABLE_NAME_INDEX = 3;
	private static final String TABLE_TYPE = "TABLE";
	public static final ExceptionAdapter<SQLException> SQL_ADAPTER =
		ExceptionAdapter.of(SQLException.class, SQLException::new);

	private Sql() {}

	/**
	 * Enum wrapper for sql type integers.
	 */
	public enum Type {
		Bit(Types.BIT, Boolean.class),
		TinyInt(Types.TINYINT, Byte.class),
		SmallInt(Types.SMALLINT, Short.class),
		Int(Types.INTEGER, Integer.class),
		BigInt(Types.BIGINT, Long.class),
		Numeric(Types.NUMERIC, BigDecimal.class),
		Float(Types.FLOAT, Float.class),
		Double(Types.DOUBLE, Double.class),
		VarChar(Types.VARCHAR, String.class),
		VarBinary(Types.VARBINARY, byte[].class),
		Clob(Types.CLOB, String.class, Clob.class),
		Blob(Types.BLOB, Blob.class),
		Date(Types.DATE, Date.class),
		Time(Types.TIME, Time.class),
		Timestamp(Types.TIMESTAMP, Timestamp.class);

		public static final Xcoder.Type<Type> xcoder = Xcoder.type(Type.class);
		public final int value;
		public final List<Class<?>> classes;

		Type(int value, Class<?>... classes) {
			this.value = value;
			this.classes = List.of(classes);
		}

		public static Type from(int sqlType) {
			return xcoder.decode(sqlType);
		}
	}

	/**
	 * Convenience typed null values that are accepted by set(...) on {@code SqlStatement}
	 */
	public enum Null {
		Bit(Type.Bit),
		Bool(Type.Bit),
		TinyInt(Type.TinyInt),
		Byte(Type.TinyInt),
		SmallInt(Type.SmallInt),
		Short(Type.SmallInt),
		Int(Type.Int),
		BigInt(Type.BigInt),
		Long(Type.BigInt),
		Numeric(Type.Numeric),
		BigDecimal(Type.Numeric),
		Float(Type.Float),
		Double(Type.Double),
		VarChar(Type.VarChar),
		String(Type.VarChar),
		VarBinary(Type.VarBinary),
		Bytes(Type.VarBinary),
		Clob(Type.Clob),
		Blob(Type.Blob),
		Date(Type.Date),
		Time(Type.Time),
		Timestamp(Type.Timestamp);

		public final Type sqlType;

		private Null(Type sqlType) {
			this.sqlType = sqlType;
		}
	}

	/**
	 * Converts sql types to strings.
	 */
	@FunctionalInterface
	public interface Formatter {
		Formatter DEFAULT = String::valueOf;

		/**
		 * Format field by class type
		 */
		String format(Object field);

		/**
		 * Format field by sql type. By default, the type is ignored and object class is used.
		 * @param type
		 */
		default String format(Object field, Type type) {
			return format(field);
		}

		/**
		 * Format field by sql type
		 */
		default String format(Object field, int sqlType) {
			return format(field, Type.from(sqlType));
		}
	}

	/**
	 * Current date/time as a sql timestamp.
	 */
	public static Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}

	public static Type type(ResultSet rs, int index) throws SQLException {
		return Type.from(rs.getMetaData().getColumnType(index));
	}

	public static List<String> tableNames(Connection con) throws SQLException {
		return tableNames(con, null, null, null, TABLE_TYPE);
	}

	public static List<String> tableNames(Connection con, String catalog, String schemaPattern,
		String namePattern, String... types) throws SQLException {
		try (var rs = con.getMetaData().getTables(catalog, schemaPattern, namePattern, types)) {
			return stream(rs).map(r -> r.getString(TABLE_NAME_INDEX)).toList();
		}
	}

	public static Stream<SQLException, ResultSet> stream(ResultSet rs) {
		return Stream.ofSupplier(c -> {
			if (!rs.next()) return false;
			c.accept(rs);
			return true;
		});
	}
}
