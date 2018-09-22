package ceri.ent.json.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class JsonTestUtil {
	private static final JsonParser parser = new JsonParser();
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private JsonTestUtil() {}

	public static String pretty(String json) {
		return gson.toJson(parser.parse(json));
	}

}
