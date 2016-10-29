package ceri.ent.selenium;

import java.io.Closeable;
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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import ceri.common.math.MathUtil;
import com.google.common.base.Function;

public class WebDriverHelper implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int TIMEOUT_MS_DEF = 5000;
	private final int timeoutSec;
	public final WebDriver driver;
	public final JavascriptExecutor js;

	public WebDriverHelper(WebDriver driver) {
		this(driver, TIMEOUT_MS_DEF);
	}

	public WebDriverHelper(WebDriver driver, long timeoutMs) {
		this.driver = driver;
		this.timeoutSec = (int) MathUtil.roundDiv(timeoutMs, 1000);
		js = driver instanceof JavascriptExecutor ? (JavascriptExecutor) driver : null;
	}

	@Override
	public void close() {
		if (driver != null) driver.quit();
	}

	public void get(String url) {
		try {
			driver.get(url);
		} catch (TimeoutException e) {
			logger.catching(Level.WARN, e);
			driver.navigate().refresh();
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

	public static WebDriverHelper firefox(int timeoutMs) {
		return new WebDriverHelper(new FirefoxDriver(), timeoutMs);
	}

	public WebElement findElement(By by) {
		try {
			return driver.findElement(by);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public WebElement findElementById(String id) {
		return findElement(By.id(id));
	}

	public List<WebElement> findElementsById(String id) {
		return driver.findElements(By.id(id));
	}

	public WebElement findElement(String xPath) {
		return findElement(By.xpath(xPath));
	}

	public List<WebElement> findElements(String xPath) {
		return driver.findElements(By.xpath(xPath));
	}

	public String pageSource() {
		return driver.getPageSource();
	}
	
}
