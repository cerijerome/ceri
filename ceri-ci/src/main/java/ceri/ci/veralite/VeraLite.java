package ceri.ci.veralite;

import java.io.IOException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import ceri.common.util.BasicUtil;

public class VeraLite {
	private final String host;
	
	public VeraLite(String host) {
		this.host = host;
	}
	
	private void call() throws IOException {
		String url =
			"http://192.168.0.109:3480/data_request?id=action&DeviceNum=5&"
				+ "serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=";
		//http://192.168.0.109:3480/data_request?id=user_data
		Content content = Request.Get(url + "100").execute().returnContent();
		System.out.println(content.asString());
		BasicUtil.delay(3000);
		content = Request.Get(url + "0").execute().returnContent();
		System.out.println(content.asString());
		
		String command = "data_request";
		url = "http://" + host + "/" + command + "?";
	}
	
}
