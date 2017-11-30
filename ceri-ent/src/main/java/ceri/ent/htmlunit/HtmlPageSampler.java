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
		off,
		save,
		load
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
		if (mode == Mode.load) return loadPageFile(fileSupplier.get());
		HtmlPage page = pageSupplier.get();
		if (mode == Mode.save) savePageFile(page, fileSupplier.get());
		return page;
	}

	private static HtmlPage loadPageFile(File file) throws IOException {
		logger.info("Loading page from file: {}", file);
		return WebClientHelper.page(file);
	}

	private static void savePageFile(HtmlPage page, File file) throws IOException {
		logger.info("Saving page to file: {}", file);
		IoUtil.setContentString(file, page.asXml());
	}

}
