package ceri.ci.phone;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.AnalyzedJob;
import ceri.ci.build.BuildAnalyzer;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.build.Job;
import ceri.ci.common.Alerter;
import ceri.common.collection.ImmutableUtil;

public class PhoneAlerter implements Alerter {
	private static final Logger logger = LogManager.getLogger();
	private final PhoneClient client;
	private final Map<String, String> numbers;
	private final BuildAnalyzer buildAnalyzer = new BuildAnalyzer();
	private final DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

	public static class Builder {
		final Map<String, String> numbers = new HashMap<>();
		final PhoneClient client;

		Builder(PhoneClient client) {
			this.client = client;
		}

		public Builder number(String name, String phoneNumber) {
			if (name == null) throw new NullPointerException("Name cannot be null");
			if (phoneNumber == null) throw new NullPointerException("Phone number cannot be null");
			numbers.put(name, phoneNumber);
			return this;
		}

		public PhoneAlerter build() {
			return new PhoneAlerter(this);
		}
	}

	public static Builder builder(PhoneClient client) {
		return new Builder(client);
	}

	PhoneAlerter(Builder builder) {
		client = builder.client;
		numbers = ImmutableUtil.copyAsMap(builder.numbers);

	}

	/**
	 * Sends SMS to names who have just broken
	 */
	@Override
	public void update(Builds builds) {
		Collection<AnalyzedJob> analyzedJobs = buildAnalyzer.update(builds);
		if (analyzedJobs.isEmpty()) return;
		Map<String, Map<String, Collection<String>>> nameBuildJobs = nameBuildJobs(analyzedJobs);
		logger.info("Alerting for {}", nameBuildJobs.keySet());
		for (Map.Entry<String, Map<String, Collection<String>>> entry : nameBuildJobs.entrySet()) {
			String name = entry.getKey();
			if (numbers.get(name) == null) continue;
			String message = createMessage(name, entry.getValue());
			alert(name, message);
		}
	}

	@Override
	public void clear() {
		// Do nothing
	}

	@Override
	public void remind() {
		// Do nothing
	}

	/**
	 * Send message as SMS if a phone number is configured for the name.
	 */
	public void alert(String name, String message) {
		String phoneNumber = numbers.get(name);
		if (phoneNumber == null) return;
		client.sendSms(phoneNumber, message);
	}

	private String createMessage(String name, Map<String, Collection<String>> buildJobs) {
		Collection<String> jobNames = jobNames(buildJobs);
		return "(" + currentTimeStamp() + ") Hi " + name + //
			", the build is broken. Please fix" + " " + jobNamesPhrase(jobNames) + ". Thank you.";
	}

	private String jobNamesPhrase(Collection<String> jobNames) {
		StringBuilder b = new StringBuilder();
		Iterator<String> i = jobNames.iterator();
		boolean first = true;
		while (i.hasNext()) {
			String job = i.next();
			if (!first && !i.hasNext()) b.append(" and ");
			else if (!first) b.append(", ");
			first = false;
			b.append(job);
		}
		return b.toString();
	}

	private Collection<String> jobNames(Map<String, Collection<String>> buildJobs) {
		Collection<String> jobNames = new LinkedHashSet<>();
		for (Map.Entry<String, Collection<String>> entry : buildJobs.entrySet())
			for (String job : entry.getValue())
				jobNames.add(entry.getKey() + "-" + job);
		return jobNames;
	}

	private Map<String, Map<String, Collection<String>>>
		nameBuildJobs(Collection<AnalyzedJob> analyzedJobs) {
		Map<String, Map<String, Collection<String>>> nameBuildJobs = new TreeMap<>();
		for (AnalyzedJob analyzedJob : analyzedJobs) {
			for (Job job : analyzedJob.justBroken) {
				Event event = BuildUtil.latestEvent(job);
				for (String name : event.names)
					add(name, analyzedJob.build, job.name, nameBuildJobs);
			}
		}
		return nameBuildJobs;
	}

	private void add(String name, String build, String job,
		Map<String, Map<String, Collection<String>>> nameBuildJobs) {
		Map<String, Collection<String>> buildJobs =
			nameBuildJobs.computeIfAbsent(name, k -> new TreeMap<>());
		Collection<String> jobs = buildJobs.computeIfAbsent(build, k -> new TreeSet<>());
		jobs.add(job);
	}

	private String currentTimeStamp() {
		return dateFormat.format(new Date());
	}

}
