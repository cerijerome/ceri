package ceri.ci.build;

public class BuildEvent {
	public final String build;
	public final String job;
	public final Event event;
	
	public BuildEvent(String build, String job, Event event) {
		this.build = build;
		this.job = job;
		this.event = event;
	}
	
	public void applyTo(Builds builds) {
		builds.build(build).job(job).event(event);
	}
	
}
