package ceri.ci.email;

import java.io.IOException;
import ceri.ci.build.BuildEvent;

public interface BuildEventProcessor {

	void process(BuildEvent event) throws IOException;

}
