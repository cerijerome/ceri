package ceri.ci.zwave;

public interface ZWaveFactory {

	ZWaveController createController(String host);
	ZWaveAlerter.Builder builder(ZWaveController controller);

}
