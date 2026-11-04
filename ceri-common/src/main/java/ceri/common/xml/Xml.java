package ceri.common.xml;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import ceri.common.except.ExceptionAdapter;
import ceri.common.text.StringBuilders;

public class Xml {

	private Xml() {}

	/**
	 * XML dom support.
	 */
	public static class Dom {
		private static final String FEATURES = "http://apache.org/xml/features/";
		public static final String LOAD_DTD_GRAMMAR = FEATURES + "nonvalidating/load-dtd-grammar";
		public static final String LOAD_EXTERNAL_DTD = FEATURES + "nonvalidating/load-external-dtd";

		private Dom() {}

		public static InputSource input(String s) {
			return new InputSource(new StringReader(s));
		}

		/**
		 * Creates an xml document object.
		 */
		public static Document document(String s) {
			return ExceptionAdapter.illegalArg.get(() -> {
				var dbf = DocumentBuilderFactory.newInstance();
				var builder = dbf.newDocumentBuilder();
				return builder.parse(input(s));
			});
		}

		/**
		 * Creates an xml document object that doesn't validate DTDs.
		 */
		public static Document unvalidatedDocument(String s) {
			return ExceptionAdapter.illegalArg.get(() -> {
				var dbf = DocumentBuilderFactory.newInstance();
				dbf.setValidating(false);
				dbf.setNamespaceAware(true);
				dbf.setFeature(LOAD_DTD_GRAMMAR, false);
				dbf.setFeature(LOAD_EXTERNAL_DTD, false);
				var builder = dbf.newDocumentBuilder();
				return builder.parse(input(s));
			});
		}
	}

	/**
	 * Simple encoding of chars to xml entities.
	 */
	public static String encode(String s) {
		if (s == null) return null;
		var state = StringBuilders.State.of(s);
		for (int i = 0; i < state.length(); i++) {
			char c = state.at(i);
			switch (c) {
				case '&' -> state.append("&amp;");
				case '<' -> state.append("&lt;");
				case '>' -> state.append("&gt;");
				case '\'' -> state.append("&apos;");
				case '"' -> state.append("&quot;");
				case ' ', '\t' -> state.append(c);
				default -> state.append(Character.isISOControl(c) ? '?' : c);
			}
		}
		return state.toString();
	}
}
