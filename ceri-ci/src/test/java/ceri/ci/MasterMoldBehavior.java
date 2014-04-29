package ceri.ci;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import org.junit.Test;
import ceri.ci.alert.AlertService;
import ceri.ci.alert.AlertContainer;
import ceri.common.util.BasicUtil;

public class MasterMoldBehavior {

	@Test
	public void should() throws IOException {
		Properties properties = new Properties();
		try (AlertContainer mm = new AlertContainer(properties)) {
//			mm.alertService;
//			mm.webService;
		}
	}

}
