package ceri.ent.htmlunit;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import ceri.common.concurrent.ExceptionSupplier;
import ceri.common.io.IoUtil;

public class HtmlPageSampler {
	private static final Logger logger = LogManager.getLogger();

	/**
	 * Sample mode for saving/loading pages as xml (debugging)
	 */
	public static enum Mode {
		off,  // always load from url
		save, // always load from url, save to file 
		load, // always load from file
		auto  // if file exists, follow 'load', otherwise follow 'save'
	}

	/**
	 * Load a page depending on sample mode.
	 */
	public static HtmlPage loadPage(Mode mode,
		ExceptionSupplier<IOException, HtmlPage> pageSupplier, File file) throws IOException {
		return loadPage(mode, pageSupplier, () -> file);
	}

	/**
	 * Load a page depending on sample mode.
	 */
	public static HtmlPage loadPage(Mode mode,
		ExceptionSupplier<IOException, HtmlPage> pageSupplier, Supplier<File> fileSupplier)
		throws IOException {
		if (mode == null || mode == Mode.off) return pageSupplier.get();
		File f = fileSupplier.get();
		if (mode == Mode.auto) mode = f.exists() ? Mode.load : Mode.save; 
		if (mode == Mode.load) return loadPageFile(f);
		return savePageFile(pageSupplier.get(), f);
	}

	private static HtmlPage loadPageFile(File file) throws IOException {
		logger.info("Loading page from file: {}", file);
		return WebClientHelper.page(file);
	}

	private static HtmlPage savePageFile(HtmlPage page, File file) throws IOException {
		logger.info("Saving page to file: {}", file);
		IoUtil.setContentString(file, page.asXml());
		return page;
	}

}
