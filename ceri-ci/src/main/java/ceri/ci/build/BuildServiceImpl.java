package ceri.ci.build;

import java.util.Collection;
import ceri.ci.service.BuildService;

public class BuildServiceImpl implements BuildService {
	private final Builds builds = new Builds();

	@Override
	public void purge() {
		// No need for notification.
		builds.purge();
	}

	@Override
	public void clear(String build) {
		builds.build(build).clear();
		alert();
	}

	@Override
	public void clear(String build, String job) {
		builds.build(build).job(job).clear();
		alert();
	}

	@Override
	public void fixed(String build, String job, Collection<String> names) {
		builds.build(build).job(job).event(Event.fixed(names));
		alert();
	}

	@Override
	public void broken(String build, String job, Collection<String> names) {
		builds.build(build).job(job).event(Event.broken(names));
		alert();
	}

	private void alert() {
		Builds builds = new Builds(this.builds);
		Builds summarizedBuilds = BuildUtil.summarize(builds);
		Collection<String> breakNames = BuildUtil.breakNames(summarizedBuilds);
		//@TODO notify alerters
	}

}
