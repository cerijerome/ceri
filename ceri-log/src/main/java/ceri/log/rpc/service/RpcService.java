package ceri.log.rpc.service;

import java.io.IOException;
import java.util.function.Supplier;
import ceri.common.function.Functions;
import ceri.common.util.Capability;
import ceri.log.util.LogUtil;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

/**
 * Extends a BindableService to be AutoCloseable, and to have a name.
 */
public interface RpcService extends BindableService, Capability.Name, AutoCloseable {
	/** A stateless, no-op instance. */
	static RpcService NULL = new Null() {};

	@SuppressWarnings("resource")
	static Container start(Supplier<? extends RpcService> serviceSupplier, RpcServer.Config config)
		throws IOException {
		return LogUtil.acceptOrClose(container(serviceSupplier, config), Container::start);
	}

	static Container container(Supplier<? extends RpcService> serviceSupplier,
		RpcServer.Config config) {
		return new Container(serviceSupplier, config);
	}

	/**
	 * A container for an optional service and a server.
	 */
	class Container implements Functions.Closeable, Capability.Enabled {
		private final RpcService service;
		private final RpcServer server;

		@SuppressWarnings("resource")
		private Container(Supplier<? extends RpcService> serviceSupplier, RpcServer.Config config) {
			try {
				this.service = config.enabled() ? serviceSupplier.get() : RpcService.NULL;
				this.server = RpcServer.of(service, config);
			} catch (Exception e) {
				close();
				throw e;
			}
		}

		public void start() throws IOException {
			server.start();
		}

		public int port() {
			return server.port();
		}

		@Override
		public boolean enabled() {
			return server.enabled();
		}

		@Override
		public void close() {
			LogUtil.close(server, service);
		}

		@Override
		public String toString() {
			return service.name() + ":" + server.port();
		}
	}

	/**
	 * A stateless, no-op implementation.
	 */
	interface Null extends RpcService {
		@Override
		default String name() {
			return "null-service";
		}

		@Override
		default ServerServiceDefinition bindService() {
			return RpcServiceUtil.NULL_SERVICE_DEFINITION;
		}

		@Override
		default void close() throws IOException {}
	}
}
