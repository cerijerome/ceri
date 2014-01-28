package ceri.ci.web;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import ceri.ci.build.Builds;
import ceri.common.io.IoUtil;

public class WebAlerter {
	private static final String MOST_WANTED_HTML = "_most-wanted.html";
	private static final String PLACEHOLDER = "__PLACEHOLDER__";
	private static final MessageFormat IMAGE = new MessageFormat(
		"<li><img src=\"{0}/{1}.jpg\"/></li>");
	private final File webDir;
	private volatile Builds builds;

	public WebAlerter(File webDir) {
		this.webDir = webDir;
		clear();
	}

	public void update(Builds builds) {
		this.builds = new Builds(builds);
	}

	public void clear() {
		builds = new Builds();
	}

	private String loadHtml(Collection<String> keys) throws IOException {
		String html = IoUtil.getContentString(new File(webDir, MOST_WANTED_HTML));
		StringBuilder b = new StringBuilder();
		for (String key : keys)
			b.append(IMAGE.format(new String[] { "/", key }));
		return html.replaceAll(PLACEHOLDER, b.toString());
	}

}
