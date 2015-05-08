package ceri.ent.htmlunit;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import ceri.common.io.IoUtil;
import ceri.ent.web.SampleHeader;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class WebClientHelper implements Closeable {
	private static final SampleHeader HEADER_DEF = SampleHeader.Chrome_MaxOSX;
	private static final int DEFAULT_TIMEOUT_MS = 60_000;
	protected final WebClient webClient;

	static {
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.SEVERE);
	}

	public WebClientHelper() {
		this(HEADER_DEF);
	}

	public WebClientHelper(SampleHeader header) {
		webClient = new WebClient();
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.setCssErrorHandler(new SilentCssErrorHandler());
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setTimeout(DEFAULT_TIMEOUT_MS);
		if (header != null) setHeaders(webClient, header);
	}

	@Override
	public void close() {
		webClient.closeAllWindows();
	}

	public String getContent(String url) throws IOException {
		return getPage(url).getWebResponse().getContentAsString();
	}

	public HtmlPage getPage(String url) throws IOException {
		return webClient.getPage(url);
	}

	public HtmlPage getPage(File file) throws IOException {
		String url = "file:///" + IoUtil.unixPath(file.getAbsolutePath());
		return getPage(url, file);
	}

	public HtmlPage getPage(String url, File file) throws IOException {
		String content = IoUtil.getContentString(file);
		StringWebResponse response = new StringWebResponse(content, new URL(url));
		return HTMLParser.parseHtml(response, new TopLevelWindow("", webClient) {
			private static final long serialVersionUID = 0L;
		});
	}
	
	public static void setHeaders(WebClient webClient, SampleHeader header) {
		webClient.addRequestHeader("User-Agent", header.userAgent);
		webClient.addRequestHeader("Accept", header.accept);
		webClient.addRequestHeader("Accept-Language", header.acceptLanguage);
		webClient.addRequestHeader("Accept-Encoding", header.acceptEncoding);
	}

	public static HtmlPage page(String url) throws IOException {
		try (WebClientHelper downloader = new WebClientHelper()) {
			return downloader.getPage(url);
		}
	}

	public static String content(String url) throws IOException {
		try (WebClientHelper downloader = new WebClientHelper()) {
			return downloader.getContent(url);
		}
	}

}
