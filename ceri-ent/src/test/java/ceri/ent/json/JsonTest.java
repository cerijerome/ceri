package ceri.ent.json;

import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class JsonTest {
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final Object OBJ1 = gson.fromJson(Testing.resource("ex1.json"), Object.class);
	private static final Object OBJ2 = gson.fromJson(Testing.resource("ex2.json"), Object.class);

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Json.class);
		Assert.privateConstructor(Json.Paths.class);
	}

	@Test
	public void testPaths() {
		Assert.equal(Json.Paths.asObj(OBJ1, "glossary.title"), "example glossary");
		Assert.equal(Json.Paths.asObj(OBJ1,
			"glossary.GlossDiv.GlossList.GlossEntry.GlossDef.GlossSeeAlso.1"), "XML");
		Assert.equal(Json.Paths.asObj(OBJ2, "cars.2.models.0"), "500");
		Assert.equal(Json.Paths.asInt(OBJ2, "cars.2.models.0"), 500);
	}
}
