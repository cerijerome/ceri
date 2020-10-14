package ceri.ci.phone;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.property.BaseProperties;

public class PhonePropertiesBehavior {
	private static Properties props = new Properties();
	private static BaseProperties baseProps = new BaseProperties(props) {};

	@BeforeClass
	public static void init() {
		props.put("x.enabled", "true");
		props.put("x.account.sid", "accountSid");
		props.put("x.auth.token", "authToken");
		props.put("x.from.number", "fromNumber");
		props.put("x.number.A", "aaa");
		props.put("x.number.BB", "bb");
		props.put("x.number.CCC", "c");
	}

	@Test
	public void shouldReadValuesWithPrefix() {
		PhoneProperties phone = new PhoneProperties(baseProps, "x");
		assertThat(phone.enabled(), is(true));
		assertThat(phone.accountSid(), is("accountSid"));
		assertThat(phone.authToken(), is("authToken"));
		assertThat(phone.fromNumber(), is("fromNumber"));
		assertCollection(phone.names(), "A", "BB", "CCC");
		assertThat(phone.number("A"), is("aaa"));
		assertThat(phone.number("BB"), is("bb"));
		assertThat(phone.number("CCC"), is("c"));
	}

}
