package ceri.common.text;

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
		Assert.equal(Dsv.codec('\t'), Dsv.Codec.TSV);
		Assert.equal(Dsv.codec(','), Dsv.Codec.CSV);
		Dsv.Codec psv = Dsv.codec('|');
		Assert.equal(psv.encodeLine("a", "b", "c"), "a|b|c");
	}

	@Test
	public void shouldEncodeTsv() {
		Assert.isNull(TSV.encodeValue(null));
		Assert.equal(TSV.encodeValue(""), "");
		Assert.equal(TSV.encodeValue("\t"), "\"\t\"");
	}

	@Test
	public void shouldEncodeDocument() {
		Assert.isNull(CSV.encode((String[][]) null));
		Assert.isNull(CSV.encode((List<List<String>>) null));
		Assert.equal(CSV.encode(new String[][] {}), "");
		Assert.equal(CSV.encode(new String[][] { {} }), "");
		Assert.equal(CSV.encode(new String[][] { {}, {} }), "\r\n");
		Assert.equal(CSV.encode(Arrays.asList(Arrays.asList(",", ""), Arrays.asList("\""))),
			"\",\",\r\n\"\"");
	}

	@Test
	public void shouldEncodeLines() {
		Assert.isNull(CSV.encodeLine((List<String>) null));
		Assert.isNull(CSV.encodeLine((String[]) null));
		Assert.equal(CSV.encodeLine((String) null), "");
		Assert.equal(CSV.encodeLine(""), "");
		Assert.equal(CSV.encodeLine("", ""), ",");
		Assert.equal(CSV.encodeLine("", "", ""), ",,");
		Assert.equal(CSV.encodeLine(" "), " ");
		Assert.equal(CSV.encodeLine(" ", " "), " , ");
		Assert.equal(CSV.encodeLine(" ", " ", ""), " , ,");
		Assert.equal(CSV.encodeLine("\"", ",", ",\"", "\",\""),
			"\"\",\",\",\",\"\"\",\"\"\",\"\"\"");
	}

	@Test
	public void shouldEncodeValues() {
		Assert.isNull(CSV.encodeValue(null));
		Assert.equal(CSV.encodeValue(""), "");
		Assert.equal(CSV.encodeValue(" "), " ");
		Assert.equal(CSV.encodeValue("\""), "\"\"");
		Assert.equal(CSV.encodeValue("\"\""), "\"\"\"\"");
		Assert.equal(CSV.encodeValue(","), "\",\"");
		Assert.equal(CSV.encodeValue(" , "), "\" , \"");
		Assert.equal(CSV.encodeValue("\",\""), "\"\"\",\"\"\"");
	}

	@Test
	public void shouldDecodeDocument() {
		Assert.isNull(CSV.decode(null));
		Assert.ordered(CSV.decode(""));
		Assert.ordered(CSV.decode(" "), asList(""));
		Assert.ordered(CSV.decode(","), asList("", ""));
		Assert.ordered(CSV.decode(",,\r\n\",\"\n\n"), asList("", "", ""), asList(","));
		Assert.ordered(CSV.decode(",,\r\n\",\"\n\n "), asList("", "", ""), asList(","), asList(),
			asList(""));
	}

	@Test
	public void shouldDecodeLines() {
		Assert.isNull(CSV.decodeLine(null));
		Assert.ordered(CSV.decodeLine(""));
		Assert.ordered(CSV.decodeLine(" "), "");
		Assert.ordered(CSV.decodeLine(","), "", "");
		Assert.ordered(CSV.decodeLine(",,"), "", "", "");
		Assert.ordered(CSV.decodeLine(" , ,"), "", "", "");
		Assert.ordered(CSV.decodeLine(", \",\"\" ,\",\" , \""), "", ",\" ,", " , ");
	}

	@Test
	public void shouldDecodeValues() {
		Assert.isNull(Dsv.Codec.decodeValue(null));
		Assert.equal(Dsv.Codec.decodeValue(""), "");
		Assert.equal(Dsv.Codec.decodeValue(" "), " ");
		Assert.equal(Dsv.Codec.decodeValue("\""), "");
		Assert.equal(Dsv.Codec.decodeValue("\"\""), "");
		Assert.equal(Dsv.Codec.decodeValue("\"\"\""), "\"");
		Assert.equal(Dsv.Codec.decodeValue("\"\"\"\""), "\"\"");
		Assert.equal(Dsv.Codec.decodeValue("\"\"\"\"\""), "\"\"");
		Assert.equal(Dsv.Codec.decodeValue("\"\"\"\"\"\""), "\"\"");
		Assert.equal(Dsv.Codec.decodeValue("\"\"\"\"\"\"\""), "\"\"\"");
		Assert.equal(Dsv.Codec.decodeValue("\"\"\"\"\"\"\"\""), "\"\"\"\"");
		Assert.equal(Dsv.Codec.decodeValue(" \"\" "), "");
		Assert.equal(Dsv.Codec.decodeValue(" \",\" "), ",");
		Assert.equal(Dsv.Codec.decodeValue(" \"\",\" "), " \",\" ");
		Assert.equal(Dsv.Codec.decodeValue(","), ",");
		Assert.equal(Dsv.Codec.decodeValue(" , "), " , ");
		Assert.equal(Dsv.Codec.decodeValue("\t"), "\t");
	}

	@Test
	public void testSplit() {
		Assert.ordered(Dsv.split(null, '|'));
		Assert.ordered(Dsv.split("abc||de|f|", '|'), "abc", "", "de", "f", "");
	}

	@Test
	public void shouldReadFieldsByName() throws IOException {
		init();
		Assert.yes(parser.hasHeaderValue("field1"));
		Assert.no(parser.hasHeaderValue("field4"));
		Assert.ordered(parser.header(), "field1", "field2", "field3");
		Assert.yes(parser.hasFields());
		Assert.equal(parser.field("field1"), "this");
		Assert.equal(parser.field("field2"), "is");
		Assert.equal(parser.field("field3", "not"), "a");
		Assert.equal(parser.field("field4", "test"), "test");
		next();
		Assert.equal(parser.field("field1"), "test");
		Assert.isNull(parser.field("field2"));
		Assert.isNull(parser.field("field3"));
		next();
		Assert.no(parser.hasFields());
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
		Assert.equal(parser.field(0), "1");
		Assert.equal(parser.field(1), "2");
		Assert.equal(parser.field(2), "3");
		Assert.isNull(parser.field(3));
	}

	@Test
	public void shouldParseTypedFields() throws IOException {
		init();
		skip(3);
		Assert.equal(parser.parse("field1").toInt(), 1);
		Assert.equal(parser.parse("field2").toInt(), 2);
		Assert.equal(parser.parse("field3").toInt(), 3);
		next();
		Assert.equal(parser.parse(0).toDouble(), 1.0);
		Assert.equal(parser.parse(1).toDouble(), null);
		Assert.equal(parser.parse(2).toDouble(), 2.0);
		next();
		Assert.equal(parser.parse("field1").toBool(), null);
		Assert.equal(parser.parse("field2").toBool(), true);
		Assert.equal(parser.parse("field3").toBool(), false);
		next();
		Assert.equal(parser.parse(0).toLong(), 1000000000000000000L);
		Assert.equal(parser.parse(1).toLong(), -1L);
		Assert.equal(parser.parse(2).toLong(), null);
	}

	private void init() throws IOException {
		parser = Dsv.of(Dsv.Codec.CSV);
		lines = lines("dsv-parser-test.csv");
		next();
		Assert.no(parser.hasHeader());
		parser.applyHeader();
		Assert.yes(parser.hasHeader());
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
