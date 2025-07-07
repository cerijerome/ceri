package ceri.ent.htmlunit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import ceri.common.function.Excepts;

public class HtmlPageSampler {
	private static final Logger logger = LogManager.getLogger();

	/**
	 * Sample mode for saving/loading pages as xml (debugging)
	 */
	public enum Mode {
		/** Always load from url */
		off,
		/** Always load from url, and save to file */
		save,
		/** Always load from file */
		load,
		/** If file exists, follow 'load', otherwise follow 'save' */
		auto
	}

	/**
	 * Load a page depending on sample mode.
	 */
	public static HtmlPage loadPage(Mode mode, Excepts.Supplier<IOException, HtmlPage> pageSupplier,
		Path file) throws IOException {
		return loadPage(mode, pageSupplier, () -> file);
	}

	/**
	 * Load a page depending on sample mode.
	 */
	public static HtmlPage loadPage(Mode mode, Excepts.Supplier<IOException, HtmlPage> pageSupplier,
		Supplier<Path> fileSupplier) throws IOException {
		if (mode == null || mode == Mode.off) return pageSupplier.get();
		Path f = fileSupplier.get();
		if (mode == Mode.auto) mode = Files.exists(f) ? Mode.load : Mode.save;
		if (mode == Mode.load) return loadPageFile(f);
		return savePageFile(pageSupplier.get(), f);
	}

	private static HtmlPage loadPageFile(Path file) throws IOException {
		logger.info("Loading page from file: {}", file);
		return WebClientHelper.page(file);
	}

	private static HtmlPage savePageFile(HtmlPage page, Path file) throws IOException {
		logger.info("Saving page to file: {}", file);
		Files.writeString(file, page.asXml());
		return page;
	}

}
