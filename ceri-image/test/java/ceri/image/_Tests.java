package ceri.image;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.test.TestUtil;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	// other test suites
	//ceri.common._Tests.class,
	// ceri.image
	CropParamsBehavior.class,
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
