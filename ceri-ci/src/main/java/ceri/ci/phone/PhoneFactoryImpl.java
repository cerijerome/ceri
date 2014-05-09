package ceri.ci.phone;

import com.twilio.sdk.TwilioRestClient;
import ceri.ci.phone.PhoneAlerter.Builder;

public class PhoneFactoryImpl implements PhoneFactory, TwilioFactory {

	@Override
	public TwilioRestClient createRestClient(String accountSid, String authToken) {
		return new TwilioRestClient(accountSid, authToken);
	}
	
	@Override
	public PhoneClient createClient(String accountSid, String authToken, String fromNumber) {
		return new TwilioClient(accountSid, authToken, fromNumber, this);
	}
	
	@Override
	public Builder builder(PhoneClient client) {
		return PhoneAlerter.builder(client);
	}
	
}
