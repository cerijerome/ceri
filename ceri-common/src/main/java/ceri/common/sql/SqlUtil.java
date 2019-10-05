package ceri.common.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SqlUtil {

	private SqlUtil() {}

	public static Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}

	public static SqlType type(ResultSet rs, int index) throws SQLException {
		return SqlType.from(rs.getMetaData().getColumnType(index));
	}
	
}
