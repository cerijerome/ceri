package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.text.DsvCodec.CSV;
import static ceri.common.text.DsvCodec.TSV;
import static java.util.Arrays.asList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class DsvCodecBehavior {

	@Test
	public void shouldCreateCodecFromDelimiter() {
		assertEquals(DsvCodec.of('\t'), DsvCodec.TSV);
		assertEquals(DsvCodec.of(','), DsvCodec.CSV);
		DsvCodec psv = DsvCodec.of('|');
		assertEquals(psv.encodeLine("a", "b", "c"), "a|b|c");
	}

	@Test
	public void shouldEncodeTsv() {
		assertNull(TSV.encodeValue(null));
		assertEquals(TSV.encodeValue(""), "");
		assertEquals(TSV.encodeValue("\t"), "\"\t\"");
	}

	@Test
	public void shouldDecodeTsv() {
		assertNull(TSV.decodeValue(null));
		assertEquals(TSV.decodeValue(""), "");
		assertEquals(TSV.decodeValue("\t"), "\t");
	}

	@Test
	public void shouldEncodeDocument() {
		assertNull(CSV.encode((String[][]) null));
		assertNull(CSV.encode((List<List<String>>) null));
		assertEquals(CSV.encode(new String[][] {}), "");
		assertEquals(CSV.encode(new String[][] { {} }), "");
		assertEquals(CSV.encode(new String[][] { {}, {} }), "\r\n");
		assertEquals(CSV.encode(Arrays.asList(Arrays.asList(",", ""), Arrays.asList("\""))),
			"\",\",\r\n\"\"");
	}

	@Test
	public void shouldEncodeLines() {
		assertNull(CSV.encodeLine((List<String>) null));
		assertNull(CSV.encodeLine((String[]) null));
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
		assertNull(CSV.encodeValue(null));
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
		assertNull(CSV.decode(null));
		assertOrdered(CSV.decode(""));
		assertOrdered(CSV.decode(" "), asList(""));
		assertOrdered(CSV.decode(","), asList("", ""));
		assertOrdered(CSV.decode(",,\r\n\",\"\n\n"), asList("", "", ""), asList(","));
		assertOrdered(CSV.decode(",,\r\n\",\"\n\n "), asList("", "", ""), asList(","), asList(),
			asList(""));
	}

	@Test
	public void shouldDecodeLines() {
		assertNull(CSV.decodeLine(null));
		assertOrdered(CSV.decodeLine(""));
		assertOrdered(CSV.decodeLine(" "), "");
		assertOrdered(CSV.decodeLine(","), "", "");
		assertOrdered(CSV.decodeLine(",,"), "", "", "");
		assertOrdered(CSV.decodeLine(" , ,"), "", "", "");
		assertOrdered(CSV.decodeLine(", \",\"\" ,\",\" , \""), "", ",\" ,", " , ");
	}

	@Test
	public void shouldDecodeValues() {
		assertNull(CSV.decodeValue(null));
		assertEquals(CSV.decodeValue(""), "");
		assertEquals(CSV.decodeValue(" "), " ");
		assertEquals(CSV.decodeValue("\""), "");
		assertEquals(CSV.decodeValue("\"\""), "");
		assertEquals(CSV.decodeValue("\"\"\""), "\"");
		assertEquals(CSV.decodeValue("\"\"\"\""), "\"\"");
		assertEquals(CSV.decodeValue("\"\"\"\"\""), "\"\"");
		assertEquals(CSV.decodeValue("\"\"\"\"\"\""), "\"\"");
		assertEquals(CSV.decodeValue("\"\"\"\"\"\"\""), "\"\"\"");
		assertEquals(CSV.decodeValue("\"\"\"\"\"\"\"\""), "\"\"\"\"");
		assertEquals(CSV.decodeValue(" \"\" "), "");
		assertEquals(CSV.decodeValue(" \",\" "), ",");
		assertEquals(CSV.decodeValue(" \"\",\" "), " \",\" ");
		assertEquals(CSV.decodeValue(","), ",");
		assertEquals(CSV.decodeValue(" , "), " , ");
	}

}
