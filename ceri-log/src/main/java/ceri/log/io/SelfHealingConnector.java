package ceri.log.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.io.Connector;
import ceri.common.io.ReplaceableStream;

/**
 * The base logic for self-healing connectors. It will automatically reconnect if the connector is
 * fatally broken, as determined by the config broken predicate.
 */
public abstract class SelfHealingConnector<T extends Connector> extends SelfHealing<T>
	implements Connector.Fixable {
	private final ReplaceableStream.In in = ReplaceableStream.in();
	private final ReplaceableStream.Out out = ReplaceableStream.out();

	protected SelfHealingConnector(SelfHealing.Config config) {
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
