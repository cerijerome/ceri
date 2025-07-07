package ceri.common.test;

import java.io.IOException;
import ceri.common.event.Listenable;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.io.Fixable;
import ceri.common.io.StateChange;
import ceri.common.text.ToString;

/**
 * A connector implementation for tests, using piped streams.
 */
public class TestFixable implements Fixable {
	public final TestListeners<StateChange> listeners = TestListeners.of();
	public final CallSync.Consumer<Boolean> open = CallSync.consumer(false, true);
	public final CallSync.Consumer<Boolean> broken = CallSync.consumer(false, true);
	public final CallSync.Runnable close = CallSync.runnable(true);
	private final String name;

	/**
	 * Create a new instance with default name.
	 */
	public static TestFixable of() {
		return new TestFixable(null);
	}

	/**
	 * Create a new open instance with default name.
	 */
	public static TestFixable ofOpen() throws IOException {
		var fixable = of();
		fixable.open();
		return fixable;
	}

	/**
	 * Constructor with optional name override. Use null for the default name.
	 */
	protected TestFixable(String name) {
		this.name = name;
	}

	/**
	 * Clear state.
	 */
	public void reset() {
		listeners.clear();
		CallSync.resetAll(broken, open, close);
	}

	@Override
	public String name() {
		return name == null ? Fixable.super.name() : name;
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public void broken() {
		if (!broken.value()) listeners.accept(StateChange.broken);
		broken.accept(true);
		open.value(false);
	}

	/**
	 * Manually mark the connector as fixed.
	 */
	public void fixed() {
		open.value(true); // don't signal call
		if (broken.value()) listeners.accept(StateChange.fixed);
		broken.accept(false);
	}

	@Override
	public void open() throws IOException {
		open.accept(true, ExceptionAdapter.io);
		verifyUnbroken();
	}

	@Override
	public void close() throws IOException {
		open.value(false);
		close.run(ExceptionAdapter.io);
	}

	/**
	 * Prints state; useful for debugging tests.
	 */
	@Override
	public String toString() {
		return asString().toString();
	}

	protected ToString asString() {
		return ToString.ofName(name(), listeners.size(), broken.value() ? "broken" : "fixed",
			open.value() ? "open" : "closed");
	}

	protected void verifyConnected() throws IOException {
		verifyUnbroken();
		if (!open.value()) throw new IOException("Not connected");
	}

	protected void verifyUnbroken() throws IOException {
		if (broken.value()) throw new IOException("Connector is broken");
	}
}
