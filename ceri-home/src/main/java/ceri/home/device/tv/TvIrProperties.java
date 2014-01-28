package ceri.home.device.tv;

import java.util.Properties;
import ceri.common.property.BaseProperties;
import ceri.home.device.common.IrProperties;


public class TvIrProperties extends BaseProperties {
	private static final String DELAY_MS = "delayMs";
	private static final String CHANNEL = "channel";
	private final IrProperties irProperties;
	
	public TvIrProperties(Properties properties, String prefix) {
		super(properties, prefix);
		irProperties = new IrProperties(properties, prefix);
	}
	
	public IrProperties common() {
    	return irProperties;
    }

	public int getChannelDelayMs() {
		return intValue(0, DELAY_MS, CHANNEL);
	}
	
}
