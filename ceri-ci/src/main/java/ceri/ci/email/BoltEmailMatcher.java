package ceri.ci.email;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Event;
import ceri.common.util.StringUtil;

public class BoltEmailMatcher implements EmailEventMatcher {
	private static final Logger logger = LogManager.getLogger();
	private static final Pattern BUILD_JOB_NAME_REGEX = Pattern.compile("(\\w+)\\-(\\w+)");
	private static final Pattern CONTENT_SEPARATOR_REGEX = Pattern.compile("^#+");
	private static final Pattern COMMIT_ID_REGEX = Pattern.compile("(?i)build\\s*#\\s*(\\d+)");
	private static final Pattern COMMITTER_REGEX = Pattern.compile("^\\[(\\w+)\\]");
	private static final Pattern SUCCESS_REGEX = Pattern.compile("(?i)success");
	private static final Pattern FAIL_REGEX = Pattern.compile("(?i)fail");
	private static final Map<String, String> buildAliasMap;
	private static final Map<String, String> jobAliasMap;

	static {
		Map<String, String> map = new HashMap<>();
		map.put("boltci", "bolt");
		map.put("xtof", "mweb");
		buildAliasMap = Collections.unmodifiableMap(map);
	}

	static {
		Map<String, String> map = new HashMap<>();
		map.put("boltci", "bolt");
		map.put("xtof", "mweb");
		jobAliasMap = Collections.unmodifiableMap(map);
	}

	public static void main(String[] args) throws IOException {
		EmailRetriever retriever =
			EmailRetriever.builder("imap.gmail.com", "ecg.sjc.ci.alert@gmail.com", "ecgsjccialert")
				.build();
		BoltEmailMatcher matcher = new BoltEmailMatcher();
		Collection<Email> emails = retriever.fetch();
		for (Email email : emails) {
			System.out.println(email);
			BuildEvent event = matcher.getEvent(email);
			System.out.println(event);
			System.out.println("--------------------");
		}
	}

	@Override
	public BuildEvent getEvent(Email email) {
		if (email == null || email.subject == null) return null;
		Event event = createEvent(email);
		if (event == null) return null;
		return createBuildEvent(email.subject, event);
	}

	private BuildEvent createBuildEvent(String subject, Event event) {
		Matcher m = BUILD_JOB_NAME_REGEX.matcher(subject);
		if (!m.find()) return null;
		String build = m.group(1).toLowerCase();
		String job = m.group(2).toLowerCase();
		String buildAlias = buildAliasMap.get(build);
		if (buildAlias != null) build = buildAlias;
		String jobAlias = jobAliasMap.get(job);
		if (jobAlias != null) job = jobAlias;
		return new BuildEvent(build, job, event);
	}

	private Event createEvent(Email email) {
		if (email.subject == null || email.content == null) return null;
		Boolean success = success(email.subject);
		if (success == null) return null;
		Collection<String> committers = committers(email.subject, email.content);
		if (committers == null) return null;
		Event.Type type = success ? Event.Type.fixed : Event.Type.broken;
		return new Event(type, email.sentDateMs, committers);
	}

	private String commitId(String line) {
		Matcher m = COMMIT_ID_REGEX.matcher(line);
		if (!m.find()) return null;
		return m.group(1);
	}

	private Collection<String> committers(String subject, String content) {
		String commitId = commitId(subject);
		if (commitId == null) return null;
		String[] lines = StringUtil.NEWLINE_REGEX.split(content);
		Iterator<String> i = Arrays.asList(lines).iterator();
		skipToCommits(i);
		skipToCommitId(commitId, i);
		return readCommitters(i);
	}

	private void skipToCommits(Iterator<String> i) {
		while (i.hasNext())
			if (CONTENT_SEPARATOR_REGEX.matcher(i.next()).matches()) break;
	}

	private void skipToCommitId(String commitId, Iterator<String> i) {
		while (i.hasNext()) {
			String id = commitId(i.next());
			if (commitId.equals(id)) break;
		}
	}

	private Collection<String> readCommitters(Iterator<String> i) {
		Collection<String> committers = new LinkedHashSet<>();
		while (i.hasNext()) {
			String line = i.next();
			// If the next commit section is found, stop reading
			if (commitId(line) != null) break;
			Matcher m = COMMITTER_REGEX.matcher(line);
			if (m.find()) committers.add(m.group(1));
		}
		return committers;
	}

	private Boolean success(String subject) {
		boolean success = SUCCESS_REGEX.matcher(subject).find();
		boolean fail = FAIL_REGEX.matcher(subject).find();
		if (!success && !fail) return null;
		if (success && fail) {
			logger.warn("Success and Failure both detected: {}", subject);
			return null;
		}
		return Boolean.valueOf(success);
	}

}
