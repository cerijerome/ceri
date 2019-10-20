package ceri.common.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SqlUtil {
	private static final int TABLE_NAME_INDEX = 3;
	private static final String TABLE_TYPE = "TABLE";


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
		List<String> tables = new ArrayList<>();
		try (ResultSet rs = con.getMetaData().getTables(
			catalog, schemaPattern, namePattern, types)) {
			while (rs.next())
				tables.add(rs.getString(TABLE_NAME_INDEX));
		}
		return tables;
	}

}
