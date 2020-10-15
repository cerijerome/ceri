package ceri.ci.email;

import static ceri.ci.email.EmailTestUtil.messageBuilder;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.common.test.TestUtil;

public class EmailRetrieverImplBehavior {
	@Mock
	Store store;
	@Mock
	Folder defaultFolder;
	@Mock
	Folder folder;

	@Before
	public void init() throws MessagingException {
		MockitoAnnotations.initMocks(this);
		when(store.getDefaultFolder()).thenReturn(defaultFolder);
		when(store.getFolder(anyString())).thenReturn(folder);
	}

	@Test
	public void shouldHaveStringRepresentation() {
		String s = create(presetBuilder()).toString();
		assertTrue(s.contains("host"));
	}

	@Test
	public void shouldFailIfNoMinimumDateForFetchingEmails() {
		EmailRetriever retriever = create(presetBuilder());
		TestUtil.assertThrown(() -> retriever.retrieve(null, null));
	}

	@Test
	public void shouldFetchEmailsBasedOnGivenMatcher() throws IOException, MessagingException {
		Message msg0 = mockMessage(1);
		Message msg1 = mockMessage(5);
		Message msg2 = mockMessage(11);
		when(folder.search(any())).thenReturn(new Message[] { msg0, msg1, msg2 });
		EmailRetriever retriever = create(presetBuilder());
		List<Email> emails =
			retriever.retrieve(new Date(2), message -> message.getSentDate().getTime() < 10);
		assertThat(emails.size(), is(1));
		assertThat(emails.get(0).sentDateMs, is(5L));
	}

	@Test
	public void shouldFetchEmailsOlderThanGivenDate() throws IOException, MessagingException {
		Message msg0 = mockMessage(0);
		Message msg1 = mockMessage(1);
		Message msg2 = mockMessage(2);
		when(folder.search(any())).thenReturn(new Message[] { msg0, msg1, msg2 });
		EmailRetriever retriever = create(presetBuilder());
		List<Email> emails = retriever.retrieve(new Date(1), null);
		assertThat(emails.size(), is(2));
		assertThat(emails.get(0).sentDateMs, is(1L));
		assertThat(emails.get(1).sentDateMs, is(2L));
	}

	private EmailRetrieverImpl.Builder presetBuilder() {
		return EmailRetrieverImpl.builder("host", "name", "password").folder("folder").port(1)
			.protocol("protocol").timeoutMs(10000L);
	}

	private EmailRetriever create(EmailRetrieverImpl.Builder builder) {
		return new EmailRetrieverImpl(builder) {
			@Override
			protected Store getStore() {
				return store;
			}
		};
	}

	private Message mockMessage(long time) {
		return messageBuilder().from("from@test.com").recipients("to@test.com").content("content")
			.subject("subject").sentDate(new Date(time)).build();
	}

}
