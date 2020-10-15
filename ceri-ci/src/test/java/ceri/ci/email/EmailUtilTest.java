package ceri.ci.email;

import static ceri.common.test.TestUtil.assertEquals;
import static ceri.common.test.TestUtil.assertNotNull;
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
		assertEquals(imap, true, "Properties do not contain imap settings");
		assertEquals(imaps, true, "Properties do not contain imaps settings");
	}

	@Test
	public void testContentForMessageWithInputStream() throws IOException, MessagingException {
		byte[] bs = { 'A', 'B', 'C' };
		ByteArrayInputStream in = new ByteArrayInputStream(bs);
		Message message = mock(Message.class);
		when(message.getContent()).thenReturn(in);
		String content = EmailUtil.content(message);
		assertEquals(content, "ABC");
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
		assertEquals(content, "content");
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
		assertEquals(content, "content");
	}

}
