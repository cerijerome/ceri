package ceri.ci;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.ci.admin.AdminServletBehavior;
import ceri.ci.admin.CommandFactoryBehavior;
import ceri.ci.admin.ParamsBehavior;
import ceri.ci.admin.ResponseBehavior;
import ceri.ci.admin.SerializerBehavior;
import ceri.ci.alert.AlertContainerBehavior;
import ceri.ci.alert.AlertPropertiesBehavior;
import ceri.ci.alert.AlertServiceContainerBehavior;
import ceri.ci.alert.AlertServiceImplBehavior;
import ceri.ci.alert.AlerterGroupBehavior;
import ceri.ci.alert.NodeBehavior;
import ceri.ci.audio.AudioAlerterBehavior;
import ceri.ci.audio.AudioBehavior;
import ceri.ci.audio.AudioContainerBehavior;
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
import ceri.ci.common.ResourceMapBehavior;
import ceri.ci.email.EmailBehavior;
import ceri.ci.email.EmailContainerBehavior;
import ceri.ci.email.EmailPropertiesBehavior;
import ceri.ci.email.EmailRetrieverImplBehavior;
import ceri.ci.email.EmailServiceBehavior;
import ceri.ci.email.EmailUtilTest;
import ceri.ci.phone.PhoneAlerterBehavior;
import ceri.ci.phone.PhoneContainerBehavior;
import ceri.ci.phone.PhonePropertiesBehavior;
import ceri.ci.phone.TwilioClientBehavior;
import ceri.ci.web.ActorBehavior;
import ceri.ci.web.ActorComparatorsTest;
import ceri.ci.web.AnalyzedActorsBehavior;
import ceri.ci.web.WebAlerterBehavior;
import ceri.ci.web.WebAlerterPropertiesBehavior;
import ceri.ci.web.WebContainerBehavior;
import ceri.ci.x10.X10AlerterBehavior;
import ceri.ci.x10.X10ContainerBehavior;
import ceri.ci.x10.X10PropertiesBehavior;
import ceri.ci.zwave.ZWaveAlerterBehavior;
import ceri.ci.zwave.ZWaveContainerBehavior;
import ceri.ci.zwave.ZWaveGroupBehavior;
import ceri.ci.zwave.ZWavePropertiesBehavior;
import ceri.common.test.TestUtil;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// admin
	AdminServletBehavior.class,
	CommandFactoryBehavior.class,
	ParamsBehavior.class,
	ResponseBehavior.class,
	SerializerBehavior.class,
	// alert
	AlertContainerBehavior.class,
	AlerterGroupBehavior.class,
	AlertServiceImplBehavior.class,
	AlertServiceContainerBehavior.class,
	AlertPropertiesBehavior.class,
	NodeBehavior.class,
	// audio
	AudioAlerterBehavior.class,
	AudioBehavior.class,
	AudioContainerBehavior.class,
	AudioMessageBehavior.class,
	AudioPropertiesBehavior.class,
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
	// common
	ResourceMapBehavior.class,
	// email
	EmailBehavior.class,
	EmailContainerBehavior.class,
	EmailPropertiesBehavior.class,
	EmailRetrieverImplBehavior.class,
	EmailServiceBehavior.class,
	EmailUtilTest.class,
	// phone
	PhoneAlerterBehavior.class,
	PhoneContainerBehavior.class,
	PhonePropertiesBehavior.class,
	TwilioClientBehavior.class,
	// web
	ActorBehavior.class,
	ActorComparatorsTest.class,
	AnalyzedActorsBehavior.class,
	WebAlerterBehavior.class,
	WebAlerterPropertiesBehavior.class,
	WebContainerBehavior.class,
	// x10
	X10AlerterBehavior.class,
	X10ContainerBehavior.class,
	X10PropertiesBehavior.class,
	// zwave
	ZWaveAlerterBehavior.class,
	ZWaveContainerBehavior.class,
	ZWaveGroupBehavior.class,
	ZWavePropertiesBehavior.class,
})
public class _Tests {
	public static void main(String... args) {
		TestUtil.exec(_Tests.class);
	}
}
