package ceri.ent.mongo.server;

import static ceri.common.net.NetUtil.LOCALHOST;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import ceri.common.collect.Immutable;
import ceri.common.net.HostPort;
import ceri.common.process.Parameters;
import ceri.ent.mongo.MongoUtil;

public class Mongod extends ProcessRunner {
	private static final String MONGOD = "mongod";
	private static final String DB_PATH_OPTION = "--dbpath";
	private static final String PORT_OPTION = "--port";
	private static final String REPL_SET_OPTION = "--replSet";
	private static final String CONFIG_SERVER_OPTION = "--configsvr";
	private static final String SHARD_SERVER_OPTION = "--shardsvr";
	private static final String LOG_PATH_OPTION = "--logpath";
	private static final String LOG_APPEND_OPTION = "--logappend";
	public final String mongodPath;
	public final Path dbPath;
	public final boolean configServer;
	public final boolean shardServer;
	public final String replSet;
	public final HostPort hostPort;
	public final Path logPath;
	public final boolean logAppend;

	public static class Group extends ProcessGroup {
		public final List<Mongod> mongods;

		public static Group of(List<Mongod> mongods) {
			return new Group(Immutable.list(mongods));
		}

		private Group(List<Mongod> mongods) {
			super(mongods);
			this.mongods = mongods;
		}

		public Mongod mongod(int index) {
			return mongods.get(index);
		}

		public Mongod first() {
			return isEmpty() ? null : mongods.get(0);
		}

		public Mongod last() {
			return isEmpty() ? null : mongods.get(count() - 1);
		}
	}

	public static Mongod start(String dbPath) throws IOException {
		return start(Path.of(dbPath));
	}

	public static Mongod start(Path dbPath) throws IOException {
		return builder().dbPath(dbPath).start();
	}

	public static class Builder {
		String mongodPath = MONGOD;
		Path dbPath = null;
		Integer port = null;
		String replSet = null;
		boolean configServer = false;
		boolean shardServer = false;
		Path logPath = null;
		boolean logAppend = false;

		Builder() {}

		public Builder mongodPath(Path mongodPath) {
			return mongodPath(mongodPath.toString());
		}

		public Builder mongodPath(String mongodPath) {
			this.mongodPath = mongodPath;
			return this;
		}

		public Builder dbPath(String dbPath) {
			return dbPath(Path.of(dbPath));
		}

		public Builder dbPath(Path dbPath) {
			this.dbPath = dbPath;
			return this;
		}

		public Builder port(Integer port) {
			this.port = port;
			return this;
		}

		public Builder replSet(String replSet) {
			this.replSet = replSet;
			return this;
		}

		public Builder configServer(boolean configServer) {
			this.configServer = configServer;
			return this;
		}

		public Builder shardServer(boolean shardServer) {
			this.shardServer = shardServer;
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
			Parameters params = Parameters.of(mongodPath);
			if (dbPath != null) params.add(DB_PATH_OPTION, dbPath);
			if (port != null) params.add(PORT_OPTION, port);
			if (replSet != null) params.add(REPL_SET_OPTION, replSet);
			if (configServer) params.add(CONFIG_SERVER_OPTION);
			if (shardServer) params.add(SHARD_SERVER_OPTION);
			if (logPath != null) params.add(LOG_PATH_OPTION, logPath);
			if (logAppend) params.add(LOG_APPEND_OPTION);
			return params;
		}

		public Mongod start() throws IOException {
			return new Mongod(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Mongod(Builder builder) throws IOException {
		super(builder.params());
		hostPort = HostPort.of(LOCALHOST, builder.port);
		mongodPath = builder.mongodPath;
		dbPath = builder.dbPath;
		replSet = builder.replSet;
		configServer = builder.configServer;
		shardServer = builder.shardServer;
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
