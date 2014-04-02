package ceri.ci;

import java.util.Properties;
import ceri.ci.proxy.MultiProxy;
import ceri.ci.proxy.MultiProxyProperties;

/**
 * Creates audio alerter.
 */
public class Proxy {
	public final MultiProxy multi;

	public Proxy(Properties properties) {
		MultiProxyProperties proxyProperties = new MultiProxyProperties(properties, "proxy");
		if (!proxyProperties.enabled()) {
			multi = null;
		} else {
			multi = new MultiProxy(proxyProperties.threads(), proxyProperties.proxyTargets());
		}
	}

}
