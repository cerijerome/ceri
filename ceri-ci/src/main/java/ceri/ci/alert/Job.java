package ceri.ci.alert;

import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Job {
	public final String name;
	public final Event lastBreak;
	public final Event lastFix;
	public final boolean broken;
	private final int hashCode;
	
	private Job(String name, Event lastBreak, Event lastFix) {
		this.name = name;
		this.lastBreak = lastBreak;
		this.lastFix = lastFix;
		broken = isBroken(lastBreak, lastFix);
		hashCode = HashCoder.hash(name, lastBreak, lastFix);
	}

	public static Job create(String name) {
		return new Job(name, null, null);
	}

	public static Job broken(Job job, Event event) {
		return new Job(job.name, event, job.lastFix);
	}

	public static Job fixed(Job job, Event event) {
		return new Job(job.name, job.lastBreak, event);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Job)) return false;
		Job other = (Job) obj;
		if (!EqualsUtil.equals(name,  other.name)) return false;
		if (!EqualsUtil.equals(lastBreak,  other.lastBreak)) return false;
		if (!EqualsUtil.equals(lastFix,  other.lastFix)) return false;
		return true;
	}
	
	private static boolean isBroken(Event lastBreak, Event lastFix) {
		if (lastBreak == null) return false;
		if (lastFix == null) return true;
		return lastBreak.after(lastFix);
	}

}
