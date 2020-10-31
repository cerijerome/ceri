package ceri.common.sql;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertTrue;
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
		try (TestResultSet rs = TestResultSet.of()) {
			TestResultSetMetaData rsmd = TestResultSetMetaData.of();
			rs.getMetaData.autoResponses(rsmd);
			rsmd.getColumnType.autoResponses(Types.VARCHAR);
			assertEquals(SqlUtil.type(rs, 1), SqlType.sqlVarChar);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testTableNames() throws SQLException {
		TestConnection con = TestConnection.of();
		TestDatabaseMetaData dbmd = TestDatabaseMetaData.of();
		TestResultSet rs = TestResultSet.of();
		con.getMetaData.autoResponses(dbmd);
		dbmd.getTables.autoResponses(rs);
		rs.next.autoResponses(true, true, true, false);
		rs.getString.autoResponses("T1", "T2", "T3");
		assertIterable(SqlUtil.tableNames(con), "T1", "T2", "T3");
	}
}
