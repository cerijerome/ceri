package ceri.common.game;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import ceri.common.util.BasicUtil;

/**
 * Cheating app for Cookie Clicker http://orteil.dashnet.org/cookieclicker/
 * Clicks the mouse when within a given rectangle.
 * Makes reminder sound to look for lucky cookie.
 */
public class CookieClicker {
	private final Rectangle clickArea;
	private final int gcReminderMs;
	private final Rectangle gcResetArea;
	private final int delayMs;

	public static class Builder {
		Rectangle clickArea = new Rectangle(150, 400, 200, 200);
		int gcReminderMs = 130000;
		Rectangle gcResetArea = new Rectangle(0, 0, 20, 20);
		int delayMs = 10;

		Builder() {}

		public Builder clickArea(int x, int y, int w, int h) {
			clickArea = new Rectangle(x, y, w, h);
			return this;
		}

		public Builder gcReminderMs(int gcReminderMs) {
			this.gcReminderMs = gcReminderMs;
			return this;
		}

		public Builder gcResetArea(int x, int y, int w, int h) {
			gcResetArea = new Rectangle(x, y, w, h);
			return this;
		}

		public Builder delayMs(int delayMs) {
			this.delayMs = delayMs;
			return this;
		}

		public CookieClicker build() {
			return new CookieClicker(this);
		}
	}

	CookieClicker(Builder builder) {
		clickArea = new Rectangle(builder.clickArea);
		gcReminderMs = builder.gcReminderMs;
		gcResetArea = new Rectangle(builder.gcResetArea);
		delayMs = builder.delayMs;
	}

	public static Builder builder() {
		return new Builder();
	}

	public void start() throws AWTException {
		Robot r = new Robot();
		//r.setAutoWaitForIdle(true);
		long gc = 0;
		while (true) {
			Point p = MouseInfo.getPointerInfo().getLocation();
			if (clickArea.contains(p)) {
				r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			} else if (gcResetArea.contains(p)) {
				gc = System.currentTimeMillis();
			}
			long t = System.currentTimeMillis();
			if (t > gc + gcReminderMs) {
				gc = t;
				BasicUtil.beep();
			}
			r.delay(delayMs);
		}
	}

	public static void main(String[] args) throws Exception {
		builder().build().start();
	}

}
