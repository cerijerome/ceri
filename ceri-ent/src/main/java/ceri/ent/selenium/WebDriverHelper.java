package ceri.ent.selenium;

import java.io.Closeable;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebDriverHelper implements Closeable {
	private static final int TIMEOUT_SEC_DEF = 5;
	private final int timeoutSec;
	public final WebDriver driver;

	public WebDriverHelper(WebDriver driver) {
		this(driver, TIMEOUT_SEC_DEF);
	}

	public WebDriverHelper(WebDriver driver, int timeoutSec) {
		this.driver = driver;
		this.timeoutSec = timeoutSec;
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

	public void waitFor(BooleanSupplier test) {
		(new WebDriverWait(driver, timeoutSec)).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return test.getAsBoolean();
			}
		});
	}

	public static WebDriverHelper firefox(int timeoutSec) {
		return new WebDriverHelper(new FirefoxDriver(), timeoutSec);
	}

	public WebElement findElementById(String id) {
		List<WebElement> elements = driver.findElements(By.id(id));
		if (elements.isEmpty()) return null;
		return elements.get(0);
	}

	public List<WebElement> findElementsById(String id) {
		return driver.findElements(By.id(id));
	}

	public WebElement findElement(String xPath) {
		List<WebElement> elements = driver.findElements(By.xpath(xPath));
		if (elements.isEmpty()) return null;
		return elements.get(0);
	}

	public List<WebElement> findElements(String xPath) {
		return driver.findElements(By.xpath(xPath));
	}

}
