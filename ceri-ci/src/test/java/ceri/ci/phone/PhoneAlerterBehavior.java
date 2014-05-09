package ceri.ci.phone;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.build.Event.Type;

public class PhoneAlerterBehavior {
	private static final Event e0 = new Event(Type.failure, 1L, "A");
	private static final Event e1 = new Event(Type.failure, 2L, "CCC");
	@Mock private PhoneClient client;
	PhoneAlerter alerter;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		PhoneAlerter.Builder builder = PhoneAlerter.builder(client);
		builder.number("A", "aaa");
		builder.number("BB", "bb");
		builder.number("CCC", "c");
		alerter = builder.build();
	}

	@Test
	public void shouldSendMessageForJustBrokenBuild() {
		Builds builds = new Builds();
		alerter.update(builds);
		builds.build("b0").job("j0").events(e0, e1);
		alerter.update(builds);
		verify(client).sendSms(eq("aaa"), any());
		verify(client).sendSms(eq("c"), any());
	}

	@Test
	public void shouldAllowClearAndRemindMethods() {
		alerter.clear();
		alerter.remind();
	}

	@Test
	public void shouldSendMessage() {
		alerter.alert("A", "Atest");
		verify(client).sendSms("aaa", "Atest");
	}

}
