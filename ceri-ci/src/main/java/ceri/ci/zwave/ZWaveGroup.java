package ceri.ci.zwave;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.audio.AudioListener;
import ceri.common.collection.ImmutableUtil;

public class ZWaveGroup {
	private static final Logger logger = LogManager.getLogger();
	private final ZWaveController zwave;
	private final Collection<Integer> devices;
	
	public ZWaveGroup(ZWaveController zwave, Integer...devices) {
		this(zwave, Arrays.asList(devices));
	}
	
	public ZWaveGroup(ZWaveController zwave, Collection<Integer> devices) {
		this.zwave = zwave;
		this.devices = ImmutableUtil.copyAsList(devices);
	}
	
	public AudioListener createAudioListener() {
		return new AudioListener() {
			@Override
			public void audioStart() {
				on();
			}
			
			@Override
			public void audioEnd() {
				off();
			}
		};
	}
	
	public void on() {
		logger.info("Turning devices on: {}", devices);
		for (int device : devices) deviceOn(device);
	}
	
	public void off() {
		logger.info("Turning devices off: {}", devices);
		for (int device : devices) deviceOff(device);
	}
	
	private void deviceOn(int device) {
		try {
			zwave.on(device);
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	private void deviceOff(int device) {
		try {
			zwave.off(device);
		} catch (IOException e) {
			logger.catching(e);
		}
	}

}
