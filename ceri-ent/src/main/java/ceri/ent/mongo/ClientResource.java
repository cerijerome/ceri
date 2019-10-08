package ceri.ent.mongo;

import static ceri.ent.mongo.MongoUtil.document;
import java.io.Closeable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import ceri.common.net.HostPort;
import ceri.common.net.NetUtil;
import ceri.log.util.LogUtil;

public class ClientResource implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final String TEST_DB = "test";
	private static final String ADMIN_DB = "admin";
	public final MongoClient client;

	public static ClientResource hosts(HostPort... hosts) {
		return hosts(HostList.of(hosts));
	}

	public static ClientResource hosts(HostList hosts) {
		return of(MongoUtil.connection(hosts));
	}

	public static ClientResource of(String connection) {
		logger.info("Connecting to {}", connection);
		return wrap(MongoClients.create(connection));
	}

	public static ClientResource of() {
		logger.info("Connecting to {}:{}", MongoUtil.MONGODB_PROTOCOL, NetUtil.LOCALHOST);
		return wrap(MongoClients.create());
	}

	public static ClientResource wrap(MongoClient client) {
		return new ClientResource(client);
	}

	private ClientResource(MongoClient client) {
		this.client = client;
	}

	public MongoDatabase db() {
		return db(TEST_DB);
	}

	public MongoDatabase adminDb() {
		return db(ADMIN_DB);
	}

	public MongoDatabase db(String name) {
		return client.getDatabase(name);
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
		LogUtil.close(logger, client, MongoClient::close);
	}
}
