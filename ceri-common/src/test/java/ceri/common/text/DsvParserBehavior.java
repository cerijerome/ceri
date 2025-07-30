package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertTrue;
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
		assertOrdered(DsvParser.split(null, '|'));
		assertOrdered(DsvParser.split("abc||de|f|", '|'), "abc", "", "de", "f", "");
	}

	@Test
	public void shouldReadFieldsByName() {
		assertTrue(parser.hasHeaderValue("field1"));
		assertFalse(parser.hasHeaderValue("field4"));
		assertOrdered(parser.header(), "field1", "field2", "field3");
		assertTrue(parser.hasFields());
		assertEquals(parser.field("field1"), "this");
		assertEquals(parser.field("field2"), "is");
		assertEquals(parser.field("field3", "not"), "a");
		assertEquals(parser.field("field4", "test"), "test");
		next();
		assertEquals(parser.field("field1"), "test");
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
		assertEquals(parser.field(0), "1");
		assertEquals(parser.field(1), "2");
		assertEquals(parser.field(2), "3");
		assertNull(parser.field(3));
	}

	@Test
	public void shouldParseTypedFields() {
		skip(3);
		assertEquals(parser.parse("field1").toInt(), 1);
		assertEquals(parser.parse("field2").toInt(), 2);
		assertEquals(parser.parse("field3").toInt(), 3);
		next();
		assertEquals(parser.parse(0).toDouble(), 1.0);
		assertEquals(parser.parse(1).toDouble(), null);
		assertEquals(parser.parse(2).toDouble(), 2.0);
		next();
		assertEquals(parser.parse("field1").toBool(), null);
		assertEquals(parser.parse("field2").toBool(), true);
		assertEquals(parser.parse("field3").toBool(), false);
		next();
		assertEquals(parser.parse(0).toLong(), 1000000000000000000L);
		assertEquals(parser.parse(1).toLong(), -1L);
		assertEquals(parser.parse(2).toLong(), null);
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
