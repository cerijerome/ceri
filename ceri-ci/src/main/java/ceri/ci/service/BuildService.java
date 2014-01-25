package ceri.ci.service;

import java.util.Collection;

public interface BuildService {
	void purge();
	void clear(String build);
	void clear(String build, String job);
	void fixed(String build, String job, Collection<String> names);
	void broken(String build, String job, Collection<String> names);
}
