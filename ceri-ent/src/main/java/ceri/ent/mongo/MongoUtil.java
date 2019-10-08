package ceri.ent.mongo;

import static ceri.common.function.FunctionUtil.nullConsumer;
import static com.mongodb.client.model.Aggregates.out;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

public class MongoUtil {
	public static final String MONGODB_PROTOCOL = "mongodb://";
	public static final int PORT = 27017;
	public static final String ID = "_id";
	public static final String SET_OPERATOR = "$set";
	private static final Pattern QUOTED_OR_WHITESPACE = Pattern.compile(
		"((?<!\\\\)\".*?(?<!\\\\)\"|\\s+)");
	private static final JsonWriterSettings PRETTY =
		JsonWriterSettings.builder().indent(true).build();

	private MongoUtil() {}

	public static String connection(String... hostPorts) {
		return connection(Arrays.asList(hostPorts));
	}

	public static String connection(Iterable<String> hostPorts) {
		return StringUtil.join(",", MONGODB_PROTOCOL, "", hostPorts);
	}

	public static String connection(HostList hosts) {
		return MONGODB_PROTOCOL + hosts;
	}

	public static CodecRegistry codec(Class<?>... classes) {
		return fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
			fromProviders(PojoCodecProvider.builder().register(classes).build()));
	}

	public static void complete(Iterable<?> iterable) {
		iterable.forEach(nullConsumer());
	}

	public static void copyCollection(MongoCollection<Document> collection, String name) {
		// unlike shell, $out needs forEach to copy to the output collection
		complete(collection.aggregate(asList(out(name))));
	}

	public static String pretty(Document document) {
		return document.toJson(PRETTY);
	}

	public static String compact(Document document) {
		return compact(document.toJson());
	}

	public static String compact(String json) {
		if (json == null) return null;
		return RegexUtil.replaceAll(QUOTED_OR_WHITESPACE, json, result ->
			json.charAt(result.start()) == '\"' ? null : ""); // skip quoted matches
	}

	public static <T extends Document> Consumer<ChangeStreamDocument<T>> forChangeStream(
		Consumer<T> consumer) {
		return c -> consumer.accept(c.getFullDocument());
	}

	public static Object id(Document document) {
		if (document == null) return null;
		return document.get(ID);
	}

	public static Bson eqId(Document document) {
		return Filters.eq(id(document));
	}

	public static Bson eqId(String hexVal) {
		return Filters.eq(new ObjectId(hexVal));
	}

	public static UpdateOptions updateUpsert() {
		return new UpdateOptions().upsert(true);
	}

	public static ReplaceOptions replaceUpsert() {
		return new ReplaceOptions().upsert(true);
	}

	public static BulkWriteOptions bulkUnordered() {
		return new BulkWriteOptions().ordered(false);
	}

	public static Document set(Document document) {
		return document(SET_OPERATOR, document);
	}

	public static Document document(String k1, Object v1) {
		return toDocument(k1, v1);
	}

	public static Document document(String k1, Object v1, String k2, Object v2) {
		return toDocument(k1, v1, k2, v2);
	}

	public static Document document(String k1, Object v1, String k2, Object v2, String k3,
		Object v3) {
		return toDocument(k1, v1, k2, v2, k3, v3);
	}

	public static Document document(String k1, Object v1, String k2, Object v2, String k3,
		Object v3, String k4, Object v4) {
		return toDocument(k1, v1, k2, v2, k3, v3, k4, v4);
	}

	public static Document document(String k1, Object v1, String k2, Object v2, String k3,
		Object v3, String k4, Object v4, String k5, Object v5) {
		return toDocument(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
	}

	public static Document document(String k1, Object v1, String k2, Object v2, String k3,
		Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6) {
		return toDocument(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
	}

	public static Document document(String k1, Object v1, String k2, Object v2, String k3,
		Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6, String k7,
		Object v7) {
		return toDocument(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
	}

	public static Document document(String k1, Object v1, String k2, Object v2, String k3,
		Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6, String k7,
		Object v7, String k8, Object v8) {
		return toDocument(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
	}

	public static Document document(String k1, Object v1, String k2, Object v2, String k3,
		Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6, String k7,
		Object v7, String k8, Object v8, String k9, Object v9) {
		return toDocument(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
	}

	public static Document document(String k1, Object v1, String k2, Object v2, String k3,
		Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6, String k7,
		Object v7, String k8, Object v8, String k9, Object v9, String k10, Object v10) {
		return toDocument(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9,
			k10, v10);
	}

	private static Document toDocument(Object... keyVals) {
		Document document = new Document();
		for (int i = 0; i < keyVals.length - 1; i += 2)
			document.append((String) keyVals[i], keyVals[i + 1]);
		return document;
	}

}

