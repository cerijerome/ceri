package ceri.ent.htmlunit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import ceri.common.function.Functions;
import ceri.common.net.NetUtil;
import ceri.ent.web.SampleHeader;

public class WebClientHelper implements Functions.Closeable {
	private static final int DEFAULT_TIMEOUT_MS = 60_000;
	private static final int DEFAULT_JS_TIMEOUT_MS = 10_000;
	protected final WebClient webClient;

	public static void disableGargoyleLog() {
		// Static initialization doesn't always work, now called from constructor
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
	}

	public static class Builder {
		boolean jsEnabled = false;
		SampleHeader header = SampleHeader.Chrome_MacOSX;

		Builder() {}

		public Builder enableJs() {
			jsEnabled = true;
			return this;
		}

		public Builder header(SampleHeader header) {
			this.header = header;
			return this;
		}

		public WebClientHelper build() {
			return new WebClientHelper(this);
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	public static WebClientHelper create() {
		return builder().build();
	}

	public static WebClientHelper createWithJs() {
		return builder().enableJs().build();
	}

	WebClientHelper(Builder builder) {
		webClient = new WebClient();
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setJavaScriptEnabled(builder.jsEnabled);
		if (builder.jsEnabled)
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		webClient.setCssErrorHandler(new SilentCssErrorHandler());
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setTimeout(DEFAULT_TIMEOUT_MS);
		if (builder.header != null) setHeaders(webClient, builder.header);
		disableGargoyleLog();
	}

	@Override
	public void close() {
		webClient.close();
	}

	public int waitForJs() {
		return waitForJs(DEFAULT_JS_TIMEOUT_MS);
	}

	public int waitForJs(int timeoutMs) {
		return webClient.waitForBackgroundJavaScript(timeoutMs);
	}

	public String getContent(String url) throws IOException {
		return getPage(url).getWebResponse().getContentAsString();
	}

	public HtmlPage getPage(String url) throws IOException {
		return webClient.getPage(url);
	}

	public HtmlPage getPage(Path file) throws IOException {
		return getPage(file.toUri().toString(), file);
	}

	public HtmlPage getPage(String url, Path file) throws IOException {
		String content = Files.readString(file);
		StringWebResponse response = new StringWebResponse(content, NetUtil.url(url));
		return (HtmlPage) webClient.getPageCreator().createPage(response,
			new TopLevelWindow("", webClient) {});
	}

	public static void setHeaders(WebClient webClient, SampleHeader header) {
		webClient.addRequestHeader("User-Agent", header.userAgent);
		webClient.addRequestHeader("Accept", header.accept);
		webClient.addRequestHeader("Accept-Language", header.acceptLanguage);
		webClient.addRequestHeader("Accept-Encoding", header.acceptEncoding);
	}

	public static HtmlPage page(String url) throws IOException {
		try (WebClientHelper downloader = WebClientHelper.create()) {
			return downloader.getPage(url);
		}
	}

	public static HtmlPage page(Path file) throws IOException {
		try (WebClientHelper downloader = WebClientHelper.create()) {
			return downloader.getPage(file);
		}
	}

	public static String content(String url) throws IOException {
		try (WebClientHelper downloader = WebClientHelper.create()) {
			return downloader.getContent(url);
		}
	}
}
