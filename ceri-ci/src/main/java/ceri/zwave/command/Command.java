package ceri.zwave.command;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;

public class Command {
	private static final String HTTP_PREFIX = "http://";
	private static final String DATA_REQUEST = "data_request";
	private static final String OUTPUT_FORMAT = "output_format";
	private static final String DEVICE_NUM = "DeviceNum";
	private static final String NEW_TARGET_VALUE = "newTargetValue";
	private final String host;
	private final Map<String, Object> params = new LinkedHashMap<>();
	
	public Command(String host) {
		this.host = host;
		outputFormat(OutputFormat.xml);
	}
	
	public Command outputFormat(OutputFormat format) {
		param(OUTPUT_FORMAT, format.name());
		return this;
	}
	
	public Command param(String key, Object value) {
		params.put(key, value);
		return this;
	}
	
	public Command targetValue(Object obj) {
		param(NEW_TARGET_VALUE, obj.toString());
		return this;
	}
	
	public Command device(int device) {
		param(DEVICE_NUM, device);
		return this;
	}
	
	public void execute() throws IOException {
		String url = createUrl(host, params);
		System.out.println(url);
		Content response = Request.Get(url).execute().returnContent();
		String content = response.asString();
		System.out.println(content);
	}
	
	private String createUrl(String host, Map<String, Object> params) {
		StringBuilder b = new StringBuilder(HTTP_PREFIX);
		b.append(host).append('/').append(DATA_REQUEST);
		if (!params.isEmpty()) {
			b.append('?');
			boolean first = true;
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				if (!first) b.append('&');
				b.append(entry.getKey()).append('=').append(entry.getValue());
				first = false;
			}
		}
		return b.toString();
	}
	
}
