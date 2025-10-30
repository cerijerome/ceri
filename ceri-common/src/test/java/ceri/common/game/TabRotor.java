package ceri.common.game;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import ceri.common.io.Io;

/**
 * Tab rotator for Chrome and other apps. Uses Robot for opt+cmd+right to advance tabs.
 */
public class TabRotor {
	private final Rectangle resetArea;
	private final Rectangle disableArea;
	private final Rectangle exitArea;
	private final int pollMs;
	private final int adjustMs;
	private final int autoDelayMs;
	private final Dimension screenSize;
	private final int rotateDelayMs;
	private int currentDelayMs;

	public static class Builder {
		Rectangle resetArea = new Rectangle(0, 0, 20, 20);
		Rectangle disableArea = new Rectangle(0, -20, 20, 20);
		Rectangle exitArea = new Rectangle(-20, -20, 20, 20);
		int rotateDelayMs = 2000;
		int pollMs = 200;
		int adjustMs = 200;
		int autoDelayMs = 40;

		Builder() {}

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

		public Builder rotateDelayMs(int rotateDelayMs) {
			this.rotateDelayMs = rotateDelayMs;
			return this;
		}

		public Builder pollMs(int pollMs) {
			this.pollMs = pollMs;
			return this;
		}

		public Builder adjustMs(int adjustMs) {
			this.adjustMs = adjustMs;
			return this;
		}

		public Builder autoDelayMs(int autoDelayMs) {
			this.autoDelayMs = autoDelayMs;
			return this;
		}

		public TabRotor build() {
			return new TabRotor(this);
		}
	}

	TabRotor(Builder builder) {
		screenSize = screenSize();
		resetArea = rectangle(builder.resetArea);
		disableArea = rectangle(builder.disableArea);
		exitArea = rectangle(builder.exitArea);
		rotateDelayMs = builder.rotateDelayMs;
		pollMs = builder.pollMs;
		adjustMs = builder.adjustMs;
		autoDelayMs = builder.autoDelayMs;
		currentDelayMs = rotateDelayMs;
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
		r.setAutoWaitForIdle(true);
		boolean enabled = false;
		long t0 = System.currentTimeMillis();
		while (true) {
			r.delay(pollMs);
			Point p = mousePosition();
			if (p == null) continue;
			long t = System.currentTimeMillis();
			if (enabled) adjust();
			if (enabled && t > t0 + currentDelayMs) {
				next(r);
				t0 = t;
			} else if (resetArea.contains(p)) {
				enabled = changeState(enabled, true);
			} else if (disableArea.contains(p)) {
				enabled = changeState(enabled, false);
			} else if (exitArea.contains(p)) {
				beep();
				break;
			}
		}
	}

	private void adjust() {
		int delayMs = currentDelayMs;
		while (true) {
			char ch = Io.availableChar();
			if (ch == 0) break;
			if (ch == '>') currentDelayMs = Math.max(pollMs, currentDelayMs - adjustMs);
			if (ch == '<') currentDelayMs += adjustMs;
			if (ch == '.') currentDelayMs = rotateDelayMs;
		}
		if (delayMs != currentDelayMs) System.out.println("Delay = " + currentDelayMs + "ms");
	}

	private void next(Robot r) {
		try {
			r.keyPress(KeyEvent.VK_META); // mac command
			r.keyPress(KeyEvent.VK_ALT); // mac option
			r.keyPress(KeyEvent.VK_RIGHT); // right-arrow
			r.delay(autoDelayMs);
		} finally {
			r.keyRelease(KeyEvent.VK_RIGHT);
			r.keyRelease(KeyEvent.VK_ALT);
			r.keyRelease(KeyEvent.VK_META);
		}
	}

	private Point mousePosition() {
		PointerInfo info = MouseInfo.getPointerInfo();
		if (info == null) return null;
		return info.getLocation();
	}

	private boolean changeState(boolean from, boolean to) {
		if (from != to) {
			beep();
			System.out.println(to ? "Enabled" : "Disabled");
		}
		return to;
	}

	private static Dimension screenSize() {
		return MouseInfo.getPointerInfo().getDevice().getDefaultConfiguration().getBounds()
			.getSize();
	}

	public static void main(String[] args) throws Exception {
		builder().build().start();
	}

	private static void beep() {
		Toolkit.getDefaultToolkit().beep();
	}
}
