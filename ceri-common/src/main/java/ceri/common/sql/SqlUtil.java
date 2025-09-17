package ceri.common.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.stream.Stream;

public class SqlUtil {
	private static final int TABLE_NAME_INDEX = 3;
	private static final String TABLE_TYPE = "TABLE";
	public static final ExceptionAdapter<SQLException> SQL_ADAPTER =
		ExceptionAdapter.of(SQLException.class, SQLException::new);

	private SqlUtil() {}

	public static Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}

	public static SqlType type(ResultSet rs, int index) throws SQLException {
		return SqlType.from(rs.getMetaData().getColumnType(index));
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
