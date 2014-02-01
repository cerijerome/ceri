package ceri.ci.alert;

import static ceri.common.test.TestUtil.assertCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import ceri.ci.audio.AudioAlerter;
import ceri.ci.build.BuildTestUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.web.WebAlerter;
import ceri.ci.x10.X10Alerter;
import ceri.ci.zwave.ZWaveAlerter;
import ceri.common.util.BasicUtil;

public class AlertersBehavior {
	private X10Alerter x10;
	private ZWaveAlerter zwave;
	private AudioAlerter audio;
	private WebAlerter web;
	private Alerters alerters;

	@Before
	public void init() {
		x10 = mock(X10Alerter.class);
		zwave = mock(ZWaveAlerter.class);
		audio = mock(AudioAlerter.class);
		web = mock(WebAlerter.class);
		alerters = new Alerters(x10, zwave, audio, web);
	}

	@Test
	public void shouldNotCallDisabledAlerters() {
		Alerters alerters = new Alerters(null, null, null, null);
		alerters.alert(new Builds());
		alerters.clear();
		alerters.remind();
		verifyNoMoreInteractions(x10, zwave, web, audio);
	}

	@Test
	public void shouldAlertAlertersWithSummarizedBuilds() {
		Builds builds = new Builds();
		Event b0 = BuildTestUtil.event(Event.Type.broken, 0, "b00", "b01");
		Event f1 = BuildTestUtil.event(Event.Type.fixed, 1, "f1");
		Event b2 = BuildTestUtil.event(Event.Type.broken, 2, "b20");
		Event b4 = BuildTestUtil.event(Event.Type.broken, 4, "b40", "b41");
		builds.build("b0").job("j0").event(b0, f1, b2, b4);
		alerters.alert(builds);

		ArgumentCaptor<Collection<String>> namesCaptor = createStringsCaptor();
		verify(x10).alert(namesCaptor.capture());
		assertCollection(namesCaptor.getValue(), "b20", "b40", "b41");
		verify(zwave).alert(namesCaptor.capture());
		assertCollection(namesCaptor.getValue(), "b20", "b40", "b41");
		
		Builds summarizedBuilds = new Builds();
		Event sf = BuildTestUtil.event(Event.Type.fixed, 1, "f1");
		Event sb = BuildTestUtil.event(Event.Type.broken, 2, "b20", "b40", "b41");
		summarizedBuilds.build("b0").job("j0").event(sf, sb);
		
		verify(web).update(summarizedBuilds);
		verify(audio).alert(summarizedBuilds);
	}

	@Test
	public void shouldClearAlerters() {
		alerters.clear();
		verify(x10).clear();
		verify(zwave).clear();
		verify(web).clear();
		verify(audio).clear();
	}

	@Test
	public void shouldRemindAlerters() {
		alerters.remind();
		verify(audio).remind();
	}

	private ArgumentCaptor<Collection<String>> createStringsCaptor() {
		Class<Collection<String>> cls = BasicUtil.uncheckedCast(Collection.class);
		return ArgumentCaptor.forClass(cls);
	}

}
