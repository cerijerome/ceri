package ceri.common.sql;

import static ceri.common.sql.SqlNull.nullClob;
import static ceri.common.sql.SqlNull.nullDate;
import static ceri.common.sql.SqlNull.nullInt;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertThat;
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
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SqlStatementBehavior {
	private static PreparedStatement ps;
	private static Connection con;
	private ArgumentCaptor<Integer> indexes;
	private ArgumentCaptor<Object> objects;
	private ArgumentCaptor<Integer> types;

	@SuppressWarnings("resource")
	@BeforeClass
	public static void beforeClass() throws SQLException {
		ps = Mockito.mock(PreparedStatement.class);
		con = Mockito.mock(Connection.class);
		when(con.prepareStatement(any())).thenReturn(ps);
	}

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		Mockito.clearInvocations(ps); // to reduce test times
		indexes = ArgumentCaptor.forClass(Integer.class);
		objects = ArgumentCaptor.forClass(Object.class);
		types = ArgumentCaptor.forClass(Integer.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateWithoutParameters() throws SQLException {
		try (SqlStatement stmt = SqlStatement.of(con, "select * from %s", "table1")) {
			assertThat(stmt.fields(), is(0));
			assertThat(stmt.toString(), is("select * from table1"));
			assertThat(stmt.jdbc(), is(ps));
		}
		verify(ps).close();
	}

	@Test
	public void shouldCreateWithParameters() throws SQLException {
		try (SqlStatement stmt =
			SqlStatement.of(con, "select ?, ?, ?, ?, ?, ?, ? from %s", "table1")) {
			assertThat(stmt.sql, is("select ?, ?, ?, ?, ?, ?, ? from table1"));
			assertThat(stmt.toString(), is("select ?, ?, ?, ?, ?, ?, ? from table1"));
			assertThat(stmt.fields(), is(7));
		}
		verify(ps).close();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSetParameters() throws SQLException {
		Blob blob = mock(Blob.class);
		Clob clob = mock(Clob.class);
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
			verify(ps, times(8)).setObject(indexes.capture(), objects.capture(), types.capture());
			assertIterable(indexes.getAllValues(), 1, 3, 4, 6, 7, 8, 9, 10);
			assertIterable(objects.getAllValues(), date, blob, clob, timestamp, bigD, time, s, txt);
			assertIterable(types.getAllValues(), DATE, BLOB, CLOB, TIMESTAMP, NUMERIC, TIME,
				VARCHAR, CLOB);
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
			verify(ps, times(8)).setObject(indexes.capture(), objects.capture(), types.capture());
			assertIterable(indexes.getAllValues(), 1, 2, 3, 6, 7, 8, 9, 10);
			assertIterable(objects.getAllValues(), bytes, bo, bt, sh, it, lg, fl, db);
			assertIterable(types.getAllValues(), VARBINARY, BIT, TINYINT, SMALLINT, INTEGER, BIGINT,
				FLOAT, DOUBLE);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSetNullParameters() throws SQLException {
		try (SqlStatement stmt = SqlStatement.track(con, "select ?, ?, ?, ?, ? from table1")) {
			stmt.set(1, nullClob, "x", nullDate, nullInt);
			verify(ps, times(2)).setObject(indexes.capture(), objects.capture());
			verify(ps, times(3)).setObject(indexes.capture(), objects.capture(), types.capture());
			assertIterable(indexes.getAllValues(), 1, 3, 2, 4, 5);
			assertIterable(objects.getAllValues(), 1, "x", null, null, null);
			assertIterable(types.getAllValues(), CLOB, DATE, INTEGER);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldTrackParameters() throws SQLException {
		try (SqlStatement stmt = SqlStatement.track(con, "select ?, ?, ? from table1")) {
			stmt.set(1, "x", SqlNull.nullClob);
			assertThat(stmt.toString(), is("select 1, x, null from table1"));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAllowFormatterToBeAdded() throws SQLException {
		try (SqlStatement stmt =
			SqlStatement.of(con, "select ?, ?, ? from table1").with(SqlFormatter.DEFAULT)) {
			stmt.set(1, "x", SqlNull.nullClob);
			assertThat(stmt.toString(), is("select 1, x, null from table1"));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSupportBatchetNullParameters() throws SQLException {
		try (SqlStatement stmt = SqlStatement.track(con, "select ?, ?, ? from table1")) {
			stmt.set(1, nullDate, "x").batch();
			assertThat(stmt.toString(), is("select 1, null, x from table1"));
			stmt.set(2).batch();
			assertThat(stmt.toString(), is("select 2, null, x from table1"));
			stmt.skip(2).set("z").batch();
			assertThat(stmt.toString(), is("select 2, null, z from table1"));

			verify(ps, times(1)).setObject(indexes.capture(), objects.capture(), types.capture());
			verify(ps, times(4)).setObject(indexes.capture(), objects.capture());
			assertIterable(indexes.getAllValues(), 2, 1, 3, 1, 3);
			assertIterable(objects.getAllValues(), null, 1, "x", 2, "z");
			assertIterable(types.getAllValues(), DATE);
		}
	}

}
