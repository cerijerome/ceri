package ceri.ci.ecg.sjc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.MessagingException;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Event;
import ceri.ci.email.Email;
import ceri.ci.email.EmailEventParser;
import ceri.common.util.StringUtil;

/**
 * Parses build event emails sent from the Bolt and MWeb Jenkins environments.
 */
public class BoltMWebEmailParser implements EmailEventParser {
	private static final Pattern BUILD_JOB_NAME_REGEX = Pattern.compile("(\\w+)\\-(\\w+)");
	private static final Pattern CONTENT_SEPARATOR_REGEX = Pattern.compile("^#+");
	private static final Pattern COMMIT_ID_REGEX = Pattern.compile("(?i)build\\s*#\\s*(\\d+)");
	private static final Pattern COMMITTER_REGEX = Pattern.compile("^\\[(\\w+)\\]");
	private static final Pattern FIXED_REGEX = Pattern.compile("(?i)fixed");
	private static final Pattern FAIL_REGEX = Pattern.compile("(?i)fail");
	private static final Map<String, String> buildAliasMap;
	private static final Map<String, String> jobAliasMap;

	/**
	 * Builds the map of build name aliases. Used to convert names found in the email to actual
	 * build names.
	 */
	static {
		Map<String, String> map = new HashMap<>();
		map.put("boltci", "bolt");
		map.put("xtof", "mweb");
		buildAliasMap = Collections.unmodifiableMap(map);
	}

	/**
	 * Builds the map of job name aliases. Used to convert names found in the email to actual job
	 * names.
	 */
	static {
		Map<String, String> map = new HashMap<>();
		map.put("boltci", "bolt");
		map.put("xtof", "mweb");
		jobAliasMap = Collections.unmodifiableMap(map);
	}

	/**
	 * Check email subject if it matches the expected syntax.
	 */
	@Override
	public boolean matches(Message message) throws MessagingException {
		String subject = message.getSubject();
		return eventType(subject) != null;
	}

	/**
	 * Parses the email subject and content to create a build event. Returns null if not in the
	 * correct syntax.
	 */
	@Override
	public BuildEvent parse(Email email) {
		if (email == null || email.subject == null) return null;
		Event event = createEvent(email);
		if (event == null) return null;
		return createBuildEvent(email.subject, event);
	}

	/**
	 * Creates a build event.
	 */
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

	/**
	 * Creates a success or fail event from email subject and content.
	 */
	private Event createEvent(Email email) {
		if (email.subject == null || email.content == null) return null;
		Event.Type type = eventType(email.subject);
		if (type == null) return null;
		Collection<String> committers = committers(email.subject, email.content);
		if (committers == null) return null;
		return new Event(type, email.sentDateMs, committers);
	}

	/**
	 * Attempts to find a commit id in the given line. Returns null if no match.
	 */
	private String commitId(String line) {
		Matcher m = COMMIT_ID_REGEX.matcher(line);
		if (!m.find()) return null;
		return m.group(1);
	}

	/**
	 * Finds committer names based on the commit id in the subject, and the commit detail block for
	 * the commit id in the email content.
	 */
	private Collection<String> committers(String subject, String content) {
		String commitId = commitId(subject);
		if (commitId == null) return null;
		String[] lines = StringUtil.NEWLINE_REGEX.split(content);
		Iterator<String> i = Arrays.asList(lines).iterator();
		skipToCommits(i);
		skipToCommitId(commitId, i);
		return readCommitters(i);
	}

	/**
	 * Skips lines until the marker for the start of commit details.
	 */
	private void skipToCommits(Iterator<String> i) {
		while (i.hasNext())
			if (CONTENT_SEPARATOR_REGEX.matcher(i.next()).matches()) break;
	}

	/**
	 * Skips lines until the given commit id is found.
	 */
	private void skipToCommitId(String commitId, Iterator<String> i) {
		while (i.hasNext()) {
			String id = commitId(i.next());
			if (commitId.equals(id)) break;
		}
	}

	/**
	 * Reads committer names from each line until a new commit id is found, or no more lines are
	 * left.
	 */
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

	/**
	 * Checks the email subject for failure or success events.
	 */
	private Event.Type eventType(String subject) {
		boolean fail = FAIL_REGEX.matcher(subject).find();
		if (fail) return Event.Type.failure;
		boolean success = FIXED_REGEX.matcher(subject).find();
		if (success) return Event.Type.success;
		return null;
	}

}
