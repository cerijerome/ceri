package ceri.ci.job;

import java.util.Collection;

public interface JobService {
	void broken(String jobName, Collection<String> responsible);
	void fixed(String jobName, Collection<String> responsible);
	void clear();
}
