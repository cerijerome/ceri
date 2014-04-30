package ceri.ci;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.ci.alert.AlertPropertiesBehavior;
import ceri.ci.alert.AlertServiceBehavior;
import ceri.ci.alert.AlerterGroupBehavior;
import ceri.ci.audio.AudioAlerterBehavior;
import ceri.ci.audio.AudioBehavior;
import ceri.ci.audio.AudioMessageBehavior;
import ceri.ci.audio.AudioPropertiesBehavior;
import ceri.ci.build.AnalyzedJobBehavior;
import ceri.ci.build.BuildAnalyzerBehavior;
import ceri.ci.build.BuildBehavior;
import ceri.ci.build.BuildEventBehavior;
import ceri.ci.build.BuildUtilTest;
import ceri.ci.build.BuildsBehavior;
import ceri.ci.build.EventBehavior;
import ceri.ci.build.EventComparatorsTest;
import ceri.ci.build.JobBehavior;
import ceri.ci.proxy.MultiProxyBehavior;
import ceri.ci.proxy.MultiProxyPropertiesBehavior;
import ceri.ci.web.WebAlerterBehavior;
import ceri.ci.web.WebAlerterPropertiesBehavior;
import ceri.ci.x10.X10AlerterBehavior;
import ceri.ci.x10.X10ContainerBehavior;
import ceri.ci.x10.X10PropertiesBehavior;
import ceri.ci.zwave.ZWaveAlerterBehavior;
import ceri.ci.zwave.ZWavePropertiesBehavior;
import ceri.common.test.TestUtil;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// alert
	AlerterGroupBehavior.class,
	AlertServiceBehavior.class,
	AlertPropertiesBehavior.class,
	// audio
	AudioAlerterBehavior.class,
	AudioPropertiesBehavior.class,
	AudioBehavior.class,
	AudioMessageBehavior.class,
	// build
	AnalyzedJobBehavior.class,
	BuildAnalyzerBehavior.class,
	BuildBehavior.class,
	BuildEventBehavior.class,
	BuildsBehavior.class,
	BuildUtilTest.class,
	EventBehavior.class,
	EventComparatorsTest.class,
	JobBehavior.class,
	// proxy
	MultiProxyBehavior.class,
	MultiProxyPropertiesBehavior.class,
	// servlet
	// web
	WebAlerterBehavior.class,
	WebAlerterPropertiesBehavior.class,
	// x10
	X10AlerterBehavior.class,
	X10ContainerBehavior.class,
	X10PropertiesBehavior.class,
	// zwave
	ZWaveAlerterBehavior.class,
	ZWavePropertiesBehavior.class,
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
