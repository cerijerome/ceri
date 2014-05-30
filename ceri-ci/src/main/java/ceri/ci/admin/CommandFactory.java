package ceri.ci.admin;

import java.util.Collection;
import ceri.ci.admin.Params.BuildJob;
import ceri.ci.alert.AlertService;
import ceri.ci.build.BuildEvent;

public class CommandFactory {
	public static final String SUCCESS = "success";
	private final Serializer serializer;

	public CommandFactory(Serializer serializer) {
		this.serializer = serializer;
	}

	public Command create(Params params) {
		Action action = params.action();
		switch (action) {
		case view:
			return view(params.buildJob());
		case clear:
			return clear(params.buildJob());
		case delete:
			return delete(params.buildJob());
		case process:
			Collection<BuildEvent> events = params.buildEvents();
			return process(events);
		case purge:
			return purge();
		}
		throw new IllegalStateException("Should not happen");
	}

	private Command view(BuildJob buildJob) {
		return (service) -> {
			String json = serialize(buildJob, service);
			return Response.success(json);
		};
	}

	private String serialize(BuildJob buildJob, AlertService service) {
		if (buildJob.build == null) return serializer.fromBuilds(service.builds());
		if (buildJob.job == null) return serializer.fromBuild(service.build(buildJob.build));
		return serializer.fromJob(service.job(buildJob.build, buildJob.job));
	}

	private Command clear(BuildJob buildJob) {
		return (service) -> {
			service.clear(buildJob.build, buildJob.job);
			return Response.success();
		};
	}

	private Command delete(BuildJob buildJob) {
		return (service) -> {
			service.delete(buildJob.build, buildJob.job);
			return Response.success();
		};
	}

	private Command process(Collection<BuildEvent> events) {
		return (service) -> {
			service.process(events);
			return Response.success();
		};
	}

	private Command purge() {
		return (service) -> {
			service.purge();
			return Response.success();
		};
	}

}
