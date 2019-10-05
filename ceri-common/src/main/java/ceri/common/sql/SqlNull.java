package ceri.common.sql;

/**
 * Convenience typed null values that are accepted by set(...) on {@code SqlStatement}
 */
public enum SqlNull {
	nullBit(SqlType.sqlBit),
	nullBool(SqlType.sqlBit),
	nullTinyInt(SqlType.sqlTinyInt),
	nullByte(SqlType.sqlTinyInt),
	nullSmallInt(SqlType.sqlSmallInt),
	nullShort(SqlType.sqlSmallInt),
	nullInt(SqlType.sqlInt),
	nullBigInt(SqlType.sqlBigInt),
	nullLong(SqlType.sqlBigInt),
	nullNumeric(SqlType.sqlNumeric),
	nullBigDecimal(SqlType.sqlNumeric),
	nullFloat(SqlType.sqlFloat),
	nullDouble(SqlType.sqlDouble),
	nullVarChar(SqlType.sqlVarChar),
	nullString(SqlType.sqlVarChar),
	nullVarBinary(SqlType.sqlVarBinary),
	nullBytes(SqlType.sqlVarBinary),
	nullClob(SqlType.sqlClob),
	nullBlob(SqlType.sqlBlob),
	nullDate(SqlType.sqlDate),
	nullTime(SqlType.sqlTime),
	nullTimestamp(SqlType.sqlTimestamp);

	public final SqlType sqlType;

	SqlNull(SqlType sqlType) {
		this.sqlType = sqlType;
	}
}
