package ceri.ci.build;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ceri.ci.alert.Alerters;
import ceri.ci.service.BuildService;

public class BuildServiceImpl implements BuildService, Closeable {
	private final Builds builds = new Builds();
	private boolean alert = false;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Alerters alerters;
	
	public BuildServiceImpl() {
		alerters = new Alerters();
	}
	
	@Override
	public void close() throws IOException {
		executor.shutdown();
	}
	
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

	private Builds copyBuilds() {
		return new Builds(this.builds);
	}
	
	private void alert() {
		Builds builds = copyBuilds();
		Builds summarizedBuilds = BuildUtil.summarize(builds);
		Collection<String> breakNames = BuildUtil.summarizedBreakNames(summarizedBuilds);
		if (alerters.x10 != null) alerters.x10.alert(breakNames);
		if (alerters.zwave != null) alerters.zwave.alert(breakNames);
		if (alerters.web != null) alerters.web.update(builds);
		if (alerters.audio != null) alerters.audio.alert(summarizedBuilds);
	}

}
