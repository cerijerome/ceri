package ceri.process.scutil.parser;

public interface ParserListener {

	@SuppressWarnings("unused")
	default void text(String text) {}

	@SuppressWarnings("unused")
	default void textGroup(String name, String group) {}

	@SuppressWarnings("unused")
	default void textValue(String text, String value) {}

	@SuppressWarnings("unused")
	default void textValueGroup(String text, String type) {}

	default void closeGroup() {}

	@SuppressWarnings("unused")
	default void unexpected(String text) {}

}
