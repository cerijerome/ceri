package ceri.common.sql;

import static ceri.common.test.TestUtil.assertIterable;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import org.junit.Test;

public class SqlUtilTest {

	@Test
	public void testNow() {
		Timestamp t0 = new Timestamp(System.currentTimeMillis() - 1000);
		assertTrue(SqlUtil.now().after(t0));
	}

	@Test
	public void testType() throws SQLException {
		@SuppressWarnings("resource")
		ResultSet rs = mock(ResultSet.class);
		ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
		when(rs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnType(anyInt())).thenReturn(Types.VARCHAR);
		assertThat(SqlUtil.type(rs, 1), is(SqlType.sqlVarChar));
	}

	@Test
	public void testTableNames() throws SQLException {
		@SuppressWarnings("resource")
		Connection con = mock(Connection.class);
		DatabaseMetaData dbmd = mock(DatabaseMetaData.class);
		@SuppressWarnings("resource")
		ResultSet rs = mock(ResultSet.class);
		when(con.getMetaData()).thenReturn(dbmd);
		when(dbmd.getTables(any(), any(), any(), any())).thenReturn(rs);
		when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
		when(rs.getString(anyInt())).thenReturn("T1").thenReturn("T2").thenReturn("T3");
		assertIterable(SqlUtil.tableNames(con), "T1", "T2", "T3");
	}
}