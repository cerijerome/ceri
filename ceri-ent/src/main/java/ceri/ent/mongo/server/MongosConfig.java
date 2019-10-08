package ceri.ent.mongo.server;

import java.nio.file.Path;
import ceri.ent.mongo.HostGroup;
import ceri.ent.mongo.MongoUtil;
import ceri.log.process.Parameters;

public class MongosConfig {
	private static final String MONGOS = "mongos";
	private static final String PORT_OPTION = "--port";
	private static final String CONFIGDB_OPTION = "--configdb";
	public final String mongosPath;
	public final Integer port;
	public final HostGroup configDb;

	public static class Builder {
		String mongosPath = MONGOS;
		Integer port = null;
		HostGroup configDb = null;

		Builder() {}

		public Builder mongosPath(Path mongosPath) {
			return mongosPath(mongosPath.toString());
		}

		public Builder mongosPath(String mongosPath) {
			this.mongosPath = mongosPath;
			return this;
		}

		public Builder port(Integer port) {
			this.port = port;
			return this;
		}

		public Builder configDb(HostGroup configDb) {
			this.configDb = configDb;
			return this;
		}

		public MongosConfig build() {
			return new MongosConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	MongosConfig(Builder builder) {
		mongosPath = builder.mongosPath;
		port = builder.port;
		configDb = builder.configDb;
	}

	public int port() {
		return port == null ? MongoUtil.PORT : port;
	}

	public Parameters params() {
		Parameters params = Parameters.of(mongosPath);
		if (port != null) params.add(PORT_OPTION).add(port);
		if (configDb != null) params.add(CONFIGDB_OPTION).add(configDb.toString());
		return params;
	}

}
