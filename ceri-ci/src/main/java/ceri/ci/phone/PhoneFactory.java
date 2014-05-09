package ceri.ci.phone;

public interface PhoneFactory {

	PhoneClient createClient(String accountSid, String authToken, String fromNumber);
	PhoneAlerter.Builder builder(PhoneClient client);
	
}
