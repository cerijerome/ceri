package ceri.ci.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

public class WebParams {
	private static final Pattern PATH_SPLIT = Pattern.compile("/*([^/]+)");
	public final String path;
	public final String build;
	public final String job;
	private final int hashCode;
	
	private WebParams(String path, String build, String job) {
		this.path = path;
		this.build = build;
		this.job = job;
		hashCode = HashCoder.hash(build, job);
	}
	
	public static WebParams createFromRequest(HttpServletRequest request) {
		String path = request.getServletPath();
		String build = null;
		String job = null;
		Matcher m = PATH_SPLIT.matcher(path);
		if (m.find()) build = m.group(1);
		if (m.find()) job = m.group(1);
		return new WebParams(path, build, job);
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof WebParams)) return false;
		WebParams other = (WebParams)obj;
		return EqualsUtil.equals(build,  other.build) && EqualsUtil.equals(job,  other.job);
	}
	
	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, build, job).toString();
	}
	
}
