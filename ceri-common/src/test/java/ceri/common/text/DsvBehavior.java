package ceri.common.text;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertOrdered;
import static ceri.common.test.Assert.assertTrue;
import static ceri.common.text.Dsv.Codec.CSV;
import static ceri.common.text.Dsv.Codec.TSV;
import static java.util.Arrays.asList;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Test;
import ceri.common.io.Resource;
import ceri.common.test.Assert;

public class DsvBehavior {
	private Dsv parser;
	private Iterator<String> lines;

	@After
	public void after() {
		parser = null;
		lines = null;
	}

	@Test
	public void shouldCreateCodecFromDelimiter() {
		assertEquals(Dsv.codec('\t'), Dsv.Codec.TSV);
		assertEquals(Dsv.codec(','), Dsv.Codec.CSV);
		Dsv.Codec psv = Dsv.codec('|');
		assertEquals(psv.encodeLine("a", "b", "c"), "a|b|c");
	}

	@Test
	public void shouldEncodeTsv() {
		Assert.isNull(TSV.encodeValue(null));
		assertEquals(TSV.encodeValue(""), "");
		assertEquals(TSV.encodeValue("\t"), "\"\t\"");
	}

	@Test
	public void shouldEncodeDocument() {
		Assert.isNull(CSV.encode((String[][]) null));
		Assert.isNull(CSV.encode((List<List<String>>) null));
		assertEquals(CSV.encode(new String[][] {}), "");
		assertEquals(CSV.encode(new String[][] { {} }), "");
		assertEquals(CSV.encode(new String[][] { {}, {} }), "\r\n");
		assertEquals(CSV.encode(Arrays.asList(Arrays.asList(",", ""), Arrays.asList("\""))),
			"\",\",\r\n\"\"");
	}

	@Test
	public void shouldEncodeLines() {
		Assert.isNull(CSV.encodeLine((List<String>) null));
		Assert.isNull(CSV.encodeLine((String[]) null));
		assertEquals(CSV.encodeLine((String) null), "");
		assertEquals(CSV.encodeLine(""), "");
		assertEquals(CSV.encodeLine("", ""), ",");
		assertEquals(CSV.encodeLine("", "", ""), ",,");
		assertEquals(CSV.encodeLine(" "), " ");
		assertEquals(CSV.encodeLine(" ", " "), " , ");
		assertEquals(CSV.encodeLine(" ", " ", ""), " , ,");
		assertEquals(CSV.encodeLine("\"", ",", ",\"", "\",\""),
			"\"\",\",\",\",\"\"\",\"\"\",\"\"\"");
	}

	@Test
	public void shouldEncodeValues() {
		Assert.isNull(CSV.encodeValue(null));
		assertEquals(CSV.encodeValue(""), "");
		assertEquals(CSV.encodeValue(" "), " ");
		assertEquals(CSV.encodeValue("\""), "\"\"");
		assertEquals(CSV.encodeValue("\"\""), "\"\"\"\"");
		assertEquals(CSV.encodeValue(","), "\",\"");
		assertEquals(CSV.encodeValue(" , "), "\" , \"");
		assertEquals(CSV.encodeValue("\",\""), "\"\"\",\"\"\"");
	}

	@Test
	public void shouldDecodeDocument() {
		Assert.isNull(CSV.decode(null));
		assertOrdered(CSV.decode(""));
		assertOrdered(CSV.decode(" "), asList(""));
		assertOrdered(CSV.decode(","), asList("", ""));
		assertOrdered(CSV.decode(",,\r\n\",\"\n\n"), asList("", "", ""), asList(","));
		assertOrdered(CSV.decode(",,\r\n\",\"\n\n "), asList("", "", ""), asList(","), asList(),
			asList(""));
	}

	@Test
	public void shouldDecodeLines() {
		Assert.isNull(CSV.decodeLine(null));
		assertOrdered(CSV.decodeLine(""));
		assertOrdered(CSV.decodeLine(" "), "");
		assertOrdered(CSV.decodeLine(","), "", "");
		assertOrdered(CSV.decodeLine(",,"), "", "", "");
		assertOrdered(CSV.decodeLine(" , ,"), "", "", "");
		assertOrdered(CSV.decodeLine(", \",\"\" ,\",\" , \""), "", ",\" ,", " , ");
	}

	@Test
	public void shouldDecodeValues() {
		Assert.isNull(Dsv.Codec.decodeValue(null));
		assertEquals(Dsv.Codec.decodeValue(""), "");
		assertEquals(Dsv.Codec.decodeValue(" "), " ");
		assertEquals(Dsv.Codec.decodeValue("\""), "");
		assertEquals(Dsv.Codec.decodeValue("\"\""), "");
		assertEquals(Dsv.Codec.decodeValue("\"\"\""), "\"");
		assertEquals(Dsv.Codec.decodeValue("\"\"\"\""), "\"\"");
		assertEquals(Dsv.Codec.decodeValue("\"\"\"\"\""), "\"\"");
		assertEquals(Dsv.Codec.decodeValue("\"\"\"\"\"\""), "\"\"");
		assertEquals(Dsv.Codec.decodeValue("\"\"\"\"\"\"\""), "\"\"\"");
		assertEquals(Dsv.Codec.decodeValue("\"\"\"\"\"\"\"\""), "\"\"\"\"");
		assertEquals(Dsv.Codec.decodeValue(" \"\" "), "");
		assertEquals(Dsv.Codec.decodeValue(" \",\" "), ",");
		assertEquals(Dsv.Codec.decodeValue(" \"\",\" "), " \",\" ");
		assertEquals(Dsv.Codec.decodeValue(","), ",");
		assertEquals(Dsv.Codec.decodeValue(" , "), " , ");
		assertEquals(Dsv.Codec.decodeValue("\t"), "\t");
	}

	@Test
	public void testSplit() {
		assertOrdered(Dsv.split(null, '|'));
		assertOrdered(Dsv.split("abc||de|f|", '|'), "abc", "", "de", "f", "");
	}

	@Test
	public void shouldReadFieldsByName() throws IOException {
		init();
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
		Assert.isNull(parser.field("field2"));
		Assert.isNull(parser.field("field3"));
		next();
		assertFalse(parser.hasFields());
	}

	@Test
	public void shouldReadFieldsByIndex() throws IOException {
		init();
		skip(2);
		Assert.isNull(parser.field(-1));
		Assert.isNull(parser.field(0));
		Assert.isNull(parser.field(1));
		next();
		Assert.isNull(parser.field(-1));
		assertEquals(parser.field(0), "1");
		assertEquals(parser.field(1), "2");
		assertEquals(parser.field(2), "3");
		Assert.isNull(parser.field(3));
	}

	@Test
	public void shouldParseTypedFields() throws IOException {
		init();
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

	private void init() throws IOException {
		parser = Dsv.of(Dsv.Codec.CSV);
		lines = lines("dsv-parser-test.csv");
		next();
		assertFalse(parser.hasHeader());
		parser.applyHeader();
		assertTrue(parser.hasHeader());
		next();
	}

	private void next() {
		parser.parseLine(lines.next());
	}

	private void skip(int count) {
		while (count-- > 0)
			next();
	}

	private Iterator<String> lines(String resource) throws IOException {
		try (var r = Resource.of(getClass(), resource); var stream = Files.lines(r.path())) {
			return stream.collect(Collectors.toList()).iterator();
		}
	}
}
