package ceri.ci.admin;

import java.util.Collection;
import ceri.ci.admin.Params.BuildJob;
import ceri.ci.build.BuildEvent;

public class CommandFactory {
	public static final String SUCCESS = "success";
	
	public static Command create(Params params) {
		Action action = params.action();
		BuildJob buildJob = params.buildJob();
		switch (action) {
		case view:
			return view(buildJob.build, buildJob.job);
		case clear:
			return clear(buildJob.build, buildJob.job);
		case delete:
			return delete(buildJob.build, buildJob.job);
		case process:
			Collection<BuildEvent> events = params.buildEvents();
			return process(events);
		case purge:
			return purge();
		}
		throw new IllegalStateException("Should not happen");
	}
	
	public static Command view(String build, String job) {
		return (service) -> {
			if (build == null) return service.builds().toString();
			if (job == null) return service.build(build).toString();
			return service.job(build, job).toString(); 
		};
	}

	public static Command clear(String build, String job) {
		return (service) -> {
			service.clear(build, job);
			return SUCCESS;
		};
	}
	
	public static Command delete(String build, String job) {
		return (service) -> {
			service.delete(build, job);
			return SUCCESS;
		};
	}
	
	public static Command process(Collection<BuildEvent> events) {
		return (service) -> {
			service.process(events);
			return SUCCESS;
		};
	}
	
	public static Command purge() {
		return (service) -> {
			service.purge();
			return SUCCESS;
		};
	}
	
}
