package ceri.ci.alert;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ceri.common.io.IoUtil;
import ceri.common.util.BasicUtil;

public class JobAggregator {
	private final Map<String, Job> jobs = new ConcurrentHashMap<>();

	public void fixed(String jobName, Event event) {
		Job job = jobs.get(jobName);
		if (job == null) job = Job.create(jobName);
		job = Job.fixed(job, event);
		jobs.put(jobName,  job);
	}

	public static void main(String[] args) throws Exception {
		BasicUtil.delay(6000);
		Robot r = new Robot();
		r.setAutoWaitForIdle(true);
		r.mouseMove(250, 550);
		long t = System.currentTimeMillis();
		while (true) {
			r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			//r.delay(2);
			r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			if (IoUtil.getChar() == 'x') break;
			r.delay(10);
			if (System.currentTimeMillis() > t + 25000) {
				r.delay(5000);
				t = System.currentTimeMillis();
			}
		}
	}
	
	private Collection<String> responsibleForBreaks(Collection<Job> jobs) {
		Collection<String> responsible = new HashSet<>();
		for (Job job : jobs) {
			if (!job.broken) continue;
			responsible.addAll(job.lastBreak.responsible);
		}
		return responsible;
	}
	
	private Collection<String> responsibleForFixes(Collection<Job> jobs) {
		Collection<String> responsible = new HashSet<>();
		for (Job job : jobs) {
			if (job.broken || job.lastFix == null) continue;
			responsible.addAll(job.lastFix.responsible);
		}
		return responsible;
	}
	
}
