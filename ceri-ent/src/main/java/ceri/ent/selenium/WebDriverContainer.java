package ceri.ent.selenium;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.google.common.base.Function;
import ceri.common.function.ExceptionSupplier;
import ceri.common.math.MathUtil;

/**
 * A container and support methods for selenium web drivers. Supports lifecycle management with
 * create, reset, and close.
 */
public class WebDriverContainer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int TIMEOUT_MS_DEF = 5000;
	private static final String BLANK_URL = "about:blank";
	private final ExceptionSupplier<IOException, WebDriver> constructor;
	private final int timeoutSec;
	private final boolean canReset;
	private volatile WebDriver driver;

	public static WebDriverContainer simple(WebDriver driver) throws IOException {
		return simple(driver, TIMEOUT_MS_DEF);
	}

	public static WebDriverContainer simple(WebDriver driver, long timeoutMs) throws IOException {
		return new WebDriverContainer(() -> driver, timeoutMs, false);
	}

	public WebDriverContainer(ExceptionSupplier<IOException, WebDriver> constructor)
		throws IOException {
		this(constructor, TIMEOUT_MS_DEF);
	}

	public WebDriverContainer(ExceptionSupplier<IOException, WebDriver> constructor, long timeoutMs)
		throws IOException {
		this(constructor, timeoutMs, true);
	}

	private WebDriverContainer(ExceptionSupplier<IOException, WebDriver> constructor,
		long timeoutMs, boolean canReset) throws IOException {
		logger.info("{} started", getClass().getSimpleName());
		this.constructor = constructor;
		this.timeoutSec = (int) MathUtil.roundDiv(timeoutMs, 1000);
		this.canReset = canReset;
		driver = create(constructor);
	}

	public void reset() throws IOException {
		if (!canReset) throw new UnsupportedOperationException();
		logger.info("Resetting web driver");
		driver = create(constructor);
	}

	@Override
	public void close() {
		if (driver != null) driver.quit();
		logger.info("{} stopped", getClass().getSimpleName());
	}

	/**
	 * Never store the returned value as it will change after a reset.
	 */
	public WebDriver driver() {
		return driver;
	}

	/**
	 * Never store the returned value as it will change after a reset.
	 */
	public JavascriptExecutor js() {
		if (driver instanceof JavascriptExecutor) return (JavascriptExecutor) driver;
		return WebDriverUtil.nullJs();
	}

	public void getBlank() {
		get(BLANK_URL);
	}

	public void get(String url) {
		try {
			driver().get(url);
		} catch (TimeoutException e) {
			logger.catching(Level.WARN, e);
			driver().navigate().refresh();
		}
	}

	public WebElement waitForElement(String xPath) {
		return waitForElement(xPath, timeoutSec);
	}

	public WebElement waitForElement(String xPath, int timeoutSec) {
		waitFor(() -> findElement(xPath) != null, timeoutSec);
		return findElement(xPath);
	}

	public List<WebElement> waitForElements(String xPath) {
		return waitForElements(xPath, timeoutSec);
	}

	public List<WebElement> waitForElements(String xPath, int timeoutSec) {
		waitFor(() -> !findElements(xPath).isEmpty(), timeoutSec);
		return findElements(xPath);
	}

	public void waitFor(BooleanSupplier test) {
		waitFor(test, timeoutSec);
	}

	public void waitFor(BooleanSupplier test, int timeoutSec) {
		Function<WebDriver, Boolean> fn = driver -> test.getAsBoolean();
		waitFor(fn, timeoutSec);
	}

	public <T> T waitFor(Function<? super WebDriver, T> isTrue) {
		return waitFor(isTrue, timeoutSec);
	}

	public <T> T waitFor(Function<? super WebDriver, T> isTrue, int timeoutSec) {
		return (new WebDriverWait(driver, timeoutSec)).until(isTrue);
	}

	public WebElement findElement(By by) {
		try {
			return driver().findElement(by);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public WebElement findElementById(String id) {
		return findElement(By.id(id));
	}

	public List<WebElement> findElementsById(String id) {
		return driver().findElements(By.id(id));
	}

	public WebElement findElement(String xPath) {
		return findElement(By.xpath(xPath));
	}

	public List<WebElement> findElements(String xPath) {
		return driver().findElements(By.xpath(xPath));
	}

	public String pageSource() {
		return driver().getPageSource();
	}

	private WebDriver create(ExceptionSupplier<IOException, WebDriver> constructor)
		throws IOException {
		return constructor.get();
	}

}
