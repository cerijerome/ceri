package ceri.ci.service;

import ceri.ci.build.Build;
import ceri.ci.build.Builds;
import ceri.ci.build.Job;

public interface CiWebService {
	Builds builds();
	Build build(String build);
	Job job(String build, String job);
}
