package ceri.ci.email;

import static ceri.ci.email.EmailTestUtil.messageBuilder;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import java.io.IOException;
import java.util.Date;
import javax.mail.Message;
import javax.mail.MessagingException;
import org.junit.Test;

public class EmailBehavior {
	private static final long time = 1390000000000L;

	@Test
	public void shouldObeyEqualsContract() {
		Email email = presetBuilder().build();
		Email email2 = presetBuilder().build();
		Email email3 = presetBuilder().content("xxx").build();
		Email email4 = presetBuilder().from("").build();
		Email email5 = presetBuilder().recipients("").build();
		Email email6 = presetBuilder().subject("").build();
		assertEquals(email, email);
		assertEquals(email, email2);
		assertEquals(email.hashCode(), email2.hashCode());
		assertEquals(email.toString(), email2.toString());
		assertNotEquals(email, new Object());
		assertNotEquals(email, email3);
		assertNotEquals(email, email4);
		assertNotEquals(email, email5);
		assertNotEquals(email, email6);
	}

	@Test
	public void shouldCreateFromMessage() throws IOException, MessagingException {
		Message message =
			messageBuilder().recipients("to0@test.com", "to1@test.com").from("from@test.com")
				.sentDate(new Date(time)).subject("subject").content("content").build();
		Email email = Email.createFrom(message);
		assertCollection(email.recipients, "to0@test.com", "to1@test.com");
		assertEquals(email.from, "from@test.com");
		assertEquals(email.sentDateMs, time);
		assertEquals(email.subject, "subject");
		assertEquals(email.content, "content");
	}

	private Email.Builder presetBuilder() {
		return Email.builder().content("content").from("from@test.com")
			.recipients("to0@test.com", "to1@test.com").sentDateMs(time).subject("subject");
	}

}
