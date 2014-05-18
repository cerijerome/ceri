package ceri.ci.sample;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;
import ceri.ci.alert.AlertContainer;
import ceri.ci.audio.AudioContainer;
import ceri.ci.audio.AudioListener;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Event;
import ceri.ci.phone.PhoneContainer;
import ceri.ci.zwave.ZWaveContainer;
import ceri.common.property.PropertyUtil;
import ceri.common.util.BasicUtil;

/**
 * Sample alert system that parses emails to trigger alerts. <code>SampleMain.properties</code>
 * determines which alert components are enabled, and their configuration. Available components are
 * audio, z-wave, and SMS alerts.
 * <p/>
 * Configure your CI pipeline (for example Jenkins) to send emails to an IMAP-supported email
 * account whenever a build event occurs. The email subject should be of the form:
 * 
 * <pre>
 * Sample: build-name job-name [fixed|broken]
 * </pre>
 * 
 * in order to trigger an alert. The content of the email should contain a comma-separated list of
 * committers to be listed in alert event.
 * <p/>
 * The email processor polls the email account every 30 seconds for new events.
 * <p/>
 * Z-wave alerts turn z-wave devices on for users who broke a job, and turns them off when a job is
 * fixed. It requires a VeraLite home automation gateway device to communicate with the devices. The
 * device ids are the VeraLite paired device ids.
 * <p/>
 * Additional device ids may assigned to turn on while the audio alert is active, and turn off when
 * the audio alert completes.
 * <p/>
 * SMS alerts send messages to user phone numbers using the Twilio service when a job becomes
 * broken. A Twilio account is required if this alert is enabled.
 */
public class SampleMain implements Closeable {
	private final ZWaveContainer zwave;
	private final AudioContainer audio;
	private final PhoneContainer phone;
	private final AlertContainer master;

	public static void main(String[] args) throws Exception {
		// Creating fakes event to demonstrate alerts
		try (SampleMain container = new SampleMain()) {
			container.master.alert().process(
				new BuildEvent("master", "commit", Event.failure("user1")));
			BasicUtil.delay(15000);
			container.master.alert().process(
				new BuildEvent("master", "commit", Event.failure("user2")));
			BasicUtil.delay(15000);
			container.master.alert().process(
				new BuildEvent("master", "commit", Event.success("user1", "user2")));
			BasicUtil.delay(15000);
		}
	}

	public SampleMain() throws IOException {
		Properties properties = PropertyUtil.load(getClass());
		AlertContainer.Builder builder = AlertContainer.builder(properties);
		zwave = new ZWaveContainer(builder.properties);
		AudioListener audioListener =
			zwave.group == null ? null : zwave.group.createAudioListener();
		audio = new AudioContainer(builder.properties, getClass(), audioListener);
		phone = new PhoneContainer(builder.properties);
		builder.alerters(zwave.alerter, audio.alerter, phone.alerter);
		builder.parsers(new SampleEmailParser());
		master = builder.build();
	}

	@Override
	public void close() {
		audio.close();
		master.close();
	}

}
