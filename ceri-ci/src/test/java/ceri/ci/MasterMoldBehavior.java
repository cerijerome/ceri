package ceri.ci;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import org.junit.Test;
import ceri.ci.service.CiAlertService;
import ceri.common.util.BasicUtil;

public class MasterMoldBehavior {

	public static void main(String[] args) throws IOException {
		try (MasterMold masterMold = new MasterMold()) {
			CiAlertService service = masterMold.alertService();
			service.broken("bolt", "smoke", Arrays.asList("cdehaudt"));
			BasicUtil.delay(10000);
			service.fixed("bolt", "smoke", Arrays.asList("cdehaudt"));
			service.broken("bolt", "regression", Arrays.asList("machung"));
			BasicUtil.delay(10000);
			service.broken("bolt", "smoke", Arrays.asList("dxie"));
			BasicUtil.delay(10000);
			service.broken("bolt", "smoke", Arrays.asList("fuzhong", "cjerome"));
			//BasicUtil.delay(10000);
		}
	}

	@Test
	public void should() throws IOException {
		Properties properties = new Properties();
		try (MasterMold mm = new MasterMold(properties)) {
			mm.alertService();
			mm.webService();
		}
	}

}
