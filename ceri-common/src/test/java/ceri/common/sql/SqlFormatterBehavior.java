package ceri.common.sql;

import static ceri.common.sql.SqlType.sqlClob;
import static ceri.common.sql.SqlType.sqlNumeric;
import static ceri.common.sql.SqlType.sqlVarChar;
import static ceri.common.test.AssertUtil.assertEquals;
import java.sql.Types;
import org.junit.Test;
import ceri.common.text.StringUtil;

public class SqlFormatterBehavior {
	private final SqlFormatter formatter = new SqlFormatter() {
		@Override
		public String format(Object field) {
			return formatByClass(field);
		}

		@Override
		public String format(Object obj, SqlType type) {
			return formatBySqlType(obj, type);
		}
	};

	@Test
	public void shouldIgnoreTypeByDefault() {
		SqlFormatter f = obj -> StringUtil.reverse(String.valueOf(obj));
		assertEquals(f.format(null), "llun");
		assertEquals(f.format(123), "321");
		assertEquals(f.format(123, Types.DATE), "321");
	}

	@Test
	public void shouldFormatByClass() {
		assertEquals(formatter.format(null), "null");
		assertEquals(formatter.format("test"), "String:test");
		assertEquals(formatter.format(9), "Number:9");
	}

	@Test
	public void shouldFormatBySqlType() {
		assertEquals(formatter.format(null, sqlVarChar), "VarChar:null");
		assertEquals(formatter.format(null, sqlClob), "null");
		assertEquals(formatter.format("test", sqlVarChar), "VarChar:test");
		assertEquals(formatter.format("test", sqlClob), "test");
		assertEquals(formatter.format(9, sqlNumeric), "Numeric:9");
	}

	private String formatByClass(Object obj) {
		if (obj instanceof String) return "String:" + obj;
		if (obj instanceof Number) return "Number:" + obj;
		return String.valueOf(obj);
	}

	private String formatBySqlType(Object obj, SqlType type) {
		if (type == sqlVarChar) return "VarChar:" + obj;
		if (type == sqlNumeric) return "Numeric:" + obj;
		return String.valueOf(obj);
	}

}
