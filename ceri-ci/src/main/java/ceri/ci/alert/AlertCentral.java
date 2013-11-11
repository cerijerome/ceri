package ceri.ci.alert;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import ceri.ci.audio.AudioAlerter;
import ceri.ci.common.Alerter;
import ceri.ci.web.WebAlerter;
import ceri.ci.x10.X10Alerter;
import ceri.common.collection.ImmutableUtil;

public class AlertCentral implements Alerter {
	private static final String PEOPLE = "people";
	private static final String HTML = "html";
	private static final String CLIPS = "clips";
	private final Collection<Alerter> alerters;

	public static void main(String[] args) throws Exception {
		try (AlertCentral alerter = new AlertCentral("./data")) {
			alerter.alert("shuochen", "moromero");
		}
	}

	public AlertCentral(String root) {
		File rootDir = new File(root);
		Collection<Alerter> alerters = new HashSet<>();
		createX10(alerters, rootDir);
		createAudio(alerters, rootDir);
		createWeb(alerters, rootDir);
		this.alerters = ImmutableUtil.copyAsSet(alerters);
	}

	@Override
	public void alert(String... keys) {
		for (Alerter alerter : alerters)
			alerter.alert(keys);
	}

	@Override
	public void clear(String... keys) {
		for (Alerter alerter : alerters)
			alerter.clear(keys);
	}

	@Override
	public void close() throws IOException {
		for (Alerter alerter : alerters)
			alerter.close();
	}

	private void createX10(Collection<Alerter> alerters, File rootDir) {
		try {
			alerters.add(X10Alerter.create(new File(rootDir, PEOPLE), null));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createAudio(Collection<Alerter> alerters, File rootDir) {
		alerters.add(new AudioAlerter(new File(rootDir, PEOPLE), new File(rootDir, CLIPS)));
	}

	private void createWeb(Collection<Alerter> alerters, File rootDir) {
		alerters.add(new WebAlerter("../" + PEOPLE, new File(rootDir, HTML)));
	}

}
