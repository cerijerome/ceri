package ceri.ci.phone;

import com.twilio.sdk.TwilioRestClient;

public interface TwilioFactory {

	TwilioRestClient createRestClient(String accountSid, String authToken);

}
