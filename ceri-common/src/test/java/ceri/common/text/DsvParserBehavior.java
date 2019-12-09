package ceri.common.text;

import static ceri.common.test.TestUtil.assertIterable;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import ceri.common.io.ResourcePath;

public class DsvParserBehavior {
	private DsvParser parser;
	private Iterator<String> lines;

	@Before
	public void before() throws IOException {
		parser = DsvParser.of(DsvCodec.CSV);
		lines = lines("dsv-parser-test.csv");
		next();
		assertFalse(parser.hasHeader());
		parser.applyHeader();
		assertTrue(parser.hasHeader());
		next();
	}

	@Test
	public void testSplit() {
		assertIterable(DsvParser.split(null, '|'));
		assertIterable(DsvParser.split("abc||de|f|", '|'), "abc", "", "de", "f", "");
	}

	@Test
	public void shouldReadFieldsByName() {
		assertTrue(parser.hasHeaderValue("field1"));
		assertFalse(parser.hasHeaderValue("field4"));
		assertIterable(parser.header(), "field1", "field2", "field3");
		assertTrue(parser.hasFields());
		assertThat(parser.field("field1"), is("this"));
		assertThat(parser.field("field2"), is("is"));
		assertThat(parser.field("field3", "not"), is("a"));
		assertThat(parser.field("field4", "test"), is("test"));
		next();
		assertThat(parser.field("field1"), is("test"));
		assertNull(parser.field("field2"));
		assertNull(parser.field("field3"));
		next();
		assertFalse(parser.hasFields());
	}

	@Test
	public void shouldReadFieldsByIndex() {
		skip(2);
		assertNull(parser.field(-1));
		assertNull(parser.field(0));
		assertNull(parser.field(1));
		next();
		assertNull(parser.field(-1));
		assertThat(parser.field(0), is("1"));
		assertThat(parser.field(1), is("2"));
		assertThat(parser.field(2), is("3"));
		assertNull(parser.field(3));
	}

	@Test
	public void shouldParseTypedFields() {
		skip(3);
		assertThat(parser.intField("field1"), is(1));
		assertThat(parser.intField("field2"), is(2));
		assertThat(parser.intField("field3"), is(3));
		assertThat(parser.intField("field4", 4), is(4));
		next();
		assertThat(parser.doubleField("field1"), is(1.0));
		assertThat(parser.doubleField("field2", 2.0), is(2.0));
		assertThat(parser.doubleField("field3", 3.0), is(2.0));
		next();
		assertThat(parser.booleanField("field1", true), is(true));
		assertThat(parser.booleanField("field2"), is(true));
		assertThat(parser.booleanField("field3", true), is(false));
		next();
		assertThat(parser.longField("field1", Long.MIN_VALUE), is(1000000000000000000L));
		assertThat(parser.longField("field2"), is(-1L));
		assertThat(parser.longField("field3", Long.MAX_VALUE), is(Long.MAX_VALUE));
	}

	private void next() {
		parser.parseLine(lines.next());
	}

	private void skip(int count) {
		while (count-- > 0)
			next();
	}

	private Iterator<String> lines(String resource) throws IOException {
		try (ResourcePath rp = ResourcePath.of(getClass(), resource)) {
			try (Stream<String> stream = Files.lines(rp.path())) {
				return stream.collect(Collectors.toList()).iterator();
			}
		}
	}

}
