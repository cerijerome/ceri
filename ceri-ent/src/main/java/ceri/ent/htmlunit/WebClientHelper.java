package ceri.ent.htmlunit;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import ceri.ent.web.SampleHeader;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class WebClientHelper implements Closeable {
	private static final SampleHeader HEADER_DEF = SampleHeader.Chrome_MaxOSX;
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
		if (header != null) setHeaders(header);
	}

	private void setHeaders(SampleHeader header) {
		webClient.addRequestHeader("User-Agent", header.userAgent);
		webClient.addRequestHeader("Accept", header.accept);
		webClient.addRequestHeader("Accept-Language", header.acceptLanguage);
		webClient.addRequestHeader("Accept-Encoding", header.acceptEncoding);
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
