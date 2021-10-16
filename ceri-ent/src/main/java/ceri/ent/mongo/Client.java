package ceri.ent.mongo;

import static ceri.ent.mongo.MongoUtil.document;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import ceri.common.function.RuntimeCloseable;
import ceri.common.net.HostPort;
import ceri.log.util.LogUtil;

public class Client implements RuntimeCloseable {
	private static final Logger logger = LogManager.getLogger();
	private static final String TEST_DB = "test";
	private static final String ADMIN_DB = "admin";
	public final MongoClient mongo;

	public static Client hosts(HostPort... hosts) {
		return hosts(HostList.of(hosts));
	}

	public static Client hosts(HostList hosts) {
		return of(MongoUtil.connection(hosts));
	}

	public static Client of(ClientSettings settings) {
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
		return of(MongoUtil.LOCALHOST);
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
		LogUtil.close(logger, mongo, MongoClient::close);
	}
}
