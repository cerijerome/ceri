package ceri.ent.selenium;

import java.io.Closeable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
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
import ceri.common.math.MathUtil;
import com.google.common.base.Function;

/**
 * A container and support methods for selenium web drivers. Supports lifecycle management with
 * create, reset, and close.
 */
public class WebDriverContainer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int TIMEOUT_MS_DEF = 5000;
	private final Supplier<WebDriver> constructor;
	private final int timeoutSec;
	private final boolean canReset;
	private volatile WebDriver driver;

	public static WebDriverContainer simple(WebDriver driver) {
		return simple(driver, TIMEOUT_MS_DEF);
	}

	public static WebDriverContainer simple(WebDriver driver, long timeoutMs) {
		return new WebDriverContainer(() -> driver, timeoutMs, false);
	}

	public WebDriverContainer(Supplier<WebDriver> constructor) {
		this(constructor, TIMEOUT_MS_DEF);
	}

	public WebDriverContainer(Supplier<WebDriver> constructor, long timeoutMs) {
		this(constructor, timeoutMs, true);
	}

	private WebDriverContainer(Supplier<WebDriver> constructor, long timeoutMs, boolean canReset) {
		logger.info("{} started", getClass().getSimpleName());
		this.constructor = constructor;
		this.timeoutSec = (int) MathUtil.roundDiv(timeoutMs, 1000);
		this.canReset = canReset;
		driver = create(constructor);
	}

	public void reset() {
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

	public void get(String url) {
		try {
			driver().get(url);
		} catch (TimeoutException e) {
			logger.catching(Level.WARN, e);
			driver().navigate().refresh();
		}
	}

	public WebElement waitForElement(String xPath) {
		waitFor(() -> findElement(xPath) != null);
		return findElement(xPath);
	}

	public List<WebElement> waitForElements(String xPath) {
		waitFor(() -> !findElements(xPath).isEmpty());
		return findElements(xPath);
	}

	public <T> T waitFor(Function<? super WebDriver, T> isTrue) {
		return (new WebDriverWait(driver, timeoutSec)).until(isTrue);
	}

	public void waitFor(BooleanSupplier test) {
		waitFor(driver -> test.getAsBoolean());
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

	private WebDriver create(Supplier<WebDriver> constructor) {
		return constructor.get();
	}

}
