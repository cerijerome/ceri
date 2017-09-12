package ceri.ent.json;

import static ceri.common.test.TestUtil.resource;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtilTest {
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final Object OBJ1 = gson.fromJson(resource("ex1.json"), Object.class);
	private static final Object OBJ2 = gson.fromJson(resource("ex2.json"), Object.class);

	@Test
	public void testExtract() {
		assertThat(GsonUtil.extract(OBJ1, "glossary.title"), is("example glossary"));
		assertThat(GsonUtil.extract(OBJ1,
			"glossary.GlossDiv.GlossList.GlossEntry.GlossDef.GlossSeeAlso.1"), is("XML"));
		assertThat(GsonUtil.extract(OBJ2, "cars.2.models.0"), is("500"));
	}

}
