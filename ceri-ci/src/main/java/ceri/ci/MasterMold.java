package ceri.ci;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import x10.Controller;
import ceri.ci.alert.AlertService;
import ceri.ci.alert.AlertServiceProperties;
import ceri.ci.alert.Alerters;
import ceri.ci.audio.AudioAlerter;
import ceri.ci.audio.AudioAlerterProperties;
import ceri.ci.audio.AudioMessage;
import ceri.ci.audio.AudioPlayer;
import ceri.ci.service.CiAlertService;
import ceri.ci.service.CiWebService;
import ceri.ci.web.WebAlerter;
import ceri.ci.web.WebAlerterProperties;
import ceri.ci.x10.X10Alerter;
import ceri.ci.x10.X10AlerterProperties;
import ceri.ci.zwave.ZWaveAlerter;
import ceri.ci.zwave.ZWaveAlerterProperties;
import ceri.ci.zwave.ZWaveController;
import ceri.common.io.IoUtil;
import ceri.common.property.PropertyUtil;
import ceri.common.util.BasicUtil;
import ceri.x10.X10Util;
import ceri.zwave.veralite.VeraLite;

/**
 * Creates everything, 'nuff said.
 */
public class MasterMold implements Closeable {
	private final ZWaveController zwaveController;
	private final ZWaveAlerter zwaveAlerter;
	private final Controller x10Controller;
	private final X10Alerter x10Alerter;
	private final AudioPlayer audioPlayer;
	private final AudioMessage audioMessage;
	private final AudioAlerter audioAlerter;
	private final WebAlerter webAlerter;
	private final Alerters alerters;
	private final AlertService alertService;

	public static void main(String[] args) throws IOException {
		try (MasterMold masterMold = new MasterMold()) {
			AlertService service = masterMold.alertService;
			service.broken("bolt", "smoke", Arrays.asList("cdehaudt"));
			BasicUtil.delay(10000);
			service.fixed("bolt", "smoke", Arrays.asList("cdehaudt"));
			service.broken("bolt", "regression", Arrays.asList("machung"));
			BasicUtil.delay(10000);
			service.broken("bolt", "smoke", Arrays.asList("dxie"));
			BasicUtil.delay(10000);
			service.broken("bolt", "smoke", Arrays.asList("fuzhong", "cjerome"));
			//BasicUtil.delay(10000);
		}
	}

	public MasterMold() throws IOException {
		this(PropertyUtil.load(AlertService.class, "alert.properties"));
	}
	
	public MasterMold(Properties properties) throws IOException {
		ZWaveAlerterProperties zwaveProperties = new ZWaveAlerterProperties(properties, "zwave");
		zwaveController = createZWaveController(zwaveProperties);
		zwaveAlerter = createZWaveAlerter(zwaveController, zwaveProperties);
		X10AlerterProperties x10Properties = new X10AlerterProperties(properties, "x10");
		x10Controller = createX10Controller(x10Properties);
		x10Alerter = createX10Alerter(x10Controller, x10Properties);
		AudioAlerterProperties audioProperties = new AudioAlerterProperties(properties, "audio");
		audioPlayer = createAudioPlayer(audioProperties);
		audioMessage = createAudioMessage(audioPlayer, audioProperties);
		audioAlerter = createAudioAlerter(audioMessage, audioProperties);
		WebAlerterProperties webProperties = new WebAlerterProperties(properties, "web");
		webAlerter = createWebAlerter(webProperties);
		alerters = createAlerters(x10Alerter, zwaveAlerter, audioAlerter, webAlerter);
		AlertServiceProperties alertProperties = new AlertServiceProperties(properties, "alert");
		alertService = createAlertService(alerters, alertProperties);
	}

	public CiAlertService alertService() {
		return alertService;
	}
	
	public CiWebService webService() {
		return webAlerter;
	}
	
	@Override
	public void close() throws IOException {
		if (alertService != null) IoUtil.close(alertService);
		if (x10Controller != null) IoUtil.close(x10Controller);
	}

	private AlertService createAlertService(Alerters alerters, AlertServiceProperties properties) {
		return new AlertService(alerters, properties.reminderMs(), properties.shutdownTimeoutMs());
	}

	private Alerters createAlerters(X10Alerter x10, ZWaveAlerter zwave, AudioAlerter audio,
		WebAlerter web) {
		return new Alerters(x10, zwave, audio, web);
	}

	private WebAlerter createWebAlerter(WebAlerterProperties properties) {
		if (!properties.enabled()) return null;
		File dir = IoUtil.getPackageDir(WebAlerter.class);
		return new WebAlerter(dir);
	}

	private AudioPlayer createAudioPlayer(AudioAlerterProperties properties) {
		if (!properties.enabled()) return null;
		return new AudioPlayer.Default();
	}

	private AudioMessage createAudioMessage(AudioPlayer player, AudioAlerterProperties properties) {
		if (!properties.enabled()) return null;
		File soundDir = IoUtil.getPackageDir(AudioMessage.class);
		return new AudioMessage(player, soundDir, properties.pitch());
	}

	private AudioAlerter
		createAudioAlerter(AudioMessage message, AudioAlerterProperties properties) {
		if (!properties.enabled()) return null;
		return new AudioAlerter(message);
	}

	private X10Alerter createX10Alerter(Controller controller, X10AlerterProperties properties) {
		if (!properties.enabled()) return null;
		X10Alerter.Builder builder = X10Alerter.builder(controller);
		for (String name : properties.names()) {
			String address = properties.address(name);
			builder.address(name, address);
		}
		return builder.build();
	}

	private Controller createX10Controller(X10AlerterProperties properties) throws IOException {
		if (!properties.enabled()) return null;
		return X10Util.createController(properties.commPort(), properties.controllerType());
	}

	private ZWaveAlerter createZWaveAlerter(ZWaveController controller,
		ZWaveAlerterProperties properties) {
		if (!properties.enabled()) return null;
		ZWaveAlerter.Builder builder = ZWaveAlerter.builder(controller);
		for (String name : properties.names()) {
			Integer device = properties.device(name);
			builder.device(name, device);
		}
		return builder.build();
	}

	private ZWaveController createZWaveController(ZWaveAlerterProperties properties) {
		if (!properties.enabled()) return null;
		return new ZWaveController(new VeraLite(properties.host()));
	}

}
