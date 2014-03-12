package ceri.ci;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import org.junit.Test;
import ceri.ci.alert.AlertService;
import ceri.common.util.BasicUtil;

public class MasterMoldBehavior {

	@Test
	public void should() throws IOException {
		Properties properties = new Properties();
		try (MasterMold mm = new MasterMold(properties)) {
//			mm.alertService;
//			mm.webService;
		}
	}

}
