package ceri.ci.proxy;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import ceri.common.property.BaseProperties;

public class MultiProxyProperties extends BaseProperties {
	private static final String ENABLED_KEY = "enabled";
	private static final String THREADS_KEY = "threads";
	private static final String TARGETS_KEY = "targets";
	private static final int THREADS_DEF = 10;
	private static final Collection<String> TARGETS_DEF = Collections.singleton("localhost");

	public MultiProxyProperties(Properties properties, String...prefix) {
		super(properties, prefix);
	}

	public boolean enabled() {
		return booleanValue(false, ENABLED_KEY);
	}

	public int threads() {
		return intValue(THREADS_DEF, THREADS_KEY);
	}
	
	public Collection<String> proxyTargets() {
		return stringValues(TARGETS_DEF, TARGETS_KEY);
	}

}
