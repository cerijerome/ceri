package ceri.common.sql;

import static ceri.common.sql.SqlType.sqlClob;
import static ceri.common.sql.SqlType.sqlNumeric;
import static ceri.common.sql.SqlType.sqlVarChar;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
		assertThat(f.format(null), is("llun"));
		assertThat(f.format(123), is("321"));
		assertThat(f.format(123, Types.DATE), is("321"));
	}

	@Test
	public void shouldFormatByClass() {
		assertThat(formatter.format(null), is("null"));
		assertThat(formatter.format("test"), is("String:test"));
		assertThat(formatter.format(9), is("Number:9"));
	}

	@Test
	public void shouldFormatBySqlType() {
		assertThat(formatter.format(null, sqlVarChar), is("VarChar:null"));
		assertThat(formatter.format(null, sqlClob), is("null"));
		assertThat(formatter.format("test", sqlVarChar), is("VarChar:test"));
		assertThat(formatter.format("test", sqlClob), is("test"));
		assertThat(formatter.format(9, sqlNumeric), is("Numeric:9"));
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
