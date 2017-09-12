package ceri.ent;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;
import ceri.ent.json.GsonUtilTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	GsonUtilTest.class,
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
