package ceri.log.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.io.Connector;
import ceri.common.io.ReplaceableInputStream;
import ceri.common.io.ReplaceableOutputStream;

/**
 * The base logic for self-healing connectors. It will automatically reconnect if the connector is
 * fatally broken, as determined by the config broken predicate.
 */
public abstract class SelfHealingConnector<T extends Connector> extends SelfHealing<T>
	implements Connector.Fixable {
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();

	protected SelfHealingConnector(Config config) {
		super(config);
		in.errors().listen(this::checkIfBroken);
		out.errors().listen(this::checkIfBroken);
	}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	protected abstract T openConnector() throws IOException;

	@SuppressWarnings("resource")
	@Override
	protected T openDevice() throws IOException {
		T connector = openConnector();
		in.set(connector.in());
		out.set(connector.out());
		return connector;
	}

}
