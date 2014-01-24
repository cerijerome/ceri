package ceri.ci.web;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import ceri.ci.build.Event;
import ceri.ci.build.Job;
import ceri.common.io.IoUtil;

public class WebAlerter {
	private static final String MOST_WANTED_HTML = "_most-wanted.html";
	private static final String PLACEHOLDER = "__PLACEHOLDER__";
	private static final MessageFormat IMAGE = new MessageFormat(
		"<li><img src=\"{0}/{1}.jpg\"/></li>");
	private final File webDir;
	private final Object sync = new Object();
	private volatile String html;

	public WebAlerter(File webDir) {
		this.webDir = webDir;
	}

	public void update(Collection<Job> jobs) {
		Event event = null;//Job.aggregate(jobs);
		try {
			setHtml(loadHtml(null));//event.responsible));
		} catch (IOException e) {
			e.printStackTrace();
			setHtml(null); // ???
		}
	}

	public String html() {	
		return html;
	}
	
	private String loadHtml(Collection<String> keys) throws IOException {
		String html = IoUtil.getContentString(new File(webDir, MOST_WANTED_HTML));
		StringBuilder b = new StringBuilder();
		for (String key : keys)
			b.append(IMAGE.format(new String[] { "/", key }));
		return html.replaceAll(PLACEHOLDER, b.toString());
	}
	
	private void setHtml(String html) {
		synchronized (sync) {
			this.html = html;
		}
	}
	
}
