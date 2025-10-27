package ceri.common.sql;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.text.Strings;

public class SqlTest {

	private final Sql.Formatter formatter = new Sql.Formatter() {
		@Override
		public String format(Object field) {
			return formatByClass(field);
		}

		@Override
		public String format(Object obj, Sql.Type type) {
			return formatBySqlType(obj, type);
		}
	};

	@Test
	public void testFormatIgnoreTypeByDefault() {
		Sql.Formatter f = obj -> Strings.reverse(String.valueOf(obj));
		Assert.equal(f.format(null), "llun");
		Assert.equal(f.format(123), "321");
		Assert.equal(f.format(123, Types.DATE), "321");
	}

	@Test
	public void testFormatByClass() {
		Assert.equal(formatter.format(null), "null");
		Assert.equal(formatter.format("test"), "String:test");
		Assert.equal(formatter.format(9), "Number:9");
	}

	@Test
	public void testFormatBySqlType() {
		Assert.equal(formatter.format(null, Sql.Type.VarChar), "VarChar:null");
		Assert.equal(formatter.format(null, Sql.Type.Clob), "null");
		Assert.equal(formatter.format("test", Sql.Type.VarChar), "VarChar:test");
		Assert.equal(formatter.format("test", Sql.Type.Clob), "test");
		Assert.equal(formatter.format(9, Sql.Type.Numeric), "Numeric:9");
	}

	@Test
	public void testNow() {
		var t0 = new Timestamp(System.currentTimeMillis() - 1000);
		Assert.yes(Sql.now().after(t0));
	}

	@Test
	public void testType() throws SQLException {
		try (var rs = TestResultSet.of()) {
			var rsmd = TestResultSetMetaData.of();
			rs.getMetaData.autoResponses(rsmd);
			rsmd.getColumnType.autoResponses(Types.VARCHAR);
			Assert.equal(Sql.type(rs, 1), Sql.Type.VarChar);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testTableNames() throws SQLException {
		var con = TestConnection.of();
		var dbmd = TestDatabaseMetaData.of();
		var rs = TestResultSet.of();
		con.getMetaData.autoResponses(dbmd);
		dbmd.getTables.autoResponses(rs);
		rs.next.autoResponses(true, true, true, false);
		rs.getString.autoResponses("T1", "T2", "T3");
		Assert.ordered(Sql.tableNames(con), "T1", "T2", "T3");
	}
	
	private String formatByClass(Object obj) {
		if (obj instanceof String) return "String:" + obj;
		if (obj instanceof Number) return "Number:" + obj;
		return String.valueOf(obj);
	}

	private String formatBySqlType(Object obj, Sql.Type type) {
		if (type == Sql.Type.VarChar) return "VarChar:" + obj;
		if (type == Sql.Type.Numeric) return "Numeric:" + obj;
		return String.valueOf(obj);
	}	
}
