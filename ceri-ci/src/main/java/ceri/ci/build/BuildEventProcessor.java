package ceri.ci.build;

import java.io.IOException;
import java.util.Collection;

public interface BuildEventProcessor {

	void process(BuildEvent...events) throws IOException;
	void process(Collection<BuildEvent> events) throws IOException;

}
