package ceri.common.text;

import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.text.DsvCodec.CSV;
import static ceri.common.text.DsvCodec.TSV;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class DsvCodecBehavior {

	@Test
	public void shouldCreateCodecFromDelimiter() {
		assertThat(DsvCodec.of('\t'), is(DsvCodec.TSV));
		assertThat(DsvCodec.of(','), is(DsvCodec.CSV));
		DsvCodec psv = DsvCodec.of('|');
		assertThat(psv.encodeLine("a", "b", "c"), is("a|b|c"));
	}

	@Test
	public void shouldEncodeTsv() {
		assertNull(TSV.encodeValue(null));
		assertThat(TSV.encodeValue(""), is(""));
		assertThat(TSV.encodeValue("\t"), is("\"\t\""));
	}

	@Test
	public void shouldDecodeTsv() {
		assertNull(TSV.decodeValue(null));
		assertThat(TSV.decodeValue(""), is(""));
		assertThat(TSV.decodeValue("\t"), is("\t"));
	}

	@Test
	public void shouldEncodeDocument() {
		assertNull(CSV.encode((String[][]) null));
		assertNull(CSV.encode((List<List<String>>) null));
		assertThat(CSV.encode(new String[][] {}), is(""));
		assertThat(CSV.encode(new String[][] { {} }), is(""));
		assertThat(CSV.encode(new String[][] { {}, {} }), is("\r\n"));
		assertThat(CSV.encode(Arrays.asList(Arrays.asList(",", ""), Arrays.asList("\""))),
			is("\",\",\r\n\"\""));
	}

	@Test
	public void shouldEncodeLines() {
		assertNull(CSV.encodeLine((List<String>) null));
		assertNull(CSV.encodeLine((String[]) null));
		assertThat(CSV.encodeLine((String) null), is(""));
		assertThat(CSV.encodeLine(""), is(""));
		assertThat(CSV.encodeLine("", ""), is(","));
		assertThat(CSV.encodeLine("", "", ""), is(",,"));
		assertThat(CSV.encodeLine(" "), is(" "));
		assertThat(CSV.encodeLine(" ", " "), is(" , "));
		assertThat(CSV.encodeLine(" ", " ", ""), is(" , ,"));
		assertThat(CSV.encodeLine("\"", ",", ",\"", "\",\""),
			is("\"\",\",\",\",\"\"\",\"\"\",\"\"\""));
	}

	@Test
	public void shouldEncodeValues() {
		assertNull(CSV.encodeValue(null));
		assertThat(CSV.encodeValue(""), is(""));
		assertThat(CSV.encodeValue(" "), is(" "));
		assertThat(CSV.encodeValue("\""), is("\"\""));
		assertThat(CSV.encodeValue("\"\""), is("\"\"\"\""));
		assertThat(CSV.encodeValue(","), is("\",\""));
		assertThat(CSV.encodeValue(" , "), is("\" , \""));
		assertThat(CSV.encodeValue("\",\""), is("\"\"\",\"\"\""));
	}

	@Test
	public void shouldDecodeDocument() {
		assertNull(CSV.decode(null));
		assertIterable(CSV.decode(""));
		assertIterable(CSV.decode(" "), asList(""));
		assertIterable(CSV.decode(","), asList("", ""));
		assertIterable(CSV.decode(",,\r\n\",\"\n\n"), asList("", "", ""), asList(","));
		assertIterable(CSV.decode(",,\r\n\",\"\n\n "), asList("", "", ""), asList(","), asList(),
			asList(""));
	}

	@Test
	public void shouldDecodeLines() {
		assertNull(CSV.decodeLine(null));
		assertIterable(CSV.decodeLine(""));
		assertIterable(CSV.decodeLine(" "), "");
		assertIterable(CSV.decodeLine(","), "", "");
		assertIterable(CSV.decodeLine(",,"), "", "", "");
		assertIterable(CSV.decodeLine(" , ,"), "", "", "");
		assertIterable(CSV.decodeLine(", \",\"\" ,\",\" , \""), "", ",\" ,", " , ");
	}

	@Test
	public void shouldDecodeValues() {
		assertNull(CSV.decodeValue(null));
		assertThat(CSV.decodeValue(""), is(""));
		assertThat(CSV.decodeValue(" "), is(" "));
		assertThat(CSV.decodeValue("\""), is(""));
		assertThat(CSV.decodeValue("\"\""), is(""));
		assertThat(CSV.decodeValue("\"\"\""), is("\""));
		assertThat(CSV.decodeValue("\"\"\"\""), is("\"\""));
		assertThat(CSV.decodeValue("\"\"\"\"\""), is("\"\""));
		assertThat(CSV.decodeValue("\"\"\"\"\"\""), is("\"\""));
		assertThat(CSV.decodeValue("\"\"\"\"\"\"\""), is("\"\"\""));
		assertThat(CSV.decodeValue("\"\"\"\"\"\"\"\""), is("\"\"\"\""));
		assertThat(CSV.decodeValue(" \"\" "), is(""));
		assertThat(CSV.decodeValue(" \",\" "), is(","));
		assertThat(CSV.decodeValue(" \"\",\" "), is(" \",\" "));
		assertThat(CSV.decodeValue(","), is(","));
		assertThat(CSV.decodeValue(" , "), is(" , "));
	}

}
