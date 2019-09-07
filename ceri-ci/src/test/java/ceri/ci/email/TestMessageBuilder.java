package ceri.ci.email;

import static ceri.ci.email.EmailTestUtil.mockAddresses;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

public class TestMessageBuilder {
	private final Collection<String> recipients = new LinkedHashSet<>();
	private String from;
	private Date sentDate;
	private String subject;
	private String content;
	
	public TestMessageBuilder recipients(String...recipients) {
		return recipients(Arrays.asList(recipients));
	}
	
	public TestMessageBuilder recipients(Collection<String> recipients) {
		this.recipients.addAll(recipients);
		return this;
	}
	
	public TestMessageBuilder from(String from) {
		this.from = from;
		return this;
	}
	
	public TestMessageBuilder sentDate(Date sentDate) {
		this.sentDate = sentDate;
		return this;
	}
	
	public TestMessageBuilder subject(String subject) {
		this.subject = subject;
		return this;
	}
	
	public TestMessageBuilder content(String content) {
		this.content = content;
		return this;
	}
	
	public Message build() {
		try {
			Message message = mock(Message.class);
			Address[] recipients = mockAddresses(this.recipients);
			Address[] from = mockAddresses(this.from);
			when(message.getAllRecipients()).thenReturn(recipients);
			when(message.getFrom()).thenReturn(from);
			when(message.getSentDate()).thenReturn(sentDate);
			when(message.getSubject()).thenReturn(subject);
			when(message.getContent()).thenReturn(content);
			return message;
		} catch (MessagingException | IOException e) {
			throw new AssertionError("Should not happen", e);
		}
	}
	
}