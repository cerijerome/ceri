package ceri.common.sql;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import ceri.common.data.TypeTranscoder;

/**
 * Enum wrapper for sql type integers.
 */
public enum SqlType {
	sqlBit(Types.BIT, Boolean.class),
	sqlTinyInt(Types.TINYINT, Byte.class),
	sqlSmallInt(Types.SMALLINT, Short.class),
	sqlInt(Types.INTEGER, Integer.class),
	sqlBigInt(Types.BIGINT, Long.class),
	sqlNumeric(Types.NUMERIC, BigDecimal.class),
	sqlFloat(Types.FLOAT, Float.class),
	sqlDouble(Types.DOUBLE, Double.class),
	sqlVarChar(Types.VARCHAR, String.class),
	sqlVarBinary(Types.VARBINARY, byte[].class),
	sqlClob(Types.CLOB, String.class, Clob.class),
	sqlBlob(Types.BLOB, Blob.class),
	sqlDate(Types.DATE, Date.class),
	sqlTime(Types.TIME, Time.class),
	sqlTimestamp(Types.TIMESTAMP, Timestamp.class);

	private static final TypeTranscoder.Single<SqlType> xcoder =
		TypeTranscoder.single(t -> t.value, SqlType.class);
	public final int value;
	public final List<Class<?>> classes;

	SqlType(int value, Class<?>... classes) {
		this.value = value;
		this.classes = List.of(classes);
	}

	public static SqlType from(int sqlType) {
		return xcoder.decode(sqlType);
	}
}
