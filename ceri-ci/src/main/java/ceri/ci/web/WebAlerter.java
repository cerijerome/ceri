package ceri.ci.web;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import ceri.ci.common.Alerter;
import ceri.common.io.IoUtil;

public class WebAlerter implements Alerter {
	private static final String MOST_WANTED_HTML = "most-wanted.html";
	private static final String PLACEHOLDER = "__PLACEHOLDER__";
	private static final MessageFormat IMAGE = new MessageFormat(
		"<li><img src=\"{0}/{1}.jpg\"/></li>");
	private final String imgDir;
	private final File htmlDir;
	private final Object sync = new Object();
	private volatile String html;

	public WebAlerter(String imgDir, File htmlDir) {
		this.imgDir = imgDir;
		this.htmlDir = htmlDir;
	}

	@Override
	public void alert(String... keys) {
		try {
			setHtml(loadHtml(keys));
		} catch (IOException e) {
			e.printStackTrace();
			setHtml(null); // ???
		}
	}

	@Override
	public void clear(String... keys) {

	}

	@Override
	public void close() throws IOException {
		// Do nothing
	}
	
	public String html() {	
		return html;
	}
	
	private String loadHtml(String...keys) throws IOException {
		String html = IoUtil.getContentString(new File(htmlDir, MOST_WANTED_HTML));
		StringBuilder b = new StringBuilder();
		for (String key : keys)
			b.append(IMAGE.format(new String[] { imgDir, key }));
		return html.replaceAll(PLACEHOLDER, b.toString());
	}
	
	private void setHtml(String html) {
		synchronized (sync) {
			this.html = html;
		}
	}
	
}
