package ceri.ent.selenium;

import org.openqa.selenium.JavascriptExecutor;

public class WebDriverUtil {
	public static final JavascriptExecutor NULL_JS_EXECUTOR = new JavascriptExecutor() {
		@Override
		public Object executeScript(String script, Object... args) {
			return null;
		}

		@Override
		public Object executeAsyncScript(String script, Object... args) {
			return null;
		}
	};
	public static final JavascriptExecutor UNSUPPORTED_JS_EXECUTOR = new JavascriptExecutor() {
		@Override
		public Object executeScript(String script, Object... args) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object executeAsyncScript(String script, Object... args) {
			throw new UnsupportedOperationException();
		}
	};

	private WebDriverUtil() {}

	public static JavascriptExecutor nullJs() {
		return NULL_JS_EXECUTOR;
	}

	public static JavascriptExecutor unsupportedJs() {
		return UNSUPPORTED_JS_EXECUTOR;
	}

}
