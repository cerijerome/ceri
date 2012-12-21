package ceri.aws;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.aws.glacier.MultiPartUploaderBehavior;
import ceri.aws.util.AwsUtilTest;
import ceri.aws.util.ByteRangeBehavior;
import ceri.aws.util.TarUtilTest;
import ceri.aws.util.TarringInputStreamBehavior;
import ceri.common.test.TestUtil;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	// other test suites
	ceri.common._Tests.class,
	// glacier
	MultiPartUploaderBehavior.class,
	// util
	AwsUtilTest.class,
	ByteRangeBehavior.class,
	TarringInputStreamBehavior.class,
	TarUtilTest.class,
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
