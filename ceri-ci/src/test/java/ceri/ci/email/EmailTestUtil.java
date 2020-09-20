package ceri.ci.email;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collection;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class EmailTestUtil {

	private EmailTestUtil() {}

	public static TestMessageBuilder messageBuilder() {
		return new TestMessageBuilder();
	}

	public static Address[] mockAddresses(String... emails) {
		return mockAddresses(Arrays.asList(emails));
	}

	public static Address[] mockAddresses(Collection<String> emails) {
		Address[] addresses = new Address[emails.size()];
		int i = 0;
		for (String email : emails)
			addresses[i++] = mockAddress(email);
		return addresses;
	}

	public static Address mockAddress(String address) {
		InternetAddress mock = mock(InternetAddress.class);
		when(mock.getAddress()).thenReturn(address);
		return mock;
	}

	public static Email.Builder presetEmail() {
		Email.Builder builder = Email.builder();
		builder.content("content");
		builder.from("from@test.com");
		builder.recipients("to@test.com");
		builder.sentDateMs(0);
		builder.subject("subject");
		return builder;
	}

}
