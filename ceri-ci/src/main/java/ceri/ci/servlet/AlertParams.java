package ceri.ci.servlet;

import java.util.Collection;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

public class AlertParams {
	public final String path;
	public final String build;
	public final String job;
	public final Collection<String> names;
	public final AlertAction action;
	private final int hashCode;

	private AlertParams(String path, String build, String job, AlertAction action,
		Collection<String> names) {
		this.path = path;
		this.build = build;
		this.job = job;
		this.action = action;
		this.names = names;
		hashCode = HashCoder.hash(path, build, job, action, names);
	}

	public static AlertParams read(String path, String build, String job) {
		return new AlertParams(path, build, job, AlertAction.read, null);
	}

	public static AlertParams delete(String path, String build, String job) {
		return new AlertParams(path, build, job, AlertAction.delete, null);
	}

	public static AlertParams clear(String path, String build, String job) {
		return new AlertParams(path, build, job, AlertAction.clear, null);
	}

	public static AlertParams
		fixed(String path, String build, String job, Collection<String> names) {
		return new AlertParams(path, build, job, AlertAction.success, names);
	}

	public static AlertParams
		broken(String path, String build, String job, Collection<String> names) {
		return new AlertParams(path, build, job, AlertAction.failure, names);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof AlertParams)) return false;
		AlertParams other = (AlertParams) obj;
		if (!EqualsUtil.equals(path, other.path)) return false;
		if (!EqualsUtil.equals(build, other.build)) return false;
		if (!EqualsUtil.equals(job, other.job)) return false;
		if (!EqualsUtil.equals(action, other.action)) return false;
		if (!EqualsUtil.equals(names, other.names)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, path, build, job, action, names).toString();
	}

}
