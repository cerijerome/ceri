package ceri.ci;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.ci.audio.AudioAlerterBehavior;
import ceri.ci.audio.AudioBehavior;
import ceri.ci.audio.AudioMessageBehavior;
import ceri.ci.audio.ClipBehavior;
import ceri.ci.audio.JobAnalyzerBehavior;
import ceri.ci.build.BuildBehavior;
import ceri.ci.build.BuildUtilTest;
import ceri.ci.build.BuildsBehavior;
import ceri.ci.build.EventBehavior;
import ceri.ci.build.EventComparatorsTest;
import ceri.ci.build.JobBehavior;
import ceri.ci.x10.X10AlerterBehavior;
import ceri.ci.x10.X10AlerterPropertiesBehavior;
import ceri.ci.zwave.ZWaveAlerterBehavior;
import ceri.ci.zwave.ZWaveAlerterPropertiesBehavior;
import ceri.common.test.TestUtil;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// audio
	AudioAlerterBehavior.class,
	AudioBehavior.class,
	AudioMessageBehavior.class,
	ClipBehavior.class,
	JobAnalyzerBehavior.class,
	// build
	BuildBehavior.class,
	BuildsBehavior.class,
	BuildUtilTest.class,
	EventBehavior.class,
	EventComparatorsTest.class,
	JobBehavior.class,
	// service
	// web
	// x10
	X10AlerterBehavior.class,
	X10AlerterPropertiesBehavior.class,
	// zwave
	ZWaveAlerterBehavior.class,
	ZWaveAlerterPropertiesBehavior.class,
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
