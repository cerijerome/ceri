package ceri.common.game;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;

/**
 * Cheating app for Cookie Clicker http://orteil.dashnet.org/cookieclicker/ Clicks the mouse when
 * within a given rectangle. Makes reminder sound to look for lucky cookie.
 */
public class CookieClicker {
	private final Rectangle clickArea;
	private final int gcReminderMs;
	private final Rectangle resetArea;
	private final Rectangle disableArea;
	private final Rectangle exitArea;
	private final int delayMs;
	private final Dimension screenSize;

	public static void main(String[] args) throws Exception {
		builder().build().start();
	}

	public static class Builder {
		Rectangle clickArea = new Rectangle(110, 340, 200, 200);
		int gcReminderMs = 130000;
		Rectangle resetArea = new Rectangle(1420, 880, 20, 20);
		Rectangle disableArea = new Rectangle(0, 880, 20, 20);
		Rectangle exitArea = new Rectangle(1420, 0, 20, 20);
		int delayMs = 50;

		Builder() {}

		public Builder clickArea(int x, int y, int w, int h) {
			clickArea = new Rectangle(x, y, w, h);
			return this;
		}

		public Builder gcReminderMs(int gcReminderMs) {
			this.gcReminderMs = gcReminderMs;
			return this;
		}

		public Builder resetArea(int x, int y, int w, int h) {
			resetArea = new Rectangle(x, y, w, h);
			return this;
		}

		public Builder disableArea(int x, int y, int w, int h) {
			disableArea = new Rectangle(x, y, w, h);
			return this;
		}

		public Builder exitArea(int x, int y, int w, int h) {
			exitArea = new Rectangle(x, y, w, h);
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
		screenSize = screenSize();
		clickArea = rectangle(builder.clickArea);
		gcReminderMs = builder.gcReminderMs;
		resetArea = rectangle(builder.resetArea);
		disableArea = rectangle(builder.disableArea);
		exitArea = rectangle(builder.exitArea);
		delayMs = builder.delayMs;
	}

	private Rectangle rectangle(Rectangle r) {
		int x = r.x >= 0 ? r.x : screenSize.width + r.x;
		int y = r.y >= 0 ? r.y : screenSize.height + r.y;
		return new Rectangle(x, y, r.width, r.height);
	}

	public static Builder builder() {
		return new Builder();
	}

	public void start() throws AWTException {
		Robot r = new Robot();
		// r.setAutoWaitForIdle(true);
		long gc = 0;
		boolean enabled = true;
		while (true) {
			r.delay(delayMs);
			Point p = mousePosition();
			if (p == null) continue;
			if (enabled && clickArea.contains(p)) {
				r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			} else if (resetArea.contains(p)) {
				gc = System.currentTimeMillis();
				enabled = changeState(enabled, true);
			} else if (disableArea.contains(p)) {
				enabled = changeState(enabled, false);
			} else if (exitArea.contains(p)) {
				beep();
				break;
			}
			long t = System.currentTimeMillis();
			if (enabled && t > gc + gcReminderMs) {
				gc = t;
				beep();
			}
		}
	}

	private Point mousePosition() {
		PointerInfo info = MouseInfo.getPointerInfo();
		if (info == null) return null;
		return info.getLocation();
	}

	private boolean changeState(boolean from, boolean to) {
		if (from != to) beep();
		return to;
	}

	private static Dimension screenSize() {
		return MouseInfo.getPointerInfo().getDevice().getDefaultConfiguration().getBounds()
			.getSize();
	}

	private static void beep() {
		Toolkit.getDefaultToolkit().beep();
	}
}
