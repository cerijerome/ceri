package ceri.ci.build;

import java.util.Collection;

public interface BuildEventProcessor {

	void process(BuildEvent... events);

	void process(Collection<BuildEvent> events);

}
