package ceri.ci.phone;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

public class TwilioClient implements PhoneClient {
	private static final Logger logger = LogManager.getLogger();
	private static final String TO = "To";
	private static final String FROM = "From";
	private static final String BODY = "Body";
	private final TwilioRestClient twilio;
	private final String fromNumber;

	public TwilioClient(String accountSid, String authToken, String fromNumber,
		TwilioFactory factory) {
		if (accountSid == null) throw new NullPointerException("Account SID cannot be null");
		if (authToken == null) throw new NullPointerException("Auth token cannot be null");
		if (fromNumber == null) throw new NullPointerException("From phone number cannot be null");
		twilio = factory.createRestClient(accountSid, authToken);
		this.fromNumber = fromNumber;
	}

	@Override
	public void sendSms(String phoneNumber, String content) {
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(TO, phoneNumber));
		params.add(new BasicNameValuePair(FROM, fromNumber));
		params.add(new BasicNameValuePair(BODY, content));
		MessageFactory messageFactory = twilio.getAccount().getMessageFactory();
		try {
			logger.info("Sending SMS to {}: {}", phoneNumber, content);
			Message message = messageFactory.create(params);
			logger.debug("Returned message SID {}", message.getSid());
		} catch (TwilioRestException e) {
			logger.catching(e);
		}
	}

}
