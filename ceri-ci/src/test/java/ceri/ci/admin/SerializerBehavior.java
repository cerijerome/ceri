package ceri.ci.admin;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import org.junit.Test;
import ceri.ci.build.Build;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.build.Job;

public class SerializerBehavior {

	@Test
	public void shouldDeSerializeBuildEvents() {
		Serializer serializer = new Serializer();
		assertTrue(serializer.toBuildEvents("").isEmpty());
		String s = "[{\"build\":\"b0\",\"job\":\"j0\"," +
			"\"event\":{\"timeStamp\":0,\"type\":\"success\",\"names\":[\"n0\"]}}," +
			"{\"build\":\"b1\",\"job\":\"j1\"," +
			"\"event\":{\"timeStamp\":1,\"type\":\"failure\",\"names\":[]}}]";
		Collection<BuildEvent> buildEvents = serializer.toBuildEvents(s);
		BuildEvent ev0 = new BuildEvent("b0", "j0", new Event(Event.Type.success, 0L, "n0"));
		BuildEvent ev1 = new BuildEvent("b1", "j1", new Event(Event.Type.failure, 1L));
		assertCollection(buildEvents, ev0, ev1);
	}

	@Test
	public void shouldSerializeBuildEvents() {
		Serializer serializer = new Serializer();
		BuildEvent ev0 = new BuildEvent("b0", "j0", new Event(Event.Type.success, 0L, "n0"));
		BuildEvent ev1 = new BuildEvent("b1", "j1", new Event(Event.Type.failure, 1L));
		String s = serializer.fromBuildEvents(ev0, ev1);
		assertTrue(s.contains("\"build\":\"b0\""));
		assertTrue(s.contains("\"job\":\"j0\""));
		assertTrue(s.contains("\"event\":{"));
		assertTrue(s.contains("\"type\":\"success\""));
		assertTrue(s.contains("\"timeStamp\":0"));
		assertTrue(s.contains("\"names\":[\"n0\"]"));
		assertTrue(s.contains("\"build\":\"b1\""));
		assertTrue(s.contains("\"job\":\"j1\""));
		assertTrue(s.contains("\"event\":{"));
		assertTrue(s.contains("\"type\":\"failure\""));
		assertTrue(s.contains("\"timeStamp\":1"));
		assertTrue(s.contains("\"names\":[]"));
	}

	@Test
	public void shouldSerializeBuildsType() {
		Serializer serializer = new Serializer();
		Builds builds = new Builds();
		String s = serializer.fromBuilds(builds);
		assertThat(s, is("{\"builds\":[]}"));
		builds.build("b0");
		builds.build("b1");
		s = serializer.fromBuilds(builds);
		assertTrue(s.contains("{\"builds\":["));
		assertTrue(s.contains("\"name\":\"b0\""));
		assertTrue(s.contains("\"name\":\"b1\""));
		assertTrue(s.contains("\"jobs\":[]"));
	}

	@Test
	public void shouldSerializeBuilds() {
		Serializer serializer = new Serializer();
		Build build = new Build("b0");
		String s = serializer.fromBuild(build);
		assertTrue(s.contains("\"name\":\"b0\""));
		assertTrue(s.contains("\"jobs\":[]"));
		build.job("j0");
		build.job("j1");
		s = serializer.fromBuild(build);
		assertTrue(s.contains("\"name\":\"b0\""));
		assertTrue(s.contains("\"jobs\":["));
		assertTrue(s.contains("\"name\":\"j0\""));
		assertTrue(s.contains("\"name\":\"j1\""));
		assertTrue(s.contains("\"events\":[]"));
	}

	@Test
	public void shouldSerializeJobs() {
		Serializer serializer = new Serializer();
		Job job = new Job("job");
		String s = serializer.fromJob(job);
		assertTrue(s.contains("\"name\":\"job\""));
		assertTrue(s.contains("\"events\":[]"));
		Event ev = new Event(Event.Type.failure, 0L, "name");
		job.events(ev);
		s = serializer.fromJob(job);
		assertTrue(s.contains("\"name\":\"job\""));
		assertTrue(s.contains("\"events\":["));
		assertTrue(s.contains("\"type\":\"failure\""));
		assertTrue(s.contains("\"timeStamp\":0"));
		assertTrue(s.contains("\"names\":[\"name\"]"));
	}

}
