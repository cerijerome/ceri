package ceri.ci.phone;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Message;
import ceri.common.test.TestUtil;

public class TwilioClientBehavior {
	@Mock private TwilioFactory factory;
	@Mock private TwilioRestClient client;
	@Mock private Account account;
	@Mock private MessageFactory messageFactory;
	@Mock private Message message;

	@Before
	public void init() throws TwilioRestException {
		MockitoAnnotations.initMocks(this);
		when(factory.createRestClient(any(), any())).thenReturn(client);
		when(client.getAccount()).thenReturn(account);
		when(account.getMessageFactory()).thenReturn(messageFactory);
		when(messageFactory.create(any())).thenReturn(message);
	}

	@Test
	public void shouldSendAMessage() throws TwilioRestException {
		TwilioClient client =
			new TwilioClient("accountSid", "authToken", "fromPhoneNumber", factory);
		client.sendSms("toPhoneNumber", "messageContent");
		verify(messageFactory).create(any());
	}

	@Test
	public void shouldNotAllowNullConstructorArguments() {
		TestUtil.assertThrown(() -> new TwilioClient(null, "authToken", "fromPhoneNumber", factory));
		TestUtil.assertThrown(() -> new TwilioClient("accountSid", null, "fromPhoneNumber", factory));
		TestUtil.assertThrown(() -> new TwilioClient("accountSid", "authToken", null, factory));
	}

}
