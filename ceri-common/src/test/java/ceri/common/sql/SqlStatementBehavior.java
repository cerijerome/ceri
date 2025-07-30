package ceri.common.sql;

import static ceri.common.sql.SqlNull.nullClob;
import static ceri.common.sql.SqlNull.nullDate;
import static ceri.common.sql.SqlNull.nullInt;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOrdered;
import static java.sql.Types.BIGINT;
import static java.sql.Types.BIT;
import static java.sql.Types.BLOB;
import static java.sql.Types.CLOB;
import static java.sql.Types.DATE;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TIME;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.TINYINT;
import static java.sql.Types.VARBINARY;
import static java.sql.Types.VARCHAR;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class SqlStatementBehavior {
	private TestConnection con;
	private TestPreparedStatement ps;

	@Before
	public void before() {
		ps = TestPreparedStatement.of();
		con = TestConnection.of();
		con.prepareStatement.autoResponses(ps);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateWithoutParameters() throws SQLException {
		try (SqlStatement stmt = SqlStatement.of(con, "select * from %s", "table1")) {
			assertEquals(stmt.fields(), 0);
			assertEquals(stmt.toString(), "select * from table1");
			assertEquals(stmt.jdbc(), ps);
		}
		ps.close.awaitAuto();
	}

	@Test
	public void shouldCreateWithParameters() throws SQLException {
		try (SqlStatement stmt =
			SqlStatement.of(con, "select ?, ?, ?, ?, ?, ?, ? from %s", "table1")) {
			assertEquals(stmt.sql, "select ?, ?, ?, ?, ?, ?, ? from table1");
			assertEquals(stmt.toString(), "select ?, ?, ?, ?, ?, ?, ? from table1");
			assertEquals(stmt.fields(), 7);
		}
		ps.close.awaitAuto();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSetParameters() throws SQLException {
		Blob blob = TestBlob.of();
		Clob clob = TestClob.of();
		Date date = new Date(77777);
		BigDecimal bigD = new BigDecimal(100);
		Timestamp timestamp = new Timestamp(99999999);
		Time time = new Time(12345);
		String s = "test";
		String txt = "testing";
		try (SqlStatement stmt = SqlStatement.of(con, // 10 params
			"select ?, ?, ?, ?, ?, ?, ?, ?, ?, ? from %s", "table1")) {
			stmt.setDate(date).skip().setBlob(blob).setClob(clob).skip().setTimestamp(timestamp)
				.setBigDecimal(bigD).setTime(time).setString(s).setText(txt);
			assertOrdered(ps.setObject.values(), //
				List.of(1, date, DATE), //
				List.of(3, blob, BLOB), //
				List.of(4, clob, CLOB), //
				List.of(6, timestamp, TIMESTAMP), //
				List.of(7, bigD, NUMERIC), //
				List.of(8, time, TIME), //
				List.of(9, s, VARCHAR), //
				List.of(10, txt, CLOB));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSetPrimitiveParameters() throws SQLException {
		byte[] bytes = new byte[] { 1, 2, 3 };
		boolean bo = true;
		byte bt = -1;
		short sh = 77;
		int it = 123456;
		long lg = 987654321;
		float fl = 0.02f;
		double db = 1.9;
		try (SqlStatement stmt = SqlStatement.of(con, // 10 params
			"select ?, ?, ?, ?, ?, ?, ?, ?, ?, ? from %s", "table1")) {
			stmt.setBytes(bytes).setBoolean(bo).setByte(bt).skip(2).setShort(sh).setInt(it)
				.setLong(lg).setFloat(fl).setDouble(db);
			assertOrdered(ps.setObject.values(), //
				List.of(1, bytes, VARBINARY), //
				List.of(2, bo, BIT), //
				List.of(3, bt, TINYINT), //
				List.of(6, sh, SMALLINT), //
				List.of(7, it, INTEGER), //
				List.of(8, lg, BIGINT), //
				List.of(9, fl, FLOAT), //
				List.of(10, db, DOUBLE));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSetNullParameters() throws SQLException {
		try (SqlStatement stmt = SqlStatement.track(con, "select ?, ?, ?, ?, ? from table1")) {
			stmt.set(1, nullClob, "x", nullDate, nullInt);
			assertOrdered(ps.setObject.values(), //
				Arrays.asList(1, 1), //
				Arrays.asList(2, null, CLOB), //
				Arrays.asList(3, "x"), //
				Arrays.asList(4, null, DATE), //
				Arrays.asList(5, null, INTEGER));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldTrackParameters() throws SQLException {
		try (SqlStatement stmt = SqlStatement.track(con, "select ?, ?, ? from table1")) {
			stmt.set(1, "x", SqlNull.nullClob);
			assertEquals(stmt.toString(), "select 1, x, null from table1");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAllowFormatterToBeAdded() throws SQLException {
		try (SqlStatement stmt =
			SqlStatement.of(con, "select ?, ?, ? from table1").with(SqlFormatter.DEFAULT)) {
			stmt.set(1, "x", SqlNull.nullClob);
			assertEquals(stmt.toString(), "select 1, x, null from table1");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSupportBatchetNullParameters() throws SQLException {
		try (SqlStatement stmt = SqlStatement.track(con, "select ?, ?, ? from table1")) {
			stmt.set(1, nullDate, "x").batch();
			assertEquals(stmt.toString(), "select 1, null, x from table1");
			stmt.set(2).batch();
			assertEquals(stmt.toString(), "select 2, null, x from table1");
			stmt.skip(2).set("z").batch();
			assertEquals(stmt.toString(), "select 2, null, z from table1");
			assertOrdered(ps.setObject.values(), //
				Arrays.asList(1, 1), //
				Arrays.asList(2, null, DATE), //
				Arrays.asList(3, "x"), //
				Arrays.asList(1, 2), //
				Arrays.asList(3, "z"));
		}
	}

}
