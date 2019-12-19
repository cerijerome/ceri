package ceri.common.sql;

import static ceri.common.function.FunctionUtil.castAccept;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.regex.Pattern;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

public class SqlStatement implements AutoCloseable {
	private static final Pattern Q_REGEX = Pattern.compile("\\?");
	public final String sql;
	public final SqlFormatter formatter;
	private final Object[] values;
	private final PreparedStatement ps;
	private int index = 1;

	public static SqlStatement track(Connection con, String sqlFormat, Object... args)
		throws SQLException {
		return track(SqlFormatter.DEFAULT, con, sqlFormat, args);
	}

	public static SqlStatement track(SqlFormatter formatter, Connection con, String sqlFormat,
		Object... args) throws SQLException {
		String sql = StringUtil.format(sqlFormat, args);
		return new SqlStatement(con.prepareStatement(sql), sql, formatter);
	}

	public static SqlStatement of(Connection con, String sqlFormat, Object... args)
		throws SQLException {
		String sql = StringUtil.format(sqlFormat, args);
		return new SqlStatement(con.prepareStatement(sql), sql, null);
	}

	private SqlStatement(PreparedStatement ps, String sql, SqlFormatter formatter) {
		this.sql = sql;
		this.ps = ps;
		this.formatter = formatter;
		values = formatter == null ? null : new Object[fields()];
	}

	public SqlStatement with(SqlFormatter formatter) {
		return new SqlStatement(ps, sql, formatter);
	}

	public int fields() {
		return (int) sql.chars().filter(c -> c == '?').count();
	}

	public SqlStatement batch() throws SQLException {
		ps.addBatch();
		index = 1;
		return this;
	}

	public PreparedStatement jdbc() {
		return ps;
	}

	public SqlStatement skip() {
		return skip(1);
	}

	public SqlStatement skip(int n) {
		index += n;
		return this;
	}

	public SqlStatement setBoolean(Boolean x) throws SQLException {
		return setWithType(x, Types.BIT);
	}

	public SqlStatement setByte(Byte x) throws SQLException {
		return setWithType(x, Types.TINYINT);
	}

	public SqlStatement setShort(Short x) throws SQLException {
		return setWithType(x, Types.SMALLINT);
	}

	public SqlStatement setInt(Integer x) throws SQLException {
		return setWithType(x, Types.INTEGER);
	}

	public SqlStatement setLong(Long x) throws SQLException {
		return setWithType(x, Types.BIGINT);
	}

	public SqlStatement setBigDecimal(BigDecimal x) throws SQLException {
		return setWithType(x, Types.NUMERIC);
	}

	public SqlStatement setFloat(Float x) throws SQLException {
		return setWithType(x, Types.FLOAT);
	}

	public SqlStatement setDouble(Double x) throws SQLException {
		return setWithType(x, Types.DOUBLE);
	}

	public SqlStatement setString(String x) throws SQLException {
		return setWithType(x, Types.VARCHAR);
	}

	public SqlStatement setText(String x) throws SQLException {
		return setWithType(x, Types.CLOB);
	}

	public SqlStatement setBytes(byte[] x) throws SQLException {
		return setWithType(x, Types.VARBINARY);
	}

	public SqlStatement setDate(Date x) throws SQLException {
		return setWithType(x, Types.DATE);
	}

	public SqlStatement setTime(Time x) throws SQLException {
		return setWithType(x, Types.TIME);
	}

	public SqlStatement setTimestamp(Timestamp x) throws SQLException {
		return setWithType(x, Types.TIMESTAMP);
	}

	public SqlStatement setBlob(Blob x) throws SQLException {
		return setWithType(x, Types.BLOB);
	}

	public SqlStatement setClob(Clob x) throws SQLException {
		return setWithType(x, Types.CLOB);
	}

	public SqlStatement set(Object... xs) throws SQLException {
		for (Object x : xs)
			if (!castAccept(SqlNull.class, x, this::setNull)) setWithoutType(x);
		return this;
	}

	private SqlStatement setNull(SqlNull value) throws SQLException {
		return setWithType(null, value.sqlType.value);
	}

	private SqlStatement setWithoutType(Object x) throws SQLException {
		track(x);
		ps.setObject(incIndex(), x);
		return this;
	}

	private SqlStatement setWithType(Object x, int sqlType) throws SQLException {
		track(x);
		ps.setObject(incIndex(), x, sqlType);
		return this;
	}

	private SqlStatement track(Object x) {
		if (values != null) values[index - 1] = x;
		return this;
	}

	private int incIndex() {
		return index++;
	}

	@Override
	public void close() throws SQLException {
		ps.close();
	}

	@Override
	public String toString() {
		if (values == null) return sql;
		return RegexUtil.replaceAll(Q_REGEX, sql, (m, i) -> formatter.format(values[i]));
	}
}
