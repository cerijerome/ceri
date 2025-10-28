package ceri.ent;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.Testing;
import ceri.ent.json.JsonUtilTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ JsonUtilTest.class, })
public class _Tests {
	public static void main(String... args) {
		Testing.exec(_Tests.class);
	}
}
