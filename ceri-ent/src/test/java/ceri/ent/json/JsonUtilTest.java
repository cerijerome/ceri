package ceri.ent.json;

import static ceri.common.test.Testing.resource;
import org.junit.Test;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ceri.common.test.Assert;

public class JsonUtilTest {
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final Object OBJ1 = gson.fromJson(resource("ex1.json"), Object.class);
	private static final Object OBJ2 = gson.fromJson(resource("ex2.json"), Object.class);

	@Test
	public void testExtract() {
		Assert.equal(JsonUtil.extract(OBJ1, "glossary.title"), "example glossary");
		Assert.equal(JsonUtil.extract(OBJ1,
			"glossary.GlossDiv.GlossList.GlossEntry.GlossDef.GlossSeeAlso.1"), "XML");
		Assert.equal(JsonUtil.extract(OBJ2, "cars.2.models.0"), "500");
		Assert.equal(JsonUtil.extractInt(OBJ2, "cars.2.models.0"), 500);
	}

}
