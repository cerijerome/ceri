package ceri.common.sql;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertString;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class SqlStatementBehavior {
	private TestConnection con;
	private TestPreparedStatement ps;
	private SqlStatement stmt;

	@After
	public void after() {
		stmt = TestUtil.close(stmt);
		ps = TestUtil.close(ps);
		con = TestUtil.close(con);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateWithoutParameters() throws SQLException {
		init();
		stmt = SqlStatement.of(con, "select * from %s", "table1");
		assertEquals(stmt.fields(), 0);
		assertString(stmt, "select * from table1");
		assertEquals(stmt.jdbc(), ps);
		stmt.close();
		ps.close.awaitAuto();
	}

	@Test
	public void shouldCreateWithParameters() throws SQLException {
		init();
		stmt = SqlStatement.of(con, "select ?, ?, ?, ?, ?, ?, ? from %s", "table1"); // 7
		assertString(stmt.sql, "select ?, ?, ?, ?, ?, ?, ? from table1");
		assertString(stmt, "select ?, ?, ?, ?, ?, ?, ? from table1");
		assertEquals(stmt.fields(), 7);
		stmt.close();
		ps.close.awaitAuto();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSetParameters() throws SQLException {
		init();
		Blob blob = TestBlob.of();
		Clob clob = TestClob.of();
		Date date = new Date(77777);
		BigDecimal bigD = new BigDecimal(100);
		Timestamp timestamp = new Timestamp(99999999);
		Time time = new Time(12345);
		String s = "test";
		String txt = "testing";
		stmt = SqlStatement.of(con, "select ?, ?, ?, ?, ?, ?, ?, ?, ?, ? from %s", "table1"); // 10
		stmt.setDate(date).skip().setBlob(blob).setClob(clob).skip().setTimestamp(timestamp)
			.setBigDecimal(bigD).setTime(time).setString(s).setText(txt);
		assertOrdered(ps.setObject.values(), //
			List.of(1, date, Types.DATE), //
			List.of(3, blob, Types.BLOB), //
			List.of(4, clob, Types.CLOB), //
			List.of(6, timestamp, Types.TIMESTAMP), //
			List.of(7, bigD, Types.NUMERIC), //
			List.of(8, time, Types.TIME), //
			List.of(9, s, Types.VARCHAR), //
			List.of(10, txt, Types.CLOB));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSetPrimitiveParameters() throws SQLException {
		init();
		byte[] bytes = new byte[] { 1, 2, 3 };
		boolean bo = true;
		byte bt = -1;
		short sh = 77;
		int it = 123456;
		long lg = 987654321;
		float fl = 0.02f;
		double db = 1.9;
		stmt = SqlStatement.of(con, "select ?, ?, ?, ?, ?, ?, ?, ?, ?, ? from %s", "table1"); // 10
		stmt.setBytes(bytes).setBoolean(bo).setByte(bt).skip(2).setShort(sh).setInt(it).setLong(lg)
			.setFloat(fl).setDouble(db);
		assertOrdered(ps.setObject.values(), //
			List.of(1, bytes, Types.VARBINARY), //
			List.of(2, bo, Types.BIT), //
			List.of(3, bt, Types.TINYINT), //
			List.of(6, sh, Types.SMALLINT), //
			List.of(7, it, Types.INTEGER), //
			List.of(8, lg, Types.BIGINT), //
			List.of(9, fl, Types.FLOAT), //
			List.of(10, db, Types.DOUBLE));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSetNullParameters() throws SQLException {
		init();
		stmt = SqlStatement.track(con, "select ?, ?, ?, ?, ? from table1"); // 5
		stmt.set(1, SqlNull.nullClob, "x", SqlNull.nullDate, SqlNull.nullInt);
		assertOrdered(ps.setObject.values(), //
			Arrays.asList(1, 1), //
			Arrays.asList(2, null, Types.CLOB), //
			Arrays.asList(3, "x"), //
			Arrays.asList(4, null, Types.DATE), //
			Arrays.asList(5, null, Types.INTEGER));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldTrackParameters() throws SQLException {
		init();
		stmt = SqlStatement.track(con, "select ?, ?, ? from table1");
		stmt.set(1, "x", SqlNull.nullClob);
		assertString(stmt, "select 1, x, null from table1");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAllowFormatterToBeAdded() throws SQLException {
		init();
		stmt = SqlStatement.of(con, "select ?, ?, ? from table1").with(SqlFormatter.DEFAULT);
		stmt.set(1, "x", SqlNull.nullClob);
		assertString(stmt, "select 1, x, null from table1");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSupportBatchetNullParameters() throws SQLException {
		init();
		stmt = SqlStatement.track(con, "select ?, ?, ? from table1");
		stmt.set(1, SqlNull.nullDate, "x").batch();
		assertString(stmt, "select 1, null, x from table1");
		stmt.set(2).batch();
		assertString(stmt, "select 2, null, x from table1");
		stmt.skip(2).set("z").batch();
		assertString(stmt, "select 2, null, z from table1");
		assertOrdered(ps.setObject.values(), //
			Arrays.asList(1, 1), //
			Arrays.asList(2, null, Types.DATE), //
			Arrays.asList(3, "x"), //
			Arrays.asList(1, 2), //
			Arrays.asList(3, "z"));
	}

	private void init() {
		ps = TestPreparedStatement.of();
		con = TestConnection.of();
		con.prepareStatement.autoResponses(ps);
	}
}
