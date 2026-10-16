package ceri.ent;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import ceri.common.test.Testing;
import ceri.ent.json.JsonTest;

@Suite
@SelectClasses({ JsonTest.class, })
public class _Tests {
	public static void main(String... args) {
		Testing.print(_Tests.class);
	}
}
