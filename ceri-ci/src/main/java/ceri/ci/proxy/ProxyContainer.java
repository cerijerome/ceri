package ceri.ci.proxy;

import java.util.Properties;

/**
 * Creates audio alerter.
 */
public class ProxyContainer {
	public final MultiProxy multi;

	public ProxyContainer(Properties properties, String prefix) {
		MultiProxyProperties proxyProperties =
			new MultiProxyProperties(properties, prefix, "proxy");
		if (!proxyProperties.enabled()) {
			multi = null;
		} else {
			multi = new MultiProxy(proxyProperties.threads(), proxyProperties.proxyTargets());
		}
	}

}
