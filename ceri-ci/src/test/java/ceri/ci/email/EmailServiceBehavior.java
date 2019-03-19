package ceri.ci.email;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import javax.mail.Message;
import javax.mail.MessagingException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.BuildEventProcessor;
import ceri.ci.build.Event;
import ceri.ci.build.Event.Type;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.util.BasicUtil;

public class EmailServiceBehavior {
	@Mock private BuildEventProcessor processor;
	@Mock private EmailRetriever mockRetriever;
	@Mock private EmailEventParser parser0;
	@Mock private EmailEventParser parser1;
	@Mock private Message message;
	private EmailRetriever retriever;
	private BooleanCondition sync;
	private TestEmailServer server;
	private EmailService.Builder builder;
	long testStartTime;

	@Before
	public void init() throws MessagingException {
		MockitoAnnotations.initMocks(this);
		when(parser0.matches(any())).thenReturn(false);
		when(parser1.matches(any())).thenReturn(true);
		retriever = testRetriever();
		builder =
			EmailService.builder(retriever, processor).parsers(parser0, parser1).maxLookBackMs(
				30000).pollMs(100000).sentDateBufferMs(5000).shutdownTimeoutMs(5000);
		sync = BooleanCondition.of();
		testStartTime = System.currentTimeMillis();
		server = new TestEmailServer();
	}

	@Test
	public void shouldCallParsersToMatchMessages() throws InterruptedException, MessagingException {
		try (EmailService service = builder.build()) {
			sync.await();
		}
		verify(parser0).matches(message);
		verify(parser1).matches(message);
	}

	@Test
	public void shouldHaveMaximumLookBackDate() throws InterruptedException, IOException {
		builder.maxLookBackMs(10000).sentDateBufferMs(5000);
		try (EmailService service = builder.build()) {
			sync.await();
		}
		ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
		verify(mockRetriever).retrieve(dateCaptor.capture(), any());
		long t = dateCaptor.getValue().getTime();
		assertTrue(t >= testStartTime - 10000 - 5000);
		assertTrue(t <= System.currentTimeMillis() - 10000 - 5000);
	}

	@Test
	public void shouldUseLastEmailSentDateForNextFetch() throws InterruptedException, IOException {
		server.addResponse(email(0));
		builder.sentDateBufferMs(5000).pollMs(1);
		try (EmailService service = builder.build()) {
			sync.await();
			sync.await();
		}
		ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
		verify(mockRetriever, atLeast(2)).retrieve(dateCaptor.capture(), any());
		long t = dateCaptor.getValue().getTime();
		assertThat(t, is(testStartTime - 5000));
	}

	@Test
	public void shouldNotProcessEmailsAlreadyRetrieved() throws InterruptedException {
		server.addResponse(email(0), email(1), email(2));
		server.addResponse(email(1), email(2), email(3));
		server.addResponse(email(3), email(1));
		builder.pollMs(1);
		try (EmailService service = builder.build()) {
			sync.await();
			sync.await();
			sync.await();
		}
		verify(parser0).parse(email(0));
		verify(parser0).parse(email(1));
		verify(parser0).parse(email(2));
		verify(parser0).parse(email(3));
	}

	@Test
	public void shouldProcessParsedEmails() throws InterruptedException, IOException {
		server.addResponse(email(0), email(1));
		builder.parsers(testParser());
		try (EmailService service = builder.build()) {
			sync.await();
		}
		ArgumentCaptor<Collection<BuildEvent>> eventCaptor =
			BasicUtil.uncheckedCast(ArgumentCaptor.forClass(Collection.class));
		verify(processor).process(eventCaptor.capture());
		assertCollection(eventCaptor.getValue(), buildEvent(0), buildEvent(1));
	}

	private EmailEventParser testParser() {
		return new EmailEventParser() {
			@Override
			public boolean matches(Message message) {
				return true;
			}

			@Override
			public BuildEvent parse(Email email) {
				return buildEvent(email.sentDateMs - testStartTime);
			}
		};
	}

	private EmailRetriever testRetriever() {
		return (minDate, matcher) -> {
			mockRetriever.retrieve(minDate, matcher);
			try {
				matcher.matches(message);
			} catch (MessagingException e) {
				throw new AssertionError(e);
			}
			sync.signal();
			return server.nextResponse();
		};
	}

	private Email email(long sentOffsetMs) {
		return EmailTestUtil.presetEmail().sentDateMs(testStartTime + sentOffsetMs).build();
	}

	BuildEvent buildEvent(long timeOffsetMs) {
		return new BuildEvent("build", "job", new Event(Type.success, testStartTime + timeOffsetMs,
			"name"));
	}

}
