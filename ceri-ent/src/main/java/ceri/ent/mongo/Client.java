package ceri.ent.mongo;

import static ceri.ent.mongo.Mongo.document;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import ceri.common.function.Functions;
import ceri.common.net.HostPort;
import ceri.common.text.ToString;
import ceri.log.util.Logs;

public class Client implements Functions.Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final String TEST_DB = "test";
	private static final String ADMIN_DB = "admin";
	public final MongoClient mongo;

	public record Settings(Hosts hosts, Integer connectionTimeoutMs, Integer readTimeoutMs) {

		public static class Builder {
			final List<HostPort> hosts = new ArrayList<>();
			Integer connectionTimeoutMs;
			Integer readTimeoutMs;

			Builder() {}

			public Builder hostPorts(HostPort... hosts) {
				return hostPorts(Arrays.asList(hosts));
			}

			public Builder hostPorts(Collection<HostPort> hosts) {
				this.hosts.addAll(hosts);
				return this;
			}

			public Builder hosts(Hosts hosts) {
				return hostPorts(hosts.hosts());
			}

			public Builder hosts(String... hosts) {
				return hosts(Arrays.asList(hosts));
			}

			public Builder hosts(Collection<String> hosts) {
				hosts.forEach(host -> this.hosts.add(HostPort.parse(host)));
				return this;
			}

			public Builder connectionTimeoutMs(int connectionTimeoutMs) {
				this.connectionTimeoutMs = connectionTimeoutMs;
				return this;
			}

			public Builder readTimeoutMs(int readTimeoutMs) {
				this.readTimeoutMs = readTimeoutMs;
				return this;
			}

			public Hosts hostList() {
				return hosts.isEmpty() ? Hosts.LOCALHOST : Hosts.of(hosts);
			}

			public Settings build() {
				return new Settings(hostList(), connectionTimeoutMs, readTimeoutMs);
			}
		}

		public static Builder builder(String connection) {
			return builder().hosts(new ConnectionString(connection).getHosts());
		}

		public static Builder builder() {
			return new Builder();
		}

		public MongoClientSettings toMongo() {
			var b = MongoClientSettings.builder();
			b.applyConnectionString(new ConnectionString(Mongo.connection(hosts)));
			if (connectionTimeoutMs != null) b.applyToSocketSettings(
				s -> s.connectTimeout(connectionTimeoutMs, TimeUnit.MILLISECONDS));
			if (readTimeoutMs != null)
				b.applyToSocketSettings(s -> s.readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS));
			return b.build();
		}

		@Override
		public String toString() {
			return ToString.forClass(this, hosts, connectionTimeoutMs, readTimeoutMs);
		}
	}

	public static Settings settings(int timeoutMs) {
		return settings(HostPort.LOCALHOST, timeoutMs);
	}

	public static Settings settings(HostPort host, int timeoutMs) {
		return Settings.builder().hostPorts(host).connectionTimeoutMs(timeoutMs)
			.readTimeoutMs(timeoutMs).build();
	}

	public static Client hosts(HostPort... hosts) {
		return hosts(Hosts.of(hosts));
	}

	public static Client hosts(Hosts hosts) {
		return of(Mongo.connection(hosts));
	}

	public static Client of(Settings settings) {
		return of(settings.toMongo());
	}

	@SuppressWarnings("resource")
	public static Client of(MongoClientSettings settings) {
		logger.info("Connecting to {}", settings.getClusterSettings().getHosts());
		return wrap(MongoClients.create(settings));
	}

	@SuppressWarnings("resource")
	public static Client of(String connection) {
		logger.info("Connecting to {}", connection);
		return wrap(MongoClients.create(connection));
	}

	public static Client of() {
		return of(Mongo.LOCALHOST);
	}

	public static Client wrap(MongoClient client) {
		return new Client(client);
	}

	private Client(MongoClient client) {
		this.mongo = client;
	}

	public MongoDatabase db() {
		return db(TEST_DB);
	}

	public MongoDatabase adminDb() {
		return db(ADMIN_DB);
	}

	public MongoDatabase db(String name) {
		return mongo.getDatabase(name);
	}

	public Document runCommand(String command, String value) {
		return runCommand(document(command, value));
	}

	public Document runCommand(String command, Document document) {
		return runCommand(document(command, document));
	}

	public Document runCommand(Document document) {
		return adminDb().runCommand(document);
	}

	@Override
	public void close() {
		Logs.close(mongo, MongoClient::close);
	}
}
