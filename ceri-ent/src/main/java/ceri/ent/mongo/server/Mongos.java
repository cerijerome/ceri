package ceri.ent.mongo.server;

import static ceri.common.net.NetUtil.LOCALHOST;
import java.io.IOException;
import java.nio.file.Path;
import ceri.common.net.HostPort;
import ceri.ent.mongo.HostGroup;
import ceri.ent.mongo.MongoUtil;
import ceri.log.process.Parameters;

public class Mongos extends ProcessRunner {
	private static final String MONGOS = "mongos";
	private static final String PORT_OPTION = "--port";
	private static final String CONFIGDB_OPTION = "--configdb";
	private static final String LOG_PATH_OPTION = "--logpath";
	private static final String LOG_APPEND_OPTION = "--logappend";
	public final String mongosPath;
	public final HostGroup configDb;
	public final HostPort hostPort;
	public final Path logPath;
	public final boolean logAppend;

	public static Mongos start(HostGroup configDb) throws IOException {
		return builder().configDb(configDb).start();
	}

	public static class Builder {
		String mongosPath = MONGOS;
		Integer port = null;
		HostGroup configDb = null;
		Path logPath = null;
		boolean logAppend = false;

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

		public Builder logPath(String logPath) {
			return logPath(Path.of(logPath));
		}

		public Builder logPath(Path logPath) {
			this.logPath = logPath;
			return this;
		}

		public Builder logAppend(boolean logAppend) {
			this.logAppend = logAppend;
			return this;
		}

		public Parameters params() {
			Parameters params = Parameters.of(mongosPath);
			if (port != null) params.add(PORT_OPTION).add(port);
			if (configDb != null) params.add(CONFIGDB_OPTION).add(configDb.toString());
			if (logPath != null) params.add(LOG_PATH_OPTION, logPath.toString());
			if (logAppend) params.add(LOG_APPEND_OPTION);
			return params;
		}

		public Mongos start() throws IOException {
			return new Mongos(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Mongos(Builder builder) throws IOException {
		super(builder.params());
		hostPort = HostPort.of(LOCALHOST, builder.port);
		mongosPath = builder.mongosPath;
		configDb = builder.configDb;
		logPath = builder.logPath;
		logAppend = builder.logAppend;
	}

	public int port() {
		return hostPort.port(MongoUtil.PORT);
	}

	public String host() {
		return hostPort.toString();
	}

}
