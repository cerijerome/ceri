package ceri.ci.zwave;

import java.io.IOException;

public interface ZWaveController {

	void off(int device) throws IOException;
	void on(int device) throws IOException;

}
