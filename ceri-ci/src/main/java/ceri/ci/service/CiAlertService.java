package ceri.ci.service;

import java.util.Collection;

public interface CiAlertService {
	void clear();
	void clear(String build);
	void clear(String build, String job);
	void fixed(String build, String job, Collection<String> names);
	void broken(String build, String job, Collection<String> names);
}
