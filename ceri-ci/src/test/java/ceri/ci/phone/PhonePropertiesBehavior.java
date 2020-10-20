package ceri.ci.phone;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
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
		assertTrue(phone.enabled());
		assertEquals(phone.accountSid(), "accountSid");
		assertEquals(phone.authToken(), "authToken");
		assertEquals(phone.fromNumber(), "fromNumber");
		assertCollection(phone.names(), "A", "BB", "CCC");
		assertEquals(phone.number("A"), "aaa");
		assertEquals(phone.number("BB"), "bb");
		assertEquals(phone.number("CCC"), "c");
	}

}
