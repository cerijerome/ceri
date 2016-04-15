package ceri.ent.selenium;

import java.io.Closeable;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.google.common.base.Function;

public class WebDriverHelper implements Closeable {
	private static final int TIMEOUT_SEC_DEF = 5;
	private final int timeoutSec;
	public final WebDriver driver;
	public final JavascriptExecutor js;

	public WebDriverHelper(WebDriver driver) {
		this(driver, TIMEOUT_SEC_DEF);
	}

	public WebDriverHelper(WebDriver driver, int timeoutSec) {
		this.driver = driver;
		this.timeoutSec = timeoutSec;
		js = driver instanceof JavascriptExecutor ? (JavascriptExecutor) driver : null;
	}

	@Override
	public void close() {
		if (driver != null) driver.quit();
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

	public static WebDriverHelper firefox(int timeoutSec) {
		return new WebDriverHelper(new FirefoxDriver(), timeoutSec);
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
