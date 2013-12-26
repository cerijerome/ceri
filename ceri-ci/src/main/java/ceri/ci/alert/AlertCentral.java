package ceri.ci.alert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import ceri.ci.audio.AudioAlerter;
import ceri.ci.job.Event;
import ceri.ci.job.Job;
import ceri.ci.job.JobCentral;
import ceri.ci.veralite.VeraLiteAlerter;
import ceri.ci.web.WebAlerter;
import ceri.ci.x10.X10Alerter;
import ceri.common.event.EventListener;
import ceri.common.io.IoUtil;

/**
 * List of jobs A, B, C, D, E
 * Each one has:
 * - name
 * - good/bad state
 * - time of last state change
 * - alert reminder period?
 * - who last fixed it
 * - who last broke it
 * 
 * Web => heroes/villains grouped by job
 * ZWave => aggregate of broken jobs
 * X10 => aggregate of broken jobs
 * Audio =>
 * 
 */
public class AlertCentral implements Closeable {
	private static final String PEOPLE = "people";
	private static final String HTML = "html";
	private static final String CLIPS = "clips";
	private final X10Alerter x10;
	private final VeraLiteAlerter veraLite;
	private final AudioAlerter audio;
	private final WebAlerter web;
	public final JobCentral jobs;

	public static void main(String[] args) throws Exception {
		try (AlertCentral alerter = new AlertCentral("./data")) {
			alerter.jobs.broken("smoke", Arrays.asList("shuochen", "moromero"));
		}
	}

	public AlertCentral(String root) {
		File rootDir = new File(root);
		x10 = createX10(rootDir);
		veraLite = createVeraLite(rootDir);
		audio = createAudio(rootDir);
		web = createWeb(rootDir);
		jobs = createJobs();
	}
	
	@Override
	public void close() throws IOException {
		IoUtil.close(audio);
		IoUtil.close(x10);
	}

	void jobsChanged(final Collection<Job> jobs) {
		Event event = Job.aggregate(jobs);
		// alert
	}
	
	private VeraLiteAlerter createVeraLite(File rootDir) {
		try {
			return VeraLiteAlerter.create(new File(rootDir, PEOPLE), null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private X10Alerter createX10(File rootDir) {
		try {
			return X10Alerter.create(new File(rootDir, PEOPLE), null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private AudioAlerter createAudio(File rootDir) {
		return new AudioAlerter(new File(rootDir, PEOPLE), new File(rootDir, CLIPS));
	}

	private WebAlerter createWeb(File rootDir) {
		return new WebAlerter("../" + PEOPLE, new File(rootDir, HTML));
	}

	private JobCentral createJobs() {
		JobCentral.Builder builder = JobCentral.builder();
		builder.listener(new EventListener<Collection<Job>>() {
			@Override
			public void event(final Collection<Job> jobs) {
				jobsChanged(jobs);
			}
		});
		return builder.build();
	}
	
}
