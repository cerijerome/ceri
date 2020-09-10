package ceri.ci.email;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import org.junit.Test;
import ceri.common.collection.Iterators;

public class EmailUtilTest {

	@Test
	public void testCreateSessionUsesSecureAndNonSecurePropertySettings() {
		Session session = EmailUtil.createSession("imap", 1000);
		assertNotNull(session);
		Properties props = session.getProperties();
		boolean imap = false;
		boolean imaps = false;
		for (Object name : Iterators.forEach(props.propertyNames())) {
			if (name.toString().contains(".imap.")) imap = true;
			if (name.toString().contains(".imaps.")) imaps = true;
		}
		assertTrue("Properties do not contain imap settings", imap);
		assertTrue("Properties do not contain imaps settings", imaps);
	}

	@Test
	public void testContentForMessageWithInputStream() throws IOException, MessagingException {
		byte[] bs = { 'A', 'B', 'C' };
		ByteArrayInputStream in = new ByteArrayInputStream(bs);
		Message message = mock(Message.class);
		when(message.getContent()).thenReturn(in);
		String content = EmailUtil.content(message);
		assertThat(content, is("ABC"));
	}

	@Test
	public void testContentForMultipartMessage() throws IOException, MessagingException {
		Message message = mock(Message.class);
		Multipart multipart = mock(Multipart.class);
		BodyPart bodyPart = mock(BodyPart.class);
		when(message.getContent()).thenReturn(multipart);
		when(multipart.getBodyPart(anyInt())).thenReturn(bodyPart);
		when(bodyPart.getContent()).thenReturn("content");
		String content = EmailUtil.content(message);
		assertThat(content, is("content"));
	}

	@Test
	public void testContentForMessageWithUnknownObject() throws IOException, MessagingException {
		Object obj = new Object() {
			@Override
			public String toString() {
				return "content";
			}
		};
		Message message = mock(Message.class);
		when(message.getContent()).thenReturn(obj);
		String content = EmailUtil.content(message);
		assertThat(content, is("content"));
	}

}
